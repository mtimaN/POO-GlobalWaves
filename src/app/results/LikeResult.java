package app.results;

import lombok.Getter;

@Getter
public final class LikeResult extends Result {
    private String message;

    public LikeResult() {
        super();
        super.setCommand("like");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
