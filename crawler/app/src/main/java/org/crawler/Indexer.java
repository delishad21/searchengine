package org.crawler;

import java.sql.*;
import java.util.*;
import java.io.*;

public class Indexer {
    private Connection conn;
    private Set<String> stopwords;
    private Porter porter = new Porter();
    private int nextPageId = 0;

    public Indexer() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:../../search_index.db");
            createTables();
            stopwords = loadStopwords("stopwords.txt");
            nextPageId = getMaxPageId() + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS pages (id INTEGER PRIMARY KEY, original_title TEXT, stemmed_title TEXT, metadata TEXT)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS keywords (word TEXT, page_id INTEGER, frequency INTEGER)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS urls (url TEXT PRIMARY KEY, page_id INTEGER)");
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS links (parent_id INTEGER, child_id INTEGER)");
        stmt.close();
    }

    private int getMaxPageId() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM pages");
        int max = rs.next() ? rs.getInt(1) : -1;
        rs.close();
        stmt.close();
        return max;
    }

    private Set<String> loadStopwords(String filename) {
        Set<String> stopwords = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                stopwords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            System.out.println("Stopwords file missing: " + e.getMessage());
        }
        return stopwords;
    }

    public void indexPage(String url, String title, String body, String lastModified, int size,
            List<String> childPageUrls) {
        try {
            Map<String, Integer> keywords = processKeywords(title + " " + body);
            String metadata = lastModified + ", " + size + " bytes";
            int pageId = getOrCreatePageId(url);

            insertPage(pageId, title, processTitle(title), metadata);
            insertKeywords(pageId, keywords);

            List<Integer> childPageIds = new ArrayList<>();
            for (String childUrl : childPageUrls) {
                int childPageId = getOrCreatePageId(childUrl);
                insertLink(pageId, childPageId);
                childPageIds.add(childPageId);
            }

            // conn.commit(); Auto commit is already on
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getOrCreatePageId(String url) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT page_id FROM urls WHERE url = ?");
        stmt.setString(1, url);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt(1);
            rs.close();
            stmt.close();
            return id;
        }

        int pageId = nextPageId++;
        rs.close();
        stmt.close();

        PreparedStatement insert = conn.prepareStatement("INSERT INTO urls (url, page_id) VALUES (?, ?)");
        insert.setString(1, url);
        insert.setInt(2, pageId);
        insert.executeUpdate();
        insert.close();

        return pageId;
    }

    private void insertPage(int pageId, String originalTitle, String stemmedTitle, String metadata)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO pages (id, original_title, stemmed_title, metadata) VALUES (?, ?, ?, ?)");
        stmt.setInt(1, pageId);
        stmt.setString(2, originalTitle);
        stmt.setString(3, stemmedTitle);
        stmt.setString(4, metadata);
        stmt.executeUpdate();
        stmt.close();
    }

    private void insertKeywords(int pageId, Map<String, Integer> keywords) throws SQLException {
        PreparedStatement stmt = conn
                .prepareStatement("INSERT INTO keywords (word, page_id, frequency) VALUES (?, ?, ?)");

        for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
            stmt.setString(1, entry.getKey());
            stmt.setInt(2, pageId);
            stmt.setInt(3, entry.getValue());
            stmt.addBatch();
        }

        stmt.executeBatch();
        stmt.close();
    }

    private void insertLink(int parentId, int childId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO links (parent_id, child_id) VALUES (?, ?)");
        stmt.setInt(1, parentId);
        stmt.setInt(2, childId);
        stmt.executeUpdate();
        stmt.close();
    }

    private Map<String, Integer> processKeywords(String text) {
        Map<String, Integer> freqMap = new HashMap<>();
        String[] words = text.toLowerCase().split("\\W+");

        for (String word : words) {
            if (!stopwords.contains(word)) {
                word = porter.stripAffixes(word);
                if (!word.isEmpty()) {
                    freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
                }
            }
        }
        return freqMap;
    }

    private String processTitle(String text) {
        StringBuilder builder = new StringBuilder();
        String[] words = text.toLowerCase().split("\\W+");

        for (String word : words) {
            if (!stopwords.contains(word)) {
                word = porter.stripAffixes(word);
                if (!word.isEmpty()) {
                    builder.append(word).append(" ");
                }
            }
        }
        return builder.toString().trim();
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
