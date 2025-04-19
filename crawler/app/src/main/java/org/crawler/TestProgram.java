package org.crawler;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class TestProgram {
    public static void main(String[] args) {
        new TestProgram().generateReport();
    }

    public void generateReport() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:../../search_index.db")) {

            StringBuilder output = new StringBuilder();

            // Query for pages
            String pageQuery = "SELECT id, original_title, metadata FROM pages WHERE original_title != ''";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(pageQuery)) {
                while (rs.next()) {
                    int pageId = rs.getInt("id");
                    String title = rs.getString("original_title");
                    String metadata = rs.getString("metadata");

                    output.append(title).append("\n");

                    // Query URL by pageId
                    String urlQuery = "SELECT url FROM urls WHERE page_id = ?";
                    try (PreparedStatement psUrl = conn.prepareStatement(urlQuery)) {
                        psUrl.setInt(1, pageId);
                        try (ResultSet urlRs = psUrl.executeQuery()) {
                            if (urlRs.next()) {
                                String url = urlRs.getString("url");
                                output.append(url).append("\n");
                            }
                        }
                    }

                    output.append(metadata).append("\n");

                    // Write keywords
                    String keywordsQuery = "SELECT word, frequency FROM keywords WHERE page_id = ?";
                    try (PreparedStatement psKeywords = conn.prepareStatement(keywordsQuery)) {
                        psKeywords.setInt(1, pageId);
                        try (ResultSet keywordsRs = psKeywords.executeQuery()) {
                            int count = 0;
                            while (keywordsRs.next() && count < 10) {
                                String keyword = keywordsRs.getString("word");
                                int frequency = keywordsRs.getInt("frequency");
                                output.append(keyword).append(" ").append(frequency)
                                        .append("; ");
                                count++;
                            }
                        }
                    }
                    output.append("\n");

                    // Write child links
                    String childLinksQuery = "SELECT child_id FROM links WHERE parent_id = ?";
                    try (PreparedStatement psChildLinks = conn.prepareStatement(childLinksQuery)) {
                        psChildLinks.setInt(1, pageId);
                        try (ResultSet childLinksRs = psChildLinks.executeQuery()) {
                            int childLinkCount = 0;
                            while (childLinksRs.next() && childLinkCount < 10) {
                                int childPageId = childLinksRs.getInt("child_id");

                                // Query URL for childPageId
                                String childUrlQuery = "SELECT url FROM urls WHERE page_id = ?";
                                try (PreparedStatement psChildUrl = conn.prepareStatement(childUrlQuery)) {
                                    psChildUrl.setInt(1, childPageId);
                                    try (ResultSet childUrlRs = psChildUrl.executeQuery()) {
                                        if (childUrlRs.next()) {
                                            String childUrl = childUrlRs.getString("url");
                                            output.append("").append(childUrl).append("\n");
                                            childLinkCount++;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    output.append("-------------------------------\n");
                }
            }

            // Write output to file
            try (FileWriter writer = new FileWriter("../../spider_result.txt")) {
                writer.write(output.toString());
            }

            System.out.println("Report generated: spider_result.txt");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
