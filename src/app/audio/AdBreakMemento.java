package app.audio;

import app.player.AudioPlayer;
import app.player.Status;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdBreakMemento {
    private Status status;
    private AudioItem currentItem;

    public AdBreakMemento(Status status, AudioItem currentItem) {
        this.status = status;
        this.currentItem = currentItem;
    }

    public void revert(AudioPlayer player) {
        player.setStatus(status);
        player.setCurrentItem(currentItem);
    }
}
