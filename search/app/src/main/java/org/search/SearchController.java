package org.search;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final SearchEngine searchEngine;

    public SearchController() throws IOException {
        this.searchEngine = new SearchEngine();
    }

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam String query) {
        try {
            return searchEngine.search(query);
        } catch (IOException e) {
            throw new SearchException("Search failed due to an error: " + e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SearchException.class)
    public String handleSearchException(SearchException e) {
        return e.getMessage();
    }

    static class SearchException extends RuntimeException {
        public SearchException(String message) {
            super(message);
        }
    }
}
