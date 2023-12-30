package app.results;

import lombok.Getter;

@Getter
public final class ShuffleResult extends Result {
    private String message;

    public ShuffleResult() {
        super();
        super.setCommand("shuffle");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
