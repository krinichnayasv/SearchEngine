package main.config;


import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigData {

    private  Map<String, Object> dataMap;
    private  Map<String, Object> properties;
    private  Map<String, Object> datasource;
    private  String userAgent;
    private  String referrer;
    private  List<SiteConfig> sites = new ArrayList<>();
    private String url;
    private String username;
    private String password;

    public ConfigData(){
        this.dataMap = getConfigParametersMap();
    }


    private Map<String, Object> getConfigParametersMap() {

        try {
            File file  = new File("application.yml");
            InputStream inputStream = new FileInputStream(file);
            Yaml yaml = new Yaml();
            dataMap = yaml.load(inputStream);

        } catch (Exception ex) {
            ex.getMessage();
        }

        return dataMap;
    }

    public Map<String, Object> getProperties() {
        return ( Map<String, Object>) getConfigParametersMap().get("properties");
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getDatasources() {
        Map<String, Object> spring = ( Map<String, Object>) getConfigParametersMap().get("spring");
        datasource = ( Map<String, Object>) spring.get("datasource");
        return datasource;
    }

    public void setDatasources(Map<String, Object> datasource) {
        this.datasource = datasource;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public String getUserAgent() {
        return (String) getProperties().get("user-agent");
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getReferrer() {
        return (String) getProperties().get("referrer");
    }
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    public List<SiteConfig> getSites() {
        List<Map<String, String>> list = ( List<Map<String, String>>) getProperties().get("sites");
        for (int i = 0; i < list.size(); i++) {
            SiteConfig siteConfig = new SiteConfig(list.get(i).get("name"),list.get(i).get("url"));
            sites.add(siteConfig);
        }
        return sites;
    }
    public void setSites(List<SiteConfig> siteList) {
        this.sites = siteList;
    }

    public String getUrl() {
        return (String) getDatasources().get("url");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return (String) getDatasources().get("username");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return (String) getDatasources().get("password");
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
