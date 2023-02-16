package service;

import model.Site;
import model.SiteStatus;

import java.util.concurrent.ForkJoinPool;

public class Task implements Runnable{

    private String urlStart;
    private String siteName;
    private DBMethods dbMethods = SpringContext.getBean(DBMethods.class);
    private volatile boolean isRunning;
    private ForkJoinPool pool;
    private service.IndexingSites map;

    public Task(String urlStart, String siteName) {
        this.urlStart = urlStart;
        this.siteName = siteName;
        this.isRunning = true;

    }


    @Override
    public void run() {
        service.Node root = new service.Node(getUrlStart(), getSiteName());
        map = new service.IndexingSites(root, urlStart, dbMethods);
        pool = new ForkJoinPool();
        pool.execute(map);

        Site site = dbMethods.getSiteByUrl(urlStart.substring(0, urlStart.length() - 1));

        if (isRunning()) {
            if (site.getLastError().length() > 0 || site.getLastError() != null) {
                site.setStatus(SiteStatus.FAILED);
            } else {
                site.setStatus(SiteStatus.INDEXED);
            }
        } else {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Индексация была прервана");
        }
        site.setStatusTime(System.currentTimeMillis());
        dbMethods.insertIntoSite(site);
    }


    public void stop() {
        isRunning = false;
        map.setRunning(false);
        pool.shutdownNow();
        Site site = dbMethods.getSiteByUrl(urlStart.substring(0, urlStart.length() - 1));
        if (site.getStatus() != SiteStatus.INDEXED) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Индексация сайта была прервана");
            site.setStatusTime(System.currentTimeMillis());
            dbMethods.insertIntoSite(site);
        }
    }

    public String getUrlStart() {
        return urlStart;
    }

    public void setUrlStart(String urlStart) {
        this.urlStart = urlStart;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
