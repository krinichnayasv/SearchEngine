package model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum")
    private SiteStatus status;

    @Column (name = "status_time", nullable = false)
    private long statusTime;

    @Column (name = "last_error", nullable = true)
    private String lastError;

    @Column (name = "url", length = 255, nullable = false)
    private String url;

    @Column (name = "name", length = 255, nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "site" , fetch = FetchType.LAZY)
    private List<Page> pages;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "site" , fetch = FetchType.LAZY)
    private List<Lemma> lemmas;


    public Site() { }

    public Site(SiteStatus status, long statusTime, String lastError, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }

    public Site(String url, String name) {
        this.status = SiteStatus.INDEXING;
        this.statusTime = System.currentTimeMillis();
        this.lastError = "";
        this.url = url;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SiteStatus getStatus() {
        return status;
    }

    public void setStatus(SiteStatus status) {
        this.status = status;
    }

    public long getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(long statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
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

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public List<Lemma> getLemmas() {
        return lemmas;
    }

    public void setLemmas(List<Lemma> lemmas) {
        this.lemmas = lemmas;
    }


}
