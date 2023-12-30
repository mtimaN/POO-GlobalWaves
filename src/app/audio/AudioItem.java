package app.audio;

import app.persons.User;
import app.player.Searchable;
import main.Command;
import app.player.AudioPlayer;
import app.player.Status;

public interface AudioItem extends Searchable {
    /**
     * @return name as a string
     */
    String getName();

    /**
     * updates information regarding play status
     * @param player the current app.player
     * @param command the given command
     * @return the current file playing, or null if none
     */
    AudioFile play(AudioPlayer player, Command command);

    /**
     * sets status to null - no audio playing.
     * @param player the current player
     */
    static void setNullStatus(AudioPlayer player) {
        Status status = player.getStatus();
        status.setName("");
        status.setRemainedTime(0);
        status.setRepeat("No Repeat");
        status.setPaused(true);
        status.setShuffle(false);
        player.setCurrentItem(null);
    }

    /**
     * @param user any user
     * @return true if the item is owned by the given user, false otherwise
     */
    boolean isOwnedBy(User user);

    /**
     * @return true if the item can be shuffled, false otherwise
     */
    boolean canBeShuffled();

    /**
     * @return true if it should allow forward and backward
     */
    boolean allowForwardBackward();

    /**
     * @return true if the item is a song colleciton
     */
    boolean isSongCollection();

    /**
     * @param album a given album
     * @return true if the item contains songs from the album
     */
    boolean containsSongsFrom(Album album);

    /**
     * @return true if should save listening progress
     */
    boolean savesProgress();
}
