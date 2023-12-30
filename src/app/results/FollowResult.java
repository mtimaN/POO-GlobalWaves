package app.results;

import lombok.Getter;

@Getter
public final class FollowResult extends Result {
    private String message;

    public FollowResult() {
        super();
        super.setCommand("follow");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
