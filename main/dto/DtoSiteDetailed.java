package main.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import service.GetBeans;
import model.Site;
import model.SiteStatus;
import service.DBMethods;

public class DtoSiteDetailed {

    private DBMethods dbMethods = new GetBeans().getDbMethods();

    @JsonProperty("url")
    private String url;
    @JsonProperty("name")
    private String name;
    @JsonProperty("status")
    private SiteStatus status;
    @JsonProperty("error")
    private String error;
    @JsonProperty("statusTime")
    private long statusTime;
    @JsonProperty("pages")
    private long pages;
    @JsonProperty("lemmas")
    private long lemmas;

    public DtoSiteDetailed(Site site)  {
    this.url = site.getUrl();
    this.name = site.getName();
    this.status = site.getStatus();
    this.statusTime = site.getStatusTime();
    this.error = site.getLastError();
    this.pages = dbMethods.getCountPagesBySite(site);
    this.lemmas = dbMethods.getLemmasCountBySite(site);
//    this.pages = site.getPages().size();
//    this.lemmas = site.getLemmas().size();
       }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SiteStatus getStatus() {
        return status;
    }

    public void setStatus(SiteStatus status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(long statusTime) {
        this.statusTime = statusTime;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public long getLemmas() {
        return lemmas;
    }

    public void setLemmas(long lemmas) {
        this.lemmas = lemmas;
    }


}
