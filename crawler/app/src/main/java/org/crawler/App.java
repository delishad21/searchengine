/*
 * This source file was generated by the Gradle 'init' task
 */
package org.crawler;

public class App {
    public static void main(String[] args) {
        String startUrl = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm";
        int maxPages = 300; // As per final requirement

        WebCrawler crawler = new WebCrawler(maxPages);
        crawler.startCrawling(startUrl);

        // Close indexer to save data
        Indexer indexer = new Indexer();
        indexer.close();

        // Generate report
        TestProgram.main(args);

    }
}
