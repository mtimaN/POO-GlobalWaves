package app.results;

import lombok.Getter;

@Getter
public final class AddRemoveInPlaylistResult extends Result {
    private String message;

    public AddRemoveInPlaylistResult() {
        super();
        super.setCommand("addRemoveInPlaylist");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
