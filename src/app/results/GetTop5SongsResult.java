package app.results;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class GetTop5SongsResult {
    private ArrayList<String> result;
    private String command;
    private int timestamp;

    public GetTop5SongsResult() {
        result = new ArrayList<>();
        command = "getTop5Songs";
    }

    public void setResult(final ArrayList<String> result) {
        this.result = result;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public void setTimestamp(final int timestamp) {
        this.timestamp = timestamp;
    }
}
