package main.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import service.DBMethods;
import service.SpringContext;

@JsonRootName(value="total")
public class DtoTotalStatistic {


    @JsonProperty("sites")
    private int sites;
    @JsonProperty("pages")
    private long pages;
    @JsonProperty("lemmas")
    private long lemmas;
    @JsonProperty("isIndexing")
    private boolean isIndexing;

    public DtoTotalStatistic (int sites, long pages, long lemmas, boolean isIndexing) {
        this.sites = sites;
        this.pages = pages;
        this.lemmas = lemmas;
        this.isIndexing = isIndexing;
    }

    public DtoTotalStatistic () {
        }


    public int getSites() {
     return sites;
    }

    public void setSites(int sites) {
        this.sites = sites;
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

    public boolean getIsIndexing() {
        return isIndexing;
    }

    public void setIsIndexing(boolean isIndexing) {
        isIndexing = isIndexing;
    }


}
