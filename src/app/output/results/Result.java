package app.output.results;

import lombok.Getter;

@Getter
public abstract class Result {
    private String command;
    private String user;
    private int timestamp;

    public final void setCommand(final String command) {
        this.command = command;
    }

    public final void setUser(final String user) {
        this.user = user;
    }

    public final void setTimestamp(final int timestamp) {
        this.timestamp = timestamp;
    }
}
