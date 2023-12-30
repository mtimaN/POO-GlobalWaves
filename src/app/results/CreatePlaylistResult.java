package app.results;

import lombok.Getter;

@Getter
public final class CreatePlaylistResult extends Result {
    private String message;

    public CreatePlaylistResult() {
        super();
        super.setCommand("createPlaylist");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
