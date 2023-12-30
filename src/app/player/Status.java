package app.player;

import lombok.Getter;

@Getter
public final class Status {
    private String name;
    private int remainedTime;
    private String repeat;
    private boolean shuffle;
    private boolean paused;

    public void setName(final String name) {
        this.name = name;
    }

    public void setRemainedTime(final int remainedTime) {
        this.remainedTime = remainedTime;
    }

    public void setRepeat(final String repeat) {
        this.repeat = repeat;
    }

    public void setShuffle(final boolean shuffle) {
        this.shuffle = shuffle;
    }

    public void setPaused(final boolean paused) {
        this.paused = paused;
    }
}
