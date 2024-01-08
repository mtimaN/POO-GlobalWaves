package app.audio;

import app.persons.Listener;
import app.persons.User;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;
import main.Command;
import app.player.AudioPlayer;
import app.player.Filter;
import app.player.Status;

import java.util.ArrayList;

@Getter @Setter
public final class Song extends AudioFile implements AudioItem {
    private String album;
    private ArrayList<String> tags;
    private String lyrics;
    private String genre;
    private int releaseYear;
    private String artist;
    private int likes;

    public Song(final SongInput input) {
        this.name = input.getName();
        this.album = input.getAlbum();
        this.duration = input.getDuration();
        this.artist = input.getArtist();
        this.genre = input.getGenre();
        this.lyrics = input.getLyrics();
        this.tags = input.getTags();
        this.releaseYear = input.getReleaseYear();
        likes = 0;
    }

    /**
     * @param filters given filters
     * @return true if all filters are met, false otherwise
     */
    public boolean matchesFilter(final Filter filters) {
        if (filters.getName() != null
                && !this.getName().toLowerCase().startsWith(filters.getName().toLowerCase())) {
            return false;
        }
        if (filters.getAlbum() != null && !this.getAlbum().equals(filters.getAlbum())) {
            return false;
        }
        if (filters.getArtist() != null && !this.getArtist().equals(filters.getArtist())) {
            return false;
        }
        if (filters.getGenre() != null && !this.getGenre().equalsIgnoreCase(filters.getGenre())) {
            return false;
        }
        if (filters.getTags() != null && !this.getTags().containsAll(filters.getTags())) {
            return false;
        }
        if (filters.getLyrics() != null
                && !this.getLyrics().toLowerCase().contains(filters.getLyrics().toLowerCase())) {
            return false;
        }

        if (filters.getReleaseYear() != null) {
            String stringReleaseYear = filters.getReleaseYear();
            int numericYear;

            if (stringReleaseYear.startsWith(">")) {
                String numericPart = stringReleaseYear.substring(1);
                numericYear = Integer.parseInt(numericPart);
                return this.getReleaseYear() > numericYear;
            } else if (stringReleaseYear.startsWith("<")) {
                String numericPart = stringReleaseYear.substring(1);
                numericYear = Integer.parseInt(numericPart);
                return this.getReleaseYear() < numericYear;
            } else {
                numericYear = Integer.parseInt(stringReleaseYear);
                return this.getReleaseYear() == numericYear;
            }
        }
        return true;
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    /**
     * simulates playing the song
     * @param player the current player
     * @param command the given command
     * @return the song if it is still playing, null otherwise
     */
    public Song play(final AudioPlayer player, final Command command) {
        int time = player.getElapsedTime();

        Status status = player.getStatus();
        Listener listener = (Listener) player.getUser();

        if (status.getName().equals(name) && !name.equals("Ad Break")) {
            listener.addToSongListens(this, -1);
        }

        if (player.isAdBreakNext() && time >= getDuration()) {
            player.setElapsedTime(player.getElapsedTime() - getDuration());
            listener.addToSongListens(this, 1);
            player.setAdBreakMemento(new AdBreakMemento(status, player.getCurrentItem()));
            player.setStatus(new Status());
            player.getStatus().empty();
            player.getStatus().setPaused(false);
            player.getStatus().setName("Ad Break");
            player.setAdBreakNext(false);
            player.setCurrentItem(LibrarySingleton.getInstance().findSongByName("Ad Break"));
            return (Song)player.updateStatus(command);
        }

        if (getName().equals("Ad Break")) {
            listener.splitMoney();
            if (time >= getDuration()) {
                player.getAdBreakMemento().revert(player);
                player.setElapsedTime(player.getElapsedTime() - getDuration());
                return (Song)player.updateStatus(command);
            } else {
                time -= getDuration();
                status.setRemainedTime(-time);
                return this;
            }
        }

        if (player.getStatus().getRepeat().equals("Repeat Once")) {
            time -= getDuration();
            listener.addToSongListens(this, 1);
            if (time < 0) {
                status.setRemainedTime(-time);
                status.setName(getName());
                return this;
            } else {
                player.setElapsedTime(player.getElapsedTime() - getDuration());
                status.setRepeat("No Repeat");
            }
        }

        if (player.getStatus().getRepeat().equals("No Repeat")) {
            time -= getDuration();
            listener.addToSongListens(this, 1);
            status.setRemainedTime(-time);
        }

        if (player.getStatus().getRepeat().equals("Repeat Infinite")) {
            listener.addToSongListens(this, time / getDuration() + 1);
            time %= getDuration();
            status.setRemainedTime(getDuration() - time);
            time = -1;
        }

        status.setName(getName());
        player.setElapsedTime(player.getElapsedTime() % getDuration());
        if (time >= 0) {
            AudioItem.setNullStatus(player);
            return null;
        }
        return this;
    }

    @Override
    public boolean isOwnedBy(final User user) {
        return user.getUsername().equals(artist);
    }

    @Override
    public boolean canBeShuffled() {
        return false;
    }

    @Override
    public boolean allowForwardBackward() {
        return false;
    }

    @Override
    public boolean isSongCollection() {
        return false;
    }

    @Override
    public boolean containsSongsFrom(final Album albumSource) {
        return this.album.equals(albumSource.getName());
    }

    @Override
    public boolean savesProgress() {
        return false;
    }

    /**
     * increase like count by one
     */
    public void like() {
        likes += 1;
    }

    /**
     * decrease like count by one
     */
    public void unlike() {
        likes -= 1;
    }

    @Override
    public boolean isSong() {
        return true;
    }
}
