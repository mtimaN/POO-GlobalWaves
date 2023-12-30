package app.results;

import lombok.Getter;

@Getter
public final class NextResult extends Result {
    private String message;

    public NextResult() {
        super();
        super.setCommand("next");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
