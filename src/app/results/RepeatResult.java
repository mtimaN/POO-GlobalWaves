package app.results;

import lombok.Getter;

@Getter
public final class RepeatResult extends Result {
    private String message;

    public RepeatResult() {
        super();
        super.setCommand("repeat");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
