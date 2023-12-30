package app.results;

import lombok.Getter;

@Getter
public final class SwitchVisibilityResult extends Result {
    private String message;

    public SwitchVisibilityResult() {
        super();
        super.setCommand("switchVisibility");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
