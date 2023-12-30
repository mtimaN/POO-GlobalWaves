package app.results;

import lombok.Getter;

@Getter
public final class BackwardResult extends Result {
    private String message;

    public BackwardResult() {
        super();
        super.setCommand("backward");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
