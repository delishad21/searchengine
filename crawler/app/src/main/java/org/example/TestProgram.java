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
            HTree pageIndex = loadHTree(recordManager, "pageIndex");

            if (pageIndex == null) {
                System.out.println("Error: HTree structure failed to load.");
                return;
            }

            StringBuilder output = new StringBuilder();
            FastIterator keysIterator = pageIndex.keys();
            String url;

            while ((url = (String) keysIterator.next()) != null) {
                PageData pageData = (PageData) pageIndex.get(url);

                if (pageData.getTitle().equals("")) {
                    continue;
                }
                output.append(pageData.getTitle()).append("\n");
                output.append(url).append("\n");
                output.append(pageData.getMetadata()).append("\n");

                // Write keywords
                int count = 0;
                for (Map.Entry<String, Integer> entry : pageData.getKeywords().entrySet()) {
                    if (count >= 10) break;
                    output.append(entry.getKey()).append(" ").append(entry.getValue()).append("; ");
                    count++;
                }
                output.append("\n");

                // Write child links
                int childLinkCount = 0;
                for (String link : pageData.getChildLinks()) {
                    if (childLinkCount < 10) {
                        output.append(link).append("\n");
                        childLinkCount++;
                    } else {
                        break;
                    }
                }

                output.append("-------------------------------\n");
            }

            try (FileWriter writer = new FileWriter("spider_result.txt")) {
                writer.write(output.toString());
            }

            System.out.println("Report generated: spider_result.txt");
            recordManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HTree loadHTree(RecordManager recordManager, String name) throws IOException {
        long recId = recordManager.getNamedObject(name);
        return recId == 0 ? null : HTree.load(recordManager, recId);
    }
}
