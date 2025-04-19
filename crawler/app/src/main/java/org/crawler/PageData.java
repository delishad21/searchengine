package org.crawler;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PageData implements Serializable {
    private String title;
    private String metadata;
    private Map<String, Integer> keywords;
    private List<Integer> childLinks;
    private List<Integer> parentLinks;

    public PageData(String title, String metadata, Map<String, Integer> keywords, List<Integer> childLinks,
            List<Integer> parentLinks) {
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

    public List<Integer> getChildLinks() {
        return childLinks;
    }

    public List<Integer> getParentLinks() {
        return parentLinks;
    }

    public void addParentLink(Integer parentUrl) {
        if (!parentLinks.contains(parentUrl)) {
            parentLinks.add(parentUrl);
        }
    }
}
