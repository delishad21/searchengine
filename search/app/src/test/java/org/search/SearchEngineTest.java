import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.List;
import org.search.SearchResult;
import org.search.SearchEngine;

public class SearchEngineTest {
    private SearchEngine searchEngine;

    @BeforeEach
    public void setUp() throws IOException {
        // Assuming that SearchEngine is constructed properly and all its dependencies
        // are loaded
        searchEngine = new SearchEngine();
    }

    @Test
    public void testSearch_singleTerm() throws IOException {
        String query = "data";
        List<SearchResult> results = searchEngine.search(query);

        // Assuming we expect at least one result for the "data" term
        assertNotNull(results);
        assertTrue(results.size() > 0);

        // Check the first result's title or URL to see if it contains the term "data"
        assertTrue(results.get(0).getTitle().toLowerCase().contains("data"));
    }

    @Test
    public void testSearch_multipleTerms() throws IOException {
        String query = "data science";
        List<SearchResult> results = searchEngine.search(query);

        // We expect some results for the query "data science"
        assertNotNull(results);
        assertTrue(results.size() > 0);

        // Check that both terms appear in the title or body (stemmed form)
        for (SearchResult result : results) {
            assertTrue(result.getTitle().toLowerCase().contains("data") ||
                    result.getTitle().toLowerCase().contains("science"));
        }
    }

    @Test
    public void testSearch_withStopwords() throws IOException {
        String query = "the quick fox";
        List<SearchResult> results = searchEngine.search(query);

        // Ensure "the" is removed from the query as it is a stopword
        assertNotNull(results);
        assertTrue(results.size() > 0);

        // Check that the result contains "quick" and "fox"
        for (SearchResult result : results) {
            assertTrue(result.getTitle().toLowerCase().contains("quick"));
            assertTrue(result.getTitle().toLowerCase().contains("fox"));
        }
    }

    @Test
    public void testSearch_phraseQuery() throws IOException {
        String query = "\"artificial intelligence\"";
        List<SearchResult> results = searchEngine.search(query);

        // Ensure that the phrase "artificial intelligence" is matched in the title or
        // body
        assertNotNull(results);
        assertTrue(results.size() > 0);

        boolean foundPhrase = false;
        for (SearchResult result : results) {
            String title = result.getTitle().toLowerCase();
            if (title.contains("artificial") && title.contains("intelligence")) {
                foundPhrase = true;
                break;
            }
        }
        assertTrue(foundPhrase);
    }

    @Test
    public void testSearch_noResults() throws IOException {
        String query = "nonexistentterm12345";
        List<SearchResult> results = searchEngine.search(query);

        // We expect no results for a non-existent term
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testSearch_resultsRanking() throws IOException {
        String query = "data science";
        List<SearchResult> results = searchEngine.search(query);

        // Assuming that the ranking system prioritizes relevance,
        // we will assert that the first result has the highest score (highest
        // relevance).
        assertNotNull(results);
        assertTrue(results.size() > 0);

        double firstScore = results.get(0).getScore();
        double secondScore = results.get(1).getScore();

        // Check if the first result has a higher score than the second one (ranking
        // logic)
        assertTrue(firstScore >= secondScore);
    }
}
