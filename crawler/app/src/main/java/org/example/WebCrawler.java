package org.example;

import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class WebCrawler {
    private Set<String> visited = new HashSet<>();
    private Queue<String> queue = new LinkedList<>();
    private int maxPages;
    private Indexer indexer;

    public WebCrawler(int maxPages) {
        this.maxPages = maxPages;
        this.indexer = new Indexer();
    }

    public void startCrawling(String startUrl) {
        queue.add(startUrl);
        int pageCount = 0;

        while (!queue.isEmpty() && pageCount < maxPages) {
            String url = queue.poll();
            if (!visited.contains(url)) {
                processPage(url);
                pageCount++;
            }
        }
        indexer.close(); // Save JDBM data
    }

    private void processPage(String url) {
        System.out.println("Processing: " + url);
        try {
            if (!shouldFetch(url))
                return;

            Parser parser = new Parser(url);

            // Extract Title
            NodeList titleNodes = parser.extractAllNodesThatMatch(new TagNameFilter("title"));
            String title = (titleNodes != null && titleNodes.size() > 0)
                    ? titleNodes.elementAt(0).toPlainTextString().trim()
                    : "No Title";

            // Extract Body
            Parser bodyParser = new Parser(url);
            NodeList bodyNodes = bodyParser.extractAllNodesThatMatch(new TagNameFilter("body"));
            String body = (bodyNodes != null && bodyNodes.size() > 0)
                    ? bodyNodes.elementAt(0).toPlainTextString().trim()
                    : "";

            int size = body.length();
            String lastModified = getLastModified(url);

            // Extract child links
            Parser linkParser = new Parser(url);
            NodeList links = linkParser.extractAllNodesThatMatch(new TagNameFilter("a"));
            List<String> childLinks = new ArrayList<>();

            for (int i = 0; i < links.size() && childLinks.size() < 10; i++) {
                TagNode tag = (TagNode) links.elementAt(i);
                String link = tag.getAttribute("href");

                if (link != null) {
                    link = resolveURL(url, link); // Convert to absolute URL
                    if (!visited.contains(link)) {
                        queue.add(link);
                        childLinks.add(link);
                    }
                }
            }

            visited.add(url);
            indexer.indexPage(url, title, body, lastModified, size, childLinks);

        } catch (ParserException e) {
            System.out.println("Failed to parse: " + url);
        }
    }

    private boolean shouldFetch(String url) {
        if (visited.contains(url))
            return false;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400);
        } catch (IOException e) {
            return false;
        }
    }

    private String getLastModified(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            long date = connection.getLastModified();
            return (date == 0) ? "Unknown" : new Date(date).toString();
        } catch (IOException e) {
            return "Unknown";
        }
    }

    private String resolveURL(String baseUrl, String link) {
        try {
            URL base = new URL(baseUrl);
            return new URL(base, link).toString(); // Convert relative to absolute URL
        } catch (Exception e) {
            return link; // Return as-is if resolution fails
        }
    }
}
