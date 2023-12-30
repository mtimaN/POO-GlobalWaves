package app.results;

import lombok.Getter;

@Getter
public final class ForwardResult extends Result {
    private String message;

    public ForwardResult() {
        super();
        super.setCommand("forward");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
