package app.player;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Status {
    private String name;
    private int remainedTime;
    private String repeat;
    private boolean shuffle;
    private boolean paused;

    public void empty() {
        name = "";
        paused = true;
        remainedTime = 0;
        repeat = "No Repeat";
        shuffle = false;
    }
}
