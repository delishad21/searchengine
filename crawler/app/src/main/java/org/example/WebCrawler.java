package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
                System.out.println("Processing: " + (pageCount + 1) + " / " + maxPages + " : " + url);
                processPage(url);
                pageCount++;
            }
        }
        indexer.close(); // Save JDBM data
    }

    private void processPage(String url) {
        try {
            if (!shouldFetch(url))
                return;

            Document doc = Jsoup.connect(url).get();

            // Extract title
            String title = doc.title().trim();
            if (title.isEmpty())
                title = "No Title";

            // Extract body text (cleaned)
            String body = doc.body().text().trim();
            int size = body.length();
            String lastModified = getLastModified(url);

            // Extract child links (up to 10)
            List<String> childLinks = new ArrayList<>();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                if (childLinks.size() >= maxPages)
                    break;

                String absUrl = link.absUrl("href");
                if (!visited.contains(absUrl) && !absUrl.isEmpty()) {
                    queue.add(absUrl);
                    childLinks.add(absUrl);
                }
            }
            visited.add(url);
            indexer.indexPage(url, title, body, lastModified, size, childLinks);

        } catch (IOException e) {
            System.out.println("Failed to fetch or parse: " + url);
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
}
