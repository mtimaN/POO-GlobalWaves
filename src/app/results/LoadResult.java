package app.results;

import lombok.Getter;

@Getter
public final class LoadResult extends Result {
    private String message;

    public LoadResult() {
        super();
        super.setCommand("load");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
