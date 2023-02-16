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
    private static final List<SiteConfig> SITES_LIST = new ConfigData().getSites();
    private static boolean isIndexing = false;
    private static DBMethods dbMethods = SpringContext.getBean(DBMethods.class);
    private static List<Task> tasks = new ArrayList<>();

    public ParseSiteOrPage() {
    }

    public static List<Task> startIndexing() {
        setIndexing(true);
        dbMethods.deleteAllSites();

        for(SiteConfig siteConfig : SITES_LIST) {
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
        Runnable run = () -> {
            Site site = dbMethods.getSiteByUrl(siteUrl);
            service.Node root = new service.Node(url, "");
            service.IndexingSites map = new service.IndexingSites(root, site, dbMethods);
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(map);
        };
        Thread thread = new Thread(run);
        thread.start();
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
            return false;
        } else {
            for(SiteConfig siteConfig : SITES_LIST) {
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

    public static void setIndexing(boolean indexing) {
        isIndexing = indexing;
    }



}