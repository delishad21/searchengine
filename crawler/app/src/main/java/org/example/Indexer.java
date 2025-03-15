package org.example;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Indexer {
    private RecordManager recordManager;
    private HTree pageTitles, pageMetadata, invertedIndex, pageLinks, pageKeywords, parentLinks;
    private Set<String> stopwords; // Store stopwords here
    private Porter porter = new Porter();

    public Indexer() {
        try {
            recordManager = RecordManagerFactory.createRecordManager("search_index");

            pageTitles = loadOrCreateHTree("pageTitles");
            pageMetadata = loadOrCreateHTree("pageMetadata");
            invertedIndex = loadOrCreateHTree("invertedIndex");
            pageLinks = loadOrCreateHTree("pageLinks");
            pageKeywords = loadOrCreateHTree("pageKeywords");
            parentLinks = loadOrCreateHTree("parentLinks");

            stopwords = loadStopwords("stopwords.txt"); // Load stopwords

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
            List<String> childLinks) {
        try {
            pageTitles.put(url, title);
            pageMetadata.put(url, lastModified + ", " + size + " bytes");
            pageLinks.put(url, childLinks);

            Map<String, Integer> keywords = processKeywords(body);
            pageKeywords.put(url, keywords);

            for (String word : keywords.keySet()) {
                List<String> pages = (List<String>) invertedIndex.get(word);
                if (pages == null)
                    pages = new ArrayList<>();
                pages.add(url);
                invertedIndex.put(word, pages);
            }

            // Update parent links for all child pages
            for (String childUrl : childLinks) {
                List<String> parents = (List<String>) parentLinks.get(childUrl);
                if (parents == null)
                    parents = new ArrayList<>();
                parents.add(url);
                parentLinks.put(childUrl, parents);
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
                freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
            }
        }

        return freqMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10)
                .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
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
