package service;

import model.Page;
import model.Site;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class SearchByQuery {

    private static final String REGEX = "[^A-Za-zА-Яа-я\\s]+";
    private static final float MIN_PERCENT_LEMMA = (float) 0.3;
    private static final int MAX_SIZE_SNIPPET = 300;
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
    private long countPages = 0;
    private static DBMethods dbMethods;


    public SearchByQuery(String query, String urlSite, DBMethods dbMethods) {
        this.query = query;
        this.site = urlSite;
        this.dbMethods = dbMethods;
    }


    public List<String> getLemmasForSearch(String query, String urlSite) throws SQLException {
        wordsOfQuery = query.toLowerCase().replaceAll(REGEX, " ").split("\\s+");
        lemmas = new Morphology().getLemmaForWord(wordsOfQuery);

        if (urlSite == null) {
            allSites = true;
        } else {
            allSites = false;
            sentSite = dbMethods.getSiteByUrl(urlSite);
        }

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
            throws SQLException, ExecutionException, InterruptedException, IOException {
        List<RelevancePage> result = new ArrayList<>();
        StringBuilder values = getResultPages(getLemmasForSearch(query, site));
        if (!values.isEmpty()) {
            Map<Page, Float> absRelev = getRelevanceOfPages(values);
            for (Page page : absRelev.keySet()) {
                Callable task = () -> getRelevancePageResult(page, absRelev.get(page));
                FutureTask<RelevancePage> future = new FutureTask<>(task);
                new Thread(future).start();
                result.add(future.get());
            }
        }
        return result;
    }

    public RelevancePage getRelevancePageResult(Page page, float relevance) throws IOException, SQLException {
        RelevancePage relevancePage = new RelevancePage();
        String siteUrl = "";
        String siteName = "";

        if (allSites) {
            ResultSet rs = DBConnection.getConnection().createStatement()
                    .executeQuery("SELECT s.* FROM page p, site s WHERE p.id = '" + page.getId() + "' AND " +
                            "s.id = p.site_id");
            while (rs.next()) {
                siteUrl = rs.getString("url");
                siteName = rs.getString("name");
            }
            rs.close();
        } else {
            siteUrl = sentSite.getUrl();
            siteName = sentSite.getName();
        }

        Document document = Jsoup.connect(siteUrl.concat(page.getPath())).ignoreHttpErrors(true)
                .userAgent(ParseSiteOrPage.getUserAgent())
                .referrer(ParseSiteOrPage.getReferrer()).get();
        relevancePage.setSiteUrl(siteUrl);
        relevancePage.setSiteName(siteName);
        relevancePage.setRelevance(((int) (relevance * 100 / maxRelevance)) / 100);
        relevancePage.setUri(page.getPath());
        relevancePage.setTitle(document.title());
        relevancePage.setSnippet(getSnippet(document));
        return relevancePage;
    }

    public Map<Page, Float> getRelevanceOfPages(StringBuilder values) throws SQLException {
        Map<Page, Float> sortedMap = new LinkedHashMap<>();
        String reg = "[^0-9]";
        Map<Page, Float> absRelev = new HashMap<>();
        String[] pagesId = values.toString().replaceAll(reg, " ").trim().split("\\s+");

        for (int i = 0; i < pagesId.length; i++) {
            int id = Integer.parseInt(pagesId[i]);
            float absRel = 0;
            Page page = dbMethods.getPageById(id);
            for (String lemma : lemmasForSearch) {
                ResultSet rsRank = DBConnection.getConnection().createStatement()
                        .executeQuery("SELECT ind.`rank` FROM lemma l, `index` ind WHERE l.id = ind.lemma_id " +
                                "AND l.lemma = '" + lemma + "' AND ind.page_id = '" + id + "'");
                while (rsRank.next()) {
                    absRel += rsRank.getFloat("rank");
                }
                rsRank.close();
            }
            absRelev.put(page, absRel);
        }

        countPages = absRelev.size();
        maxRelevance = absRelev.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .findFirst().get().getValue();
        absRelev.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .forEachOrdered(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
        return sortedMap;
    }


    public String getSnippet(Document document) {

        String contentOfPage = document.text();
        StringBuilder snip = new StringBuilder();
        String[] wordsOfPage = document.text().split("\\s+");
        TreeMap<Integer, String> positionOfWord = getPositionOfWord(wordsOfPage, contentOfPage);

        if ((positionOfWord.lastKey() - positionOfWord.firstKey()) <= 20
                && (positionOfWord.lastKey() - positionOfWord.firstKey()) >= 10) {
            for (int i = positionOfWord.firstKey(); i <= positionOfWord.lastKey(); i++) {
                snip.append((positionOfWord.containsKey(i) ? "<b>" + wordsOfPage[i] + "</b>" : wordsOfPage[i])).append(" ");
                if (snip.length() >= MAX_SIZE_SNIPPET) {
                    break;
                }
            }
        } else {
            int previousPosition = 0;
            int start = positionOfWord.firstKey();
            for (Integer position : positionOfWord.keySet()) {
                if ((position - start) <= 15) {
                    previousPosition = position;
                } else {
                    for (int i = start; i <= (previousPosition + 10) && i < wordsOfPage.length; i++) {
                        snip.append((positionOfWord.containsKey(i) ? "<b>" + wordsOfPage[i] + "</b>" : wordsOfPage[i]))
                                .append(" ");
                        if (snip.length() >= MAX_SIZE_SNIPPET) {
                            break;
                        }
                    }
                    snip.append("...");
                    start = position - 2;
                }
            }
        }
        return snip.toString().trim();
    }


    public TreeMap<Integer, String> getPositionOfWord(String[] wordsOfPage, String contentOfPage) {
        TreeMap<Integer, String> positionOfWord = new TreeMap<>();
        TreeMap<String, String> lemmasOfPage =
                new Morphology().getLemmaForWord(contentOfPage.toLowerCase().split("\\s+"));

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
        if (lemmasForSearch.size() > 0) {
            StringBuilder sitesList = getSitesList();
            for (String lemma : lemmasForSearch) {
                StringBuilder pages = getEndPagesBySites(values, lemma, sitesList);
                values = pages;
            }

            if (lemmaNotFound.size() == lemmasForSearch.size()) {
            } else if (lemmaNotFound.size() > 0 && lemmaNotFound.size() < lemmasForSearch.size()) {
                newQuery = new StringBuilder();
                for (int i = 0; i < wordsOfQuery.length; i++) {
                    if (!lemmaNotFound.contains(lemmas.get(wordsOfQuery[i]))) {
                        newQuery.append(wordsOfQuery[i] + " ");
                    }
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
        if (newValues == null || newValues.toString().isEmpty()) {
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
