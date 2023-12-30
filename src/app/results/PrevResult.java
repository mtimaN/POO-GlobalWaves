package app.results;

import lombok.Getter;

@Getter
public final class PrevResult extends Result {
    private String message;

    public PrevResult() {
        super();
        super.setCommand("prev");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
