package app.results;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class ShowPlaylistsResult extends Result {
    private ArrayList<PlaylistOutput> result;

    public ShowPlaylistsResult() {
        super();
        result = new ArrayList<>();
        setCommand("showPlaylists");
    }

    public void setResult(final ArrayList<PlaylistOutput> result) {
        this.result = result;
    }
}
