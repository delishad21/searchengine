package org.example;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PageData implements Serializable {
    private String title;
    private String metadata;
    private Map<String, Integer> keywords;
    private List<String> childLinks;
    private List<String> parentLinks;

    public PageData(String title, String metadata, Map<String, Integer> keywords, List<String> childLinks,
            List<String> parentLinks) {
        this.title = title;
        this.metadata = metadata;
        this.keywords = keywords;
        this.childLinks = childLinks;
        this.parentLinks = parentLinks;
    }

    public String getTitle() {
        return title;
    }

    public String getMetadata() {
        return metadata;
    }

    public Map<String, Integer> getKeywords() {
        return keywords;
    }

    public List<String> getChildLinks() {
        return childLinks;
    }

    public List<String> getParentLinks() {
        return parentLinks;
    }

    public void addParentLink(String parentUrl) {
        if (!parentLinks.contains(parentUrl)) {
            parentLinks.add(parentUrl);
        }
    }
}
