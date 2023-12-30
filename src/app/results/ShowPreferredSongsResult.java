package app.results;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class ShowPreferredSongsResult extends Result {
    private ArrayList<String> result;

    public ShowPreferredSongsResult() {
        super();
        result = new ArrayList<>();
        super.setCommand("showPreferredSongs");
    }

    public void setResult(final ArrayList<String> result) {
        this.result = result;
    }
}
