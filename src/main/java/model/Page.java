package model;

import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Table(name = "page", indexes = {
        @Index(name = "index_path", columnList = "path")
        , @Index(name = "unique_SP", columnList = "id, site_id", unique = true)
})
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name = "path", nullable = false)
    private String path;

    @Column(name = "code", nullable = false)
    private int code;

    @Column (name = "content", nullable = false)
    private String content;

    @ManyToOne (cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Site site;


    public Page() {
    }

    public Page(String path, int code, String content, Site site) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.site = site;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }



}
