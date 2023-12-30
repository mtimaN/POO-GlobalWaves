package app.results;

import lombok.Getter;
import app.player.Status;

@Getter
public final class StatusResult extends Result {
    private Status stats;

    public StatusResult() {
        super();
        super.setCommand("status");
    }

    public void setStats(final Status stats) {
        this.stats = stats;
    }
}
