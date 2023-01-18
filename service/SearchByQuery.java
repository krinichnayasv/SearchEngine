package service;

import model.Page;
import model.Site;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SearchByQuery {

    private static final String REGEX = "[^A-Za-zА-Яа-я\\s]+";
    private String query;
    private TreeMap<String, String> lemmas = new TreeMap<>();
    private boolean allSites;
    private String site;
    private Site sendSite;
    private List<Page> pages;
    private List<String> lemmaNotFound = new ArrayList<>();
    private List<String> lemmasForSearch = new ArrayList<>();
    private List<String> lemmasForSnippet = new ArrayList<>();
    private String[] wordsOfQuery;
    private StringBuilder newQuery;
    private float maxRelevance = 0;
    private long countPages = 0;
    private static DBMethods dbMethods;


    public SearchByQuery(String query, String urlSite, DBMethods dbMethods) {
        this.query = query;
        this.site = urlSite;
        this.dbMethods = dbMethods;
    }



    public  List<String> getLemmasForSearch(String query, String urlSite)
            throws IOException, SQLException {

        wordsOfQuery = query.toLowerCase().replaceAll(REGEX, " ").split("\\s+");
        lemmas = new Morphology().getLemmaForWord(wordsOfQuery);

        if(urlSite == null) {
            allSites = true;
        } else {
            allSites = false;
            sendSite = dbMethods.getSiteByUrl(urlSite);
        }

        HashMap<String, Long> lemmasFrequency = new HashMap<>();
        for(String word : lemmas.keySet()) {
            if(!lemmasFrequency.containsKey(lemmas.get(word))) {
            ResultSet rs;
                if (urlSite != null) {
                    rs = DBConnection.getConnection().createStatement()
                            .executeQuery("SELECT frequency FROM lemma WHERE lemma = '" + lemmas.get(word)
                                    + "' AND site_id = '" + sendSite.getId() + "'");
                    while (rs.next()) {
                        lemmasFrequency.put(lemmas.get(word), rs.getLong("frequency"));
                    }
                } else {
                    rs = DBConnection.getConnection().createStatement()
                            .executeQuery("SELECT sum(frequency) as freq_sum FROM lemma WHERE lemma = '"
                                    + lemmas.get(word) + "'");
                    while (rs.next()) {
                        lemmasFrequency.put(lemmas.get(word), rs.getLong("freq_sum"));
                    }
                }
                rs.close();
            } else {
                continue;
            }
        }

       long pagesCount = urlSite != null ?  dbMethods.getCountPagesBySite(sendSite) : dbMethods.findPagesCount();

        Map<String, Double> lemmasPercent = new LinkedHashMap<>();
        lemmasFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(entry -> lemmasPercent.put(entry.getKey(),
                        (double) ((int)((double)entry.getValue() * 1000 / pagesCount))/1000));

        for(String lemma : lemmasPercent.keySet()) {
            lemmasForSnippet.add(lemma);
            if(lemmasPercent.get(lemma) < 0.3) {
                lemmasForSearch.add(lemma);
            } else {
                continue;
            }
        }

        return lemmasForSearch;
    }

    public List<RelevancePage> getResultOfSearch() throws SQLException, IOException {

        List<RelevancePage> result = new ArrayList<>();
        StringBuilder values = getResultPages(getLemmasForSearch(query, site));
        if(!values.isEmpty()) {
            Map<Page, Float> absRelev = getRelevanceOfPages(values);
            for(Page page : absRelev.keySet()) {
                result.add(getRelevancePageResult(page,absRelev.get(page)));
            }
        }
        return result;
    }

    public RelevancePage getRelevancePageResult(Page page, float relevance) throws IOException, SQLException {
        RelevancePage relevancePage = new RelevancePage();
        int siteId = 0;
        String siteUrl = "";
        String siteName = "";

        if(allSites) {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT site_id FROM page WHERE id = '" + page.getId() + "'");
            while (rs.next()) {
                siteId = rs.getInt("site_id");
            }
            rs.close();

            ResultSet rsSite = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT * FROM site WHERE id = '" + siteId + "'");
            while (rsSite.next()) {
                siteUrl = rsSite.getString("url");
                siteName = rsSite.getString("name");
            }
            rsSite.close();
        } else {
            siteUrl = sendSite.getUrl();
            siteName = sendSite.getName();
        }

        Document document = Jsoup.connect(siteUrl.concat(page.getPath())).ignoreHttpErrors(true)
                .userAgent(ParseSiteOrPage.getUserAgent())
                .referrer(ParseSiteOrPage.getReferrer()).get();
        relevancePage.setSiteUrl(siteUrl);
        relevancePage.setSiteName(siteName);
        relevancePage.setRelevance(( (int) (relevance * 100 /maxRelevance)) / 100);
        relevancePage.setUri(page.getPath());
        relevancePage.setTitle(document.title());
        relevancePage.setSnippet(getSnippet(document));
        return relevancePage;
    }

    public  Map<Page, Float> getRelevanceOfPages(StringBuilder values) throws SQLException{
        String reg = "[^0-9]";
        Map<Page, Float> absRelev = new HashMap<>();
        String[] pagesId = values.toString().replaceAll(reg, " ").trim().split("\\s+");

        for (int i = 0; i < pagesId.length; i++) {
            int id = Integer.parseInt(pagesId[i]);
            float absRel = 0;
            Page page = dbMethods.getPageById(id);
            for(String lemma : lemmasForSearch) {
                ResultSet rsRank = DBConnection.getConnection().createStatement()
                        .executeQuery("SELECT ind.`rank` FROM lemma l, `index` ind WHERE l.id = ind.lemma_id " +
                                "AND l.lemma = '" + lemma + "' AND ind.page_id = '" + id + "'");
                while(rsRank.next()) {
                    absRel += rsRank.getFloat("rank");
                }
                rsRank.close();
            }
            absRelev.put(page, absRel);
        }

        countPages = absRelev.size();
        maxRelevance =  absRelev.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .findFirst().get().getValue();
        Map<Page, Float> sortedMap = new LinkedHashMap<>();
        absRelev.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        return sortedMap;
    }

    public String getSnippet(Document document) throws IOException {

        String contentOfPage = document.text();
        StringBuilder snip = new StringBuilder();

        TreeMap<Integer, String> positionOfWord = new TreeMap<>();
        String[] wordsOfPage = contentOfPage.split("\\s+");
        TreeMap<String, String> lemmasOfPage =
                new Morphology().getLemmaForWord(contentOfPage.toLowerCase().split("\\s+"));

        for(int i = 0; i < wordsOfPage.length; i++) {
            for(String word : lemmasOfPage.keySet()) {
                int compareResult = word.compareTo(wordsOfPage[i].toLowerCase().trim());
                if (compareResult == 0 && lemmasForSnippet.contains(lemmasOfPage.get(word))) {
                    positionOfWord.put(i, wordsOfPage[i].trim());
                    wordsOfPage[i].replace(wordsOfPage[i], "<b>" + wordsOfPage[i] + "</b>");
                }
            }
        }

        if((positionOfWord.lastKey() - positionOfWord.firstKey()) <= 20
                && (positionOfWord.lastKey() - positionOfWord.firstKey()) >= 10) {
            for(int i = positionOfWord.firstKey(); i <= positionOfWord.lastKey(); i++) {
                snip.append((positionOfWord.containsKey(i) ? "<b>" + wordsOfPage[i] + "</b>" : wordsOfPage[i]))
                        .append(" ");
            }
        } else {
            int previousPosition = 0;
            int start = positionOfWord.firstKey();
            for(Integer position : positionOfWord.keySet()) {
                if((position - start) <= 15) {
                    previousPosition = position;
                    continue;
                } else {
                    for(int i = start; i <= (previousPosition + 10) && i < wordsOfPage.length; i++) {
                        snip.append((positionOfWord.containsKey(i) ? "<b>" + wordsOfPage[i] + "</b>" : wordsOfPage[i]))
                                .append(" ");
                    }
                    snip.append("...");
                    start = position - 2;
                }
            }
        }
        return snip.toString().trim();
    }


    public StringBuilder getResultPages(List<String> lemmasForSearch)  throws SQLException {
        int n = 0;
        StringBuilder values = new StringBuilder();
        if(sendSite == null) {
            for(String lemma : lemmasForSearch) {
                StringBuilder pages = getEndPagesAllSites(n, values, lemma);
                values = pages;
                n++;
            }
        } else {
            for(String lemma : lemmasForSearch) {
                StringBuilder pages = getEndPagesBySite(n, values, lemma);
                values = pages;
                n++;
            }
        }

        if(lemmaNotFound.size() == lemmasForSearch.size()) {
            values = new StringBuilder();
        } else if(lemmaNotFound.size() > 0 && lemmaNotFound.size() < lemmasForSearch.size()) {
            newQuery = new StringBuilder();
            for (int i = 0; i < wordsOfQuery.length; i++) {
                if(lemmaNotFound.contains(lemmas.get(wordsOfQuery[i]))) {
                    continue;
                } else {
                    newQuery.append(wordsOfQuery[i] + " ");
                }
            }
            System.out.println(newQuery);
        } else {  }

        return values;
    }


    public StringBuilder getEndPagesAllSites(int n, StringBuilder values, String lemma) throws SQLException {
        StringBuilder newValues = new StringBuilder();
        if(n == 0) {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT DISTINCT(ind.page_id) FROM `index` ind, lemma l WHERE " +
                            "l.lemma = '" + lemma + "' AND l.id = ind.lemma_id");
                while (rs.next()) {
                    newValues.append((newValues.length() == 0 ? "" : ",") + "'" + rs.getInt("page_id") + "'");
                }
                rs.close();
            if(newValues == null || newValues.toString().isEmpty()) {
                ResultSet rsPages = DBConnection.getConnection().createStatement()
                        .executeQuery("SELECT id FROM page");
                lemmaNotFound.add(lemma);
                lemmasForSnippet.remove(lemma);
                while(rsPages.next()) {
                    newValues.append((newValues.length()==0 ? "" : ",") + "'" + rsPages.getInt("id") + "'");
                }
                rsPages.close();
            }
        } else {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT DISTINCT(ind.page_id) FROM `index` ind, lemma l WHERE " +
                            "l.lemma = '" + lemma + "' AND l.id = ind.lemma_id AND ind.page_id in (" +
                            values.toString() + ")");
                while (rs.next()) {
                    newValues.append((newValues.length() == 0 ? "" : ",") + "'" + rs.getInt("page_id") + "'");
                }
                rs.close();
            if(newValues == null || newValues.toString().isEmpty()) {
                lemmaNotFound.add(lemma);
                lemmasForSnippet.remove(lemma);
                newValues = values;
            }
        }
        return newValues;
    }


    public StringBuilder getEndPagesBySite(int n, StringBuilder values, String lemma) throws SQLException {
        StringBuilder newValues = new StringBuilder();
        if(n == 0) {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT DISTINCT(ind.page_id) FROM `index` ind, lemma l WHERE " +
                            "l.lemma = '" + lemma + "' AND l.id = ind.lemma_id " +
                            "AND l.site_id = '" + sendSite.getId() + "'");
                while(rs.next()) {
                    newValues.append((newValues.length() == 0 ? "" : ",") + "'" + rs.getInt("page_id") + "'");
                }
                rs.close();
            if(newValues == null || newValues.toString().isEmpty()) {
                lemmaNotFound.add(lemma);
                lemmasForSnippet.remove(lemma);
                ResultSet rsPages = DBConnection.getConnection().createStatement()
                        .executeQuery("SELECT * FROM page WHERE site_id = '" + sendSite.getId() + "'");
                while (rsPages.next()) {
                    int pageId = rsPages.getInt("id");
                    newValues.append((newValues.length() == 0 ? "" : ",") + "'" + pageId + "'");
                }
                rsPages.close();
            }
        } else {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT DISTINCT(ind.page_id) FROM `index` ind, lemma l WHERE " +
                            "l.lemma = '" + lemma + "' AND l.id = ind.lemma_id AND ind.page_id in (" +
                            values.toString() + ")");
                while (rs.next()) {
                    newValues.append((newValues.length() == 0 ? "" : ",") + "'" + rs.getInt("page_id") + "'");
                }
                rs.close();
            if(newValues == null || newValues.toString().isEmpty()) {
                lemmaNotFound.add(lemma);
                lemmasForSnippet.remove(lemma);
                newValues = values;
            }
        }
        return newValues;
    }

    public StringBuilder getNewQuery() {
        return newQuery;
    }

    public void setNewQuery(StringBuilder newQuery) {
        this.newQuery = newQuery;
    }

    public long getCountPages() {
        return countPages;
    }

    public void setCountPages(long countPages) {
        this.countPages = countPages;
    }

    public float getMaxRelevance() {
        return maxRelevance;
    }

    public void setMaxRelevance(float maxRelevance) {
        this.maxRelevance = maxRelevance;
    }

    public List<String> getLemmasForSearch() {
        return lemmasForSearch;
    }

    public void setLemmasForSearch(List<String> lemmasForSearch) {
        this.lemmasForSearch = lemmasForSearch;
    }

    public List<String> getLemmaNotFound() {
        return lemmaNotFound;
    }

    public void setLemmaNotFound(List<String> lemmaNotFound) {
        this.lemmaNotFound = lemmaNotFound;
    }

    public TreeMap<String, String> getLemmas() {
        return lemmas;
    }

    public void setLemmas(TreeMap<String, String> lemmas) {
        this.lemmas = lemmas;
    }

    public Site getSendSite() {
        return sendSite;
    }

    public void setSendSite(Site sendSite) {
        this.sendSite = sendSite;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
