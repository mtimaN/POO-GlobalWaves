package app.audio;

import app.player.AudioPlayer;
import app.player.Status;
import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@Getter @Setter
public abstract class SongCollection implements AudioItem {
    protected String name;
    protected String owner;
    protected ArrayList<Song> songs;

    /**
     * updates the player metadata, as if playing the actual songs.
     * @param player the current player
     * @param command the given command
     * @return the current song played
     */
    public Song play(final AudioPlayer player, final Command command) {
        int time = player.getElapsedTime();
        Status status = player.getStatus();
        ArrayList<Integer> positions = new ArrayList<>();
        int id = player.getTrackId();
        for (int i = 0; i < songs.size(); ++i) {
            positions.add(i, i);
        }
        if (status.isShuffle()) {
            Collections.shuffle(positions, new Random(player.getSeed()));
            for (int i = 0; i < positions.size(); ++i) {
                if (positions.get(i) == id) {
                    id = i;
                    break;
                }
            }
        }
        switch (status.getRepeat()) {
            case "Repeat All" -> {
                while (time >= 0) {
                    for (int i = 0; i < songs.size(); ++i) {
                        int position = positions.get((i + id) % songs.size());
                        if (time >= songs.get(position).getDuration()) {
                            time -= songs.get(position).getDuration();
                        } else {
                            player.setElapsedTime(time);
                            status.setName(songs.get(position).getName());
                            status.setRemainedTime(songs.get(position).getDuration() - time);
                            player.setTrackId(position);
                            return songs.get(position);
                        }
                    }
                }
            }
            case "No Repeat" -> {
                for (int i = 0; i < songs.size(); ++i) {
                    if (i + id >= songs.size()) {
                        time = 1;
                        break;
                    }
                    int position = positions.get(i + id);
                    if (time >= songs.get(position).getDuration()) {
                        time -= songs.get(position).getDuration();
                    } else {
                        player.setElapsedTime(time);
                        status.setName(songs.get(position).getName());
                        status.setRemainedTime(songs.get(position).getDuration() - time);
                        player.setTrackId(position);
                        return songs.get(position);
                    }
                }
            }
            case "Repeat Current Song" -> {
                Song currentSong = (Song) player.getCurrentFile();
                time %= currentSong.getDuration();
                player.setElapsedTime(time);
                status.setRemainedTime(currentSong.getDuration() - time);
                status.setName(currentSong.getName());
                return (Song) player.getCurrentFile();
            }
            default -> {
                return null;
            }
        }
        if (time >= 0) {
            AudioItem.setNullStatus(player);
        }
        return null;
    }

    /**
     * @return true if the songs can be shuffled
     */
    @Override
    public boolean canBeShuffled() {
        return true;
    }

    /**
     * @return true if it allows forward and backward
     */
    @Override
    public boolean allowForwardBackward() {
        return false;
    }

    /**
     * @return true, always
     */
    @Override
    public boolean isSongCollection() {
        return true;
    }

    /**
     * @return true if the collection should save listening progress
     */
    @Override
    public boolean savesProgress() {
        return false;
    }
    /**
     * @param album a given album
     * @return true if the collection  contains songs from the album
     */
    public boolean containsSongsFrom(final Album album) {
        for (Song song: songs) {
            if (song.getAlbum().equals(album.getName())) {
                return true;
            }
        }
        return false;
    }
}
