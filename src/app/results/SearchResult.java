package app.results;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class SearchResult extends Result {
    private String message;
    private ArrayList<String> results;

    public SearchResult() {
        super();
        super.setCommand("search");
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setResults(final ArrayList<String> results) {
        this.results = results;
    }
}
