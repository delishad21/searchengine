package org.crawler;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Indexer {
    private RecordManager recordManager;
    private HTree pageIndex, invertedIndex, urlToPageId, pageIdToUrl;
    private Set<String> stopwords;
    private Porter porter = new Porter();
    private int nextPageId = 0;

    public Indexer() {
        try {
            recordManager = RecordManagerFactory.createRecordManager("../../search_index");

            pageIndex = loadOrCreateHTree("pageIndex");
            invertedIndex = loadOrCreateHTree("invertedIndex");
            urlToPageId = loadOrCreateHTree("urlToPageId");
            pageIdToUrl = loadOrCreateHTree("pageIdToUrl");

            stopwords = loadStopwords("stopwords.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HTree loadOrCreateHTree(String name) throws IOException {
        long recId = recordManager.getNamedObject(name);
        if (recId != 0) {
            return HTree.load(recordManager, recId);
        } else {
            HTree tree = HTree.createInstance(recordManager);
            recordManager.setNamedObject(name, tree.getRecid());
            return tree;
        }
    }

    private Set<String> loadStopwords(String filename) {
        Set<String> stopwords = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopwords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            System.out.println("Error loading stopwords file: " + e.getMessage());
        }
        return stopwords;
    }

    public void indexPage(String url, String title, String body, String lastModified, int size,
            List<String> childPageUrls) {
        try {
            Map<String, Integer> keywords = processKeywords(title + " " + body);
            title = processTitle(title);
            String metadata = lastModified + ", " + size + " bytes";

            int pageId = getPageId(url);

            List<Integer> childPageIds = new ArrayList<>();
            for (String childUrl : childPageUrls) {
                int childPageId = getPageId(childUrl);
                childPageIds.add(childPageId);
            }

            PageData pageData = new PageData(title, metadata, keywords, childPageIds, new ArrayList<>());
            pageIndex.put(pageId, pageData);

            for (String word : keywords.keySet()) {
                @SuppressWarnings("unchecked")
                List<Integer> pages = (List<Integer>) invertedIndex.get(word);
                if (pages == null)
                    pages = new ArrayList<>();
                if (!pages.contains(pageId)) {
                    pages.add(pageId);
                }
                invertedIndex.put(word, pages);
            }

            for (int childPageId : childPageIds) {
                PageData childPage = (PageData) pageIndex.get(childPageId);
                if (childPage == null) {
                    childPage = new PageData("", "", new HashMap<>(), new ArrayList<>(), new ArrayList<>());
                }
                childPage.addParentLink(pageId);
                pageIndex.put(childPageId, childPage);
            }
            recordManager.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Integer> processKeywords(String text) {
        Map<String, Integer> freqMap = new HashMap<>();
        String[] words = text.toLowerCase().split("\\W+");

        for (String word : words) {
            if (!stopwords.contains(word)) { // Ignore stopwords
                word = porter.stripAffixes(word);
                if (!word.equals("")) {
                    freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
                }
            }
        }

        return freqMap; // No limit, stores all keywords
    }

    private String processTitle(String text) {
        StringBuilder titleBuilder = new StringBuilder();
        String[] words = text.toLowerCase().split("\\W+");

        for (String word : words) {
            if (!stopwords.contains(word)) { // Ignore stopwords
                word = porter.stripAffixes(word); // Apply Porter stemming
                if (!word.equals("")) {
                    titleBuilder.append(word).append(" "); // Append the processed word
                }
            }
        }

        // Remove the trailing space before returning the result
        return titleBuilder.toString().trim();
    }

    private int getPageId(String url) {
        try {
            Integer pageId = (Integer) urlToPageId.get(url);

            if (pageId == null) {
                pageId = nextPageId;
                urlToPageId.put(url, pageId);
                pageIdToUrl.put(pageId, url);
                recordManager.commit();
                nextPageId++;
                return pageId;
            }
            return pageId;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void close() {
        try {
            recordManager.commit();
            recordManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
