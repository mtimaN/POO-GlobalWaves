package app.results;

import lombok.Getter;

@Getter
public final class PlayPauseResult extends Result {
    private String message;
    public PlayPauseResult() {
        super();
        super.setCommand("playPause");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
