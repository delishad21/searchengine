package org.search;

import java.util.List;
import java.util.Map;

public class SearchResult {
    private double score;
    private String title;
    private String url;
    private String metadata;
    private Map<String, Integer> keywords;
    private List<Integer> parentLinks;
    private List<Integer> childLinks;

    public SearchResult(double score, String title, String url, String metadata, Map<String, Integer> keywords,
            List<Integer> parentLinks, List<Integer> childLinks) {
        this.score = score;
        this.title = title;
        this.url = url;
        this.metadata = metadata;
        this.keywords = keywords;
        this.parentLinks = parentLinks;
        this.childLinks = childLinks;
    }

    public double getScore() {
        return score;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getMetadata() {
        return metadata;
    }

    public Map<String, Integer> getKeywords() {
        return keywords;
    }

    public List<Integer> getParentLinks() {
        return parentLinks;
    }

    public List<Integer> getChildLinks() {
        return childLinks;
    }
}
