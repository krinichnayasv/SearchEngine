package service;

import model.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;


public class IndexingSites extends RecursiveAction {

    private static DBMethods dbMethods;
    private volatile String urlStart;
    private Node node;
    private String url;
    private volatile static ArrayList<String> gettedSites = new ArrayList<>();
    private Connection.Response response = null;
    private volatile Site site;
    private HashMap<String, Integer> lemmas = new HashMap<>();
    private Page page;
    private boolean isReindexingPage = false;
    private volatile boolean isRunning = true;



    public IndexingSites(Node node, Site site) {
        this.node = node;
        this.site = site;
       }

       // reindex page
    public IndexingSites(Node node, Site site, DBMethods dbMethods) {
        this.dbMethods = dbMethods;
        this.node = node;
        this.site = site;
        this.isReindexingPage = true;
    }

        // start index/reindex sites
    public IndexingSites(Node node, String urlStart, DBMethods dbMethods) {
        this.dbMethods = dbMethods;
        this.node = node;
        this.urlStart = urlStart;
        if(dbMethods.ifSiteExist(urlStart.substring(0,urlStart.length()-1))) {
            dbMethods.deleteSite(dbMethods.getSiteByUrl(urlStart.substring(0,urlStart.length()-1)).getId());
        }
        site = new Site(urlStart.substring(0,urlStart.length()-1), node.getName());
        dbMethods.insertIntoSite(site);
       }


    @Override
    protected void compute() {
        int delay = new Random().nextInt(4500) + 500;
        List<IndexingSites> subTasks = new LinkedList<>();
        int statusCode = 0;
        String htmlOfSite = "";
        url = node.getUrl().trim();

        try {
            Thread.sleep(delay);
            Document document = Jsoup.connect(url).userAgent(ParseSiteOrPage.getUserAgent())
                    .referrer(ParseSiteOrPage.getReferrer()).get();
            htmlOfSite = document.outerHtml();
            statusCode = Jsoup.connect(url).userAgent(ParseSiteOrPage.getUserAgent())
                    .referrer(ParseSiteOrPage.getReferrer()).timeout(2000).ignoreHttpErrors(true)
                    .execute().statusCode();
            String allText = document.text();
            String metaContent = document.getElementsByTag("meta").attr("content"); // надо ли?
            String bodyText = document.body().text();
            String titleText = document.title();

            page = setPage(url, site, statusCode, htmlOfSite);
            lemmas = new Morphology().getLemmas(allText.concat(" ").concat(metaContent));
            dbMethods.insertOrUpdateIntoLemmaAllJDBC(lemmas, site.getId());
            indexingPage(page, titleText, bodyText, metaContent);

            if (!isReindexingPage) {
                Elements elements = document.select("a");
                for (Element el : elements) {
                    String pageUrl = el.attr("abs:href");
                    makeChildTasks(pageUrl, site, subTasks);
                }
            }
        } catch (Exception ex) {
            setExceptionForSite(site, statusCode);
        }

        if (!isReindexingPage) {
            ForkJoinTask.invokeAll(subTasks);
        } else {
            setExceptionForReindexPage(site);
        }
    }

    private void makeChildTasks(String pageUrl, Site site, List<IndexingSites> subTasks) {
        if (!pageUrl.isEmpty() && pageUrl.startsWith(site.getUrl().concat("/")) && !pageUrl.contains("#")
                && !pageUrl.matches(site.getUrl().concat("/")) && !pageUrl.matches(url)
                && !gettedSites.contains(pageUrl)
                && !pageUrl.toLowerCase().endsWith(".jpg") && !pageUrl.toLowerCase().endsWith(".png")
                && !pageUrl.toLowerCase().endsWith(".jpeg") && !pageUrl.contains("/?")
                && !pageUrl.toLowerCase().endsWith(".mp4") && !pageUrl.toLowerCase().endsWith(".mp3")
                && !pageUrl.toLowerCase().contains("login?") && !pageUrl.toLowerCase().contains(".pdf")
                && !pageUrl.toLowerCase().contains(".ppt") && !pageUrl.toLowerCase().contains(".doc")
                && !pageUrl.toLowerCase().contains(".csv") && !pageUrl.toLowerCase().contains(".xls")
                && !pageUrl.toLowerCase().contains(".zip") && isRunning()) {
            Node child = new Node(pageUrl.trim(), "");
            IndexingSites task = new IndexingSites(child, site);
            task.fork();
            subTasks.add(task);
            node.addChild(child);
            gettedSites.add(pageUrl.trim());
        }
    }

    private void setExceptionForSite(Site site, int statusCode) {
        ExceptionByIndexing exception = new ExceptionByIndexing();
        if (isReindexingPage) {
            site.setLastError(exception.getErrorMessageForReindexingPage(statusCode));
        } else {
            site.setLastError(exception.getErrorMessageForSite(statusCode));
        }
    }

    private void setExceptionForReindexPage(Site site) {
        if (site.getLastError().length() > 0) {
            site.setStatus(SiteStatus.FAILED);
        } else {
            site.setStatus(SiteStatus.INDEXED);
        }
        site.setStatusTime(System.currentTimeMillis());
        dbMethods.insertIntoSite(site);
    }

    private Page setPage(String url, Site site, int statusCode, String htmlOfSite) {

        page = new Page(url.replace(site.getUrl().concat("/"), "/"), statusCode, htmlOfSite, site);
        if (dbMethods.ifPageExist(url.replace(site.getUrl().concat("/"), "/"), site.getId())
                && isReindexingPage == true) {
            Page oldPage = dbMethods.getPageByPath(url.replace(site.getUrl().concat("/"), "/"), site.getId());
            dbMethods.updateLemmaSetLessFrequency(dbMethods.getLemmasIdListByPage(oldPage));
            dbMethods.deletePage(oldPage.getId());
            dbMethods.insertIntoPage(page);
            dbMethods.updateSiteOnIndexing(site);
        } else if (!dbMethods.ifPageExist(url.replace(site.getUrl().concat("/"), "/"), site.getId())) {
            dbMethods.insertIntoPage(page);
            dbMethods.updateSiteOnIndexing(site);
        } else {
            page = dbMethods.getPageByPath(url.replace(site.getUrl().concat("/"), "/"), site.getId());
        }
        return page;
    }

    private void indexingPage(Page page, String titleText, String bodyText, String metaContent)
            throws IOException, SQLException {
        if ((page.getCode() < 400 || page.getCode() > 599)) {
            String query = CalculateIndexForLemmaOnPage.getIndexes(titleText,
                    bodyText.concat(" ").concat(metaContent), page.getId(), page.getSite().getId());
            dbMethods.insertIntoIndexJDBC(query);
        }
    }


    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }


}
