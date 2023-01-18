package service;

import java.util.ArrayList;

public class Node {

    private String url;
    private ArrayList<Node> children;
    private String name;

    public Node (String url, String name)    {
        this.url = url;
        children = new ArrayList<>();
        this.name = name;
    }

    public void addChild (Node node) {
        children.add(node);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}