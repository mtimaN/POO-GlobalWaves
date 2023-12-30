package app.results;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class GetTop5PlaylistsResult {
    private ArrayList<String> result;
    private String command;
    private int timestamp;

    public GetTop5PlaylistsResult() {
        result = new ArrayList<>();
        command = "getTop5Playlists";
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
