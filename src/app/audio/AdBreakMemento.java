package app.audio;

import app.player.AudioPlayer;
import app.player.Status;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class AdBreakMemento {
    private Status status;
    private AudioItem currentItem;

    public AdBreakMemento(final Status status, final AudioItem currentItem) {
        this.status = status;
        this.currentItem = currentItem;
    }

    /**
     * revert the player to the initial state using memento
     * @param player the given player
     */
    public void revert(final AudioPlayer player) {
        player.setStatus(status);
        player.setCurrentItem(currentItem);
    }
}
