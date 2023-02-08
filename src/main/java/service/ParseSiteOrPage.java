package service;

import main.config.ConfigData;
import main.config.SiteConfig;
import model.Site;
import model.SiteStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;


public class ParseSiteOrPage {

    private static final String REGEX = "https?:\\/\\/[^,\\s]+";
    private static ConfigData configData = new ConfigData();
    private static List<SiteConfig>  sitesList = configData.getSites();
    private static String userAgent = configData.getUserAgent();
    private static String referrer = configData.getReferrer();
    private static boolean isIndexing = false;
    private static DBMethods dbMethods = SpringContext.getBean(DBMethods.class);
    private static List<Task> tasks = new ArrayList<>();

    public ParseSiteOrPage() {
    }

    public static List<Task> startIndexing() {
        setIndexing(true);
        dbMethods.deleteAllSites();

        for(SiteConfig siteConfig : sitesList) {
            Task task = new Task(siteConfig.getUrl().concat("/"), siteConfig.getName());
            Thread thread = new Thread(task);
            tasks.add(task);
            thread.start();
        }
        return tasks;
    }

    public static void stopIndexing(){
        getTasks().stream().forEach(t -> t.stop());
    }

    public static List<Task> getTasks() {
        return tasks;
    }

    public static void reIndexingPage(String url, String siteUrl) {
        Site site = dbMethods.getSiteByUrl(siteUrl);
        service.Node root = new service.Node(url, "");
        service.IndexingSites map = new service.IndexingSites(root, site, dbMethods);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(map);
    }

    public static List<Task> reIndexingSite(SiteConfig siteConfig) {
        Task task = new Task(siteConfig.getUrl().concat("/"), siteConfig.getName());
        Thread thread = new Thread(task);
        tasks.add(task);
        thread.start();
        return tasks;
    }

    public static boolean checkUrlForReindexing(String url) {
        boolean checked = false;
        if (!url.matches(REGEX)) {
            checked = false;
        } else {
            for(SiteConfig siteConfig : sitesList) {
                if(url.matches(siteConfig.getUrl())) {
                    checked = true;
                    reIndexingSite(siteConfig);
                    break;
                } else if(url.startsWith(siteConfig.getUrl())) {
                    checked = true;
                    reIndexingPage(url, siteConfig.getUrl());
                    break;
                }
            }
        }
        return checked;
    }

    public static boolean getIndexing() {
        List<Site> sites =  dbMethods.getAllSites();
        for(Site site : sites) {
            if(site.getStatus() == SiteStatus.INDEXING) {
                isIndexing = true;
                break;
            } else {
                isIndexing = false;
            }
        }
        return isIndexing;
    }

    public static SearchByQuery searchByQuery(String urlSite, String query) {
       return new SearchByQuery(query, urlSite, dbMethods);
    }

    public static String getUserAgent() {
        return userAgent;
    }

    public static String getReferrer() {
        return referrer;
    }

    public static void setIndexing(boolean indexing) {
        isIndexing = indexing;
    }



}