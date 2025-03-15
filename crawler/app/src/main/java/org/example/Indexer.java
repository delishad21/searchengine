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
    private HTree pageIndex, invertedIndex;
    private Set<String> stopwords;
    private Porter porter = new Porter();

    public Indexer() {
        try {
            recordManager = RecordManagerFactory.createRecordManager("search_index");

            pageIndex = loadOrCreateHTree("pageIndex");
            invertedIndex = loadOrCreateHTree("invertedIndex");

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
            List<String> childLinks) {
        try {
            Map<String, Integer> keywords = processKeywords(body);
            String metadata = lastModified + ", " + size + " bytes";

            PageData pageData = new PageData(title, metadata, keywords, childLinks, new ArrayList<>());
            pageIndex.put(url, pageData);

            // Update inverted index
            for (String word : keywords.keySet()) {
                List<String> pages = (List<String>) invertedIndex.get(word);
                if (pages == null)
                    pages = new ArrayList<>();
                if (!pages.contains(url)) {
                    pages.add(url);
                }
                invertedIndex.put(word, pages);
            }

            // Update parent links for all child pages
            for (String childUrl : childLinks) {
                PageData childPage = (PageData) pageIndex.get(childUrl);
                if (childPage == null) {
                    childPage = new PageData("", "", new HashMap<>(), new ArrayList<>(), new ArrayList<>());
                }
                childPage.addParentLink(url);
                pageIndex.put(childUrl, childPage);
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

        return freqMap; // No limit, stores all keywords
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
