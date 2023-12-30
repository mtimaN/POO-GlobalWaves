package app.results;


import lombok.Getter;

@Getter
public final class SelectResult extends Result {
    private String message;

    public SelectResult() {
        super();
        super.setCommand("select");
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
