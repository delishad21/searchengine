package org.example;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TestProgram {
    public static void main(String[] args) {
        new TestProgram().generateReport();
    }

    public void generateReport() {
        try {
            RecordManager recordManager = RecordManagerFactory.createRecordManager("search_index");
            HTree pageTitles = loadHTree(recordManager, "pageTitles");
            HTree pageMetadata = loadHTree(recordManager, "pageMetadata");
            HTree pageKeywords = loadHTree(recordManager, "pageKeywords");
            HTree pageLinks = loadHTree(recordManager, "pageLinks");

            if (pageTitles == null || pageMetadata == null || pageKeywords == null || pageLinks == null) {
                System.out.println("Error: Some HTree structures failed to load.");
                return;
            }

            StringBuilder output = new StringBuilder();

            FastIterator keysIterator = pageTitles.keys();
            List<String> keys = new ArrayList<>();
            String key;
            while ((key = (String) keysIterator.next()) != null) {
                keys.add(key);
            }

            for (String url : keys) {
                String title = (String) pageTitles.get(url);
                String metadata = (String) pageMetadata.get(url);
                Map<String, Integer> keywords = (Map<String, Integer>) pageKeywords.get(url);
                List<String> childLinks = (List<String>) pageLinks.get(url);

                output.append(title).append("\n");
                output.append(url).append("\n");
                output.append(metadata).append("\n");

                // Write keywords
                if (keywords != null) {
                    for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
                        output.append(entry.getKey()).append(" ").append(entry.getValue()).append("; ");
                    }
                }
                output.append("\n");

                // Write child links (limit to 10)
                if (childLinks != null) {
                    for (String link : childLinks) {
                        output.append(link).append("\n");
                    }
                }

                output.append("-------------------------------\n");
            }

            if (output.length() > 0) {
                try (FileWriter writer = new FileWriter("spider_result.txt")) {
                    writer.write(output.toString());
                }
                System.out.println("Report generated: spider_result.txt");
            } else {
                System.out.println("No data to write.");
            }

            recordManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HTree loadHTree(RecordManager recordManager, String name) throws IOException {
        long recId = recordManager.getNamedObject(name);
        if (recId == 0) {
            System.out.println("Warning: No data found for '" + name + "'. It may not have been indexed.");
            return null;
        }
        return HTree.load(recordManager, recId);
    }
}
