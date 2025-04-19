package org.search;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {
    private RecordManager recordManager;
    private HTree pageIndex, invertedIndex, pageIdToUrl;
    private Porter porter = new Porter();
    private Set<String> stopwords;
    private static final int MAX_RESULTS = 50;

    public SearchEngine() throws IOException {
        recordManager = RecordManagerFactory.createRecordManager("../../search_index");

        pageIndex = HTree.load(recordManager, recordManager.getNamedObject("pageIndex"));
        invertedIndex = HTree.load(recordManager, recordManager.getNamedObject("invertedIndex"));
        pageIdToUrl = HTree.load(recordManager, recordManager.getNamedObject("pageIdToUrl"));
        stopwords = loadStopwords("stopwords.txt");
    }

    private Set<String> loadStopwords(String filename) {
        Set<String> stopwords = new HashSet<>();
        try (Scanner scanner = new Scanner(new java.io.File(filename))) {
            while (scanner.hasNextLine()) {
                stopwords.add(scanner.nextLine().trim().toLowerCase());
            }
        } catch (IOException e) {
            System.out.println("Could not load stopwords.");
        }
        return stopwords;
    }

    public List<SearchResult> search(String query) throws IOException {
        Map<Integer, Double> scores = new HashMap<>();
        Map<Integer, Integer> maxTf = new HashMap<>();
        Map<String, Integer> dfMap = new HashMap<>();
        Set<String> terms = new HashSet<>();
        Map<Integer, PageData> docMap = new HashMap<>();

        List<String> phrases = extractPhrases(query);
        List<String> words = tokenizeAndStem(query.replaceAll("\".*?\"", "")); // remove phrases

        terms.addAll(words);

        for (String term : terms) {
            @SuppressWarnings("unchecked")
            List<Integer> docIds = (List<Integer>) invertedIndex.get(term);
            if (docIds == null)
                continue;

            int pageIndexSize = 0;
            try {
                jdbm.helper.FastIterator keys = pageIndex.keys();
                while (keys.next() != null) {
                    pageIndexSize++;
                }
            } catch (IOException e) {
                System.out.println("Error calculating pageIndex size: " + e.getMessage());
            }
            double idf = Math.log((double) pageIndexSize / (1 + docIds.size()));
            for (int docId : docIds) {
                PageData pd = (PageData) pageIndex.get(docId);
                docMap.put(docId, pd);
                int tf = pd.getKeywords().getOrDefault(term, 0);
                maxTf.put(docId, Math.max(maxTf.getOrDefault(docId, 0), tf));
            }

            for (int docId : docIds) {
                double tf = (double) docMap.get(docId).getKeywords().get(term);
                double score = (tf / maxTf.get(docId)) * idf;

                if (docMap.get(docId).getTitle().contains(term)) {
                    score *= 1.5; // boost for title match
                }

                scores.put(docId, scores.getOrDefault(docId, 0.0) + score);
            }
        }

        // Phrase matching: give additional boost if phrase appears in title/body
        for (String phrase : phrases) {
            String[] phraseTerms = tokenizeAndStem(phrase).toArray(new String[0]);
            for (Map.Entry<Integer, PageData> entry : docMap.entrySet()) {
                int docId = entry.getKey();
                PageData pd = entry.getValue();
                if (matchesPhrase(pd.getTitle(), phraseTerms)
                        || matchesPhrase(String.join(" ", pd.getKeywords().keySet()), phraseTerms)) {
                    scores.put(docId, scores.getOrDefault(docId, 0.0) + 2.0); // boost
                }
            }
        }

        // Rank and return top 50
        PriorityQueue<SearchResult> pq = new PriorityQueue<>(
                Comparator.comparingDouble(SearchResult::getScore).reversed());
        for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
            int docId = entry.getKey();
            double score = entry.getValue();
            PageData pd = docMap.get(docId);
            String url = (String) pageIdToUrl.get(docId);

            pq.add(new SearchResult(score, pd.getTitle(), url, pd.getMetadata(), pd.getKeywords(), pd.getParentLinks(),
                    pd.getChildLinks()));
        }

        List<SearchResult> results = new ArrayList<>();
        int count = 0;
        while (!pq.isEmpty() && count < MAX_RESULTS) {
            results.add(pq.poll());
            count++;
        }

        return results;
    }

    private boolean matchesPhrase(String text, String[] stemmedPhraseTerms) {
        String[] tokens = text.toLowerCase().split("\\W+");
        for (int i = 0; i <= tokens.length - stemmedPhraseTerms.length; i++) {
            boolean match = true;
            for (int j = 0; j < stemmedPhraseTerms.length; j++) {
                String stemmedToken = porter.stripAffixes(tokens[i + j]);
                if (!stemmedToken.equals(stemmedPhraseTerms[j])) {
                    match = false;
                    break;
                }
            }
            if (match)
                return true;
        }
        return false;
    }

    private List<String> extractPhrases(String query) {
        List<String> phrases = new ArrayList<>();
        Matcher m = Pattern.compile("\"(.*?)\"").matcher(query);
        while (m.find()) {
            phrases.add(m.group(1));
        }
        return phrases;
    }

    private List<String> tokenizeAndStem(String query) {
        List<String> tokens = new ArrayList<>();
        for (String word : query.toLowerCase().split("\\W+")) {
            if (!stopwords.contains(word)) {
                String stemmed = porter.stripAffixes(word);
                if (!stemmed.isEmpty()) {
                    tokens.add(stemmed);
                }
            }
        }
        return tokens;
    }
}
