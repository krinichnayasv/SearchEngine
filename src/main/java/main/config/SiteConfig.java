package main.config;

public class SiteConfig {

    private String url;
    private String name;

    public SiteConfig(String name, String url) {
        this.url = url;
        this.name = name;
    }

    public SiteConfig() {
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
}
