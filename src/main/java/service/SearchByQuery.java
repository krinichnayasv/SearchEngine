package service;

import main.config.ConfigData;
import model.Page;
import model.Site;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class SearchByQuery {

    private static final String REGEX = "[^A-Za-zА-Яа-я\\s]+";
    private static final float MIN_PERCENT_LEMMA = (float) 0.3;
    private static final int MAX_SIZE_SNIPPET = 250;
    private static final String USER_AGENT = new ConfigData().getUserAgent();
    private static final String REFERRER = new ConfigData().getReferrer();
    private String query;
    private TreeMap<String, String> lemmas = new TreeMap<>();
    private boolean allSites;
    private String site;
    private Site sentSite;
    private List<String> lemmaNotFound = new ArrayList<>();
    private List<String> lemmasForSearch = new ArrayList<>();
    private List<String> lemmasForSnippet = new ArrayList<>();
    private String[] wordsOfQuery;
    private StringBuilder newQuery;
    private float maxRelevance = 0;
    private static DBMethods dbMethods;


    public SearchByQuery(String query, String urlSite, DBMethods dbMethods) {
        this.query = query;
        this.site = urlSite;
        this.dbMethods = dbMethods;
    }


    public List<String> getLemmasForSearch(String query, String urlSite) throws SQLException {
        wordsOfQuery = query.toLowerCase().replaceAll(REGEX, " ").split("\\s+");
        lemmas = new Morphology().getLemmaForWord(wordsOfQuery);

        allSites = urlSite == null;
        sentSite = urlSite == null ? null : dbMethods.getSiteByUrl(urlSite);

        HashMap<String, Long> lemmasFrequency = urlSite != null
                ? DBConnection.getLemmasFrequencyForSite(lemmas, sentSite.getId())
                : DBConnection.getLemmasFrequencyForAllSites(lemmas);

        long pagesCount = urlSite != null ? dbMethods.getCountPagesBySite(sentSite) : dbMethods.findPagesCount();

        Map<String, Double> lemmasPercent = new LinkedHashMap<>();
        lemmasFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(entry -> lemmasPercent.put(entry.getKey(),
                        (double) ((int) ((double) entry.getValue() * 1000 / pagesCount)) / 1000));

        for (String lemma : lemmasPercent.keySet()) {
            lemmasForSnippet.add(lemma);
            if (lemmasPercent.get(lemma) < MIN_PERCENT_LEMMA) {
                lemmasForSearch.add(lemma);
            }
        }
        lemmasForSearch = lemmasForSearch.size() == 0 ? lemmasForSnippet : lemmasForSearch;
        return lemmasForSearch;
    }

    public List<RelevancePage> getResultOfSearch()
            throws SQLException, ExecutionException, InterruptedException {
        List<RelevancePage> result = new ArrayList<>();
        StringBuilder values = getResultPages(getLemmasForSearch(query, site));
        if (!values.isEmpty()) {
            Map<Page, Float> absRelev = getSortedRelevanceOfPages(values);
            for (Page page : absRelev.keySet()) {
                Callable<RelevancePage> task = () -> getRelevancePageResult(page, absRelev.get(page));
                FutureTask<RelevancePage> future = new FutureTask<>(task);
                new Thread(future).start();
                result.add(future.get());
            }
        }
        return result;
    }

    public RelevancePage getRelevancePageResult(Page page, float relevance) throws IOException, SQLException {
        RelevancePage relevancePage = new RelevancePage();
        String siteUrl;
        String siteName;

        if (allSites) {
            Map<String, String> sitesParam = DBConnection.getSiteOfPage(page);
            siteUrl = sitesParam.keySet().stream().findFirst().get();
            siteName = sitesParam.get(siteUrl);
        } else {
            siteUrl = sentSite.getUrl();
            siteName = sentSite.getName();
        }

        Document document = Jsoup.connect(siteUrl.concat(page.getPath())).ignoreHttpErrors(true)
                .userAgent(USER_AGENT).referrer(REFERRER).get();
        relevancePage.setSiteUrl(siteUrl);
        relevancePage.setSiteName(siteName);
        relevancePage.setRelevance(((int) (relevance * 100 / maxRelevance)) / 100);
        relevancePage.setUri(page.getPath());
        relevancePage.setTitle(document.title());
        relevancePage.setSnippet(getSnippet(document));
        return relevancePage;
    }

    public Map<Page, Float> getSortedRelevanceOfPages(StringBuilder values) throws SQLException {
        Map<Page, Float> absRelev = getRelevanceOfPages(values);
        Map<Page, Float> sortedMap = new LinkedHashMap<>();
        maxRelevance = absRelev.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .findFirst().get().getValue();
        absRelev.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }

    public Map<Page, Float> getRelevanceOfPages(StringBuilder values) throws SQLException {
        String reg = "[^0-9]";
        Map<Page, Float> absRelev = new HashMap<>();
        String[] pagesId = values.toString().replaceAll(reg, " ").trim().split("\\s+");

        for (String pageId : pagesId) {
            int id = Integer.parseInt(pageId);
            Page page = dbMethods.getPageById(id);
            float absRel = DBConnection.getRelevanceOfPage(lemmasForSearch, id);
            absRelev.put(page, absRel);
        }
        return absRelev;
    }

    public String getSnippet(Document document) {
        StringBuilder snip = new StringBuilder();
        String[] wordsOfPage = document.text().split("\\s+");
        TreeMap<Integer, String> positionOfWord = getPositionOfWord(wordsOfPage, document);

        int start = positionOfWord.firstKey();
        for (int i = start > 5 ? start - 4 : start; i < wordsOfPage.length; i++) {
            snip.append((positionOfWord.containsKey(i) ? "<b>" + wordsOfPage[i] + "</b>" : wordsOfPage[i])).append(" ");
            if (snip.length() >= MAX_SIZE_SNIPPET) {
                break;
            }
        }
        snip.append("...");

        return snip.toString().trim();
    }


    public TreeMap<Integer, String> getPositionOfWord(String[] wordsOfPage, Document document) {
        TreeMap<Integer, String> positionOfWord = new TreeMap<>();
        TreeMap<String, String> lemmasOfPage =
                new Morphology().getLemmaForWord(document.text().toLowerCase().split("\\s+"));

        for (int i = 0; i < wordsOfPage.length; i++) {
            for (String word : lemmasOfPage.keySet()) {
                int compareResult = word.compareTo(wordsOfPage[i].toLowerCase().trim());
                if (compareResult == 0 && lemmasForSnippet.contains(lemmasOfPage.get(word))) {
                    positionOfWord.put(i, wordsOfPage[i].trim());
                }
            }
        }
        return positionOfWord;
    }


    public StringBuilder getResultPages(List<String> lemmasForSearch) throws SQLException {
        StringBuilder values = new StringBuilder();
        if (!lemmasForSearch.isEmpty()) {
            StringBuilder sitesList = getSitesList();
            for (String lemma : lemmasForSearch) {
                StringBuilder pages = getEndPagesBySites(values, lemma, sitesList);
                values = pages;
            }

            if (!lemmaNotFound.isEmpty() && lemmaNotFound.size() < lemmasForSearch.size()) {
                newQuery = new StringBuilder();
                for (String s : wordsOfQuery) {
                    newQuery.append(lemmaNotFound.contains(lemmas.get(s)) ? "" : s + " ");
                }
            }
        }
        return values;
    }


    public StringBuilder getSitesList() {
        StringBuilder sitesList = new StringBuilder();
        if (allSites) {
            List<Site> sites = dbMethods.getAllSites();
            for (Site site : sites) {
                sitesList.append((sitesList.length() == 0 ? "" : ",") + "'" + site.getId() + "'");
            }
        } else {
            sitesList.append("'" + sentSite.getId() + "'");
        }
        return sitesList;
    }


    public StringBuilder getEndPagesBySites(StringBuilder values, String lemma, StringBuilder sites)
            throws SQLException {
        StringBuilder newValues;
        newValues = DBConnection.getPages(lemma, values, sites);
        if (newValues.toString().isEmpty()) {
            lemmaNotFound.add(lemma);
            lemmasForSnippet.remove(lemma);
            newValues = values;
        }
        return newValues;
    }

    public StringBuilder getNewQuery() {
        return newQuery;
    }

}
