package app.audio;

import app.persons.User;
import app.output.results.GeneralResult;
import lombok.Getter;
import lombok.Setter;
import main.Command;
import app.player.Filter;
import app.persons.Listener;

import java.util.ArrayList;

@Getter @Setter
public final class Playlist extends SongCollection {
    private ArrayList<Listener> followers;
    private String visibility;
    private final int timestamp;

    public Playlist(final String name, final ArrayList<Song> songs) {
        this.name = name;
        timestamp = -1;
        this.songs = songs;
        visibility = "private";
    }
    private Playlist(final Command command) {
        this.name = command.getPlaylistName();
        songs = new ArrayList<>();
        owner = command.getUsername();
        timestamp = command.getTimestamp();
        visibility = "public";
        followers = new ArrayList<>();
        LibrarySingleton library = LibrarySingleton.getInstance();
        Listener listener = (Listener) library.findListenerByUsername(command.getUsername());
        if (listener != null && listener.getPlaylists() != null) {
            listener.getPlaylists().add(this);
        }
        library.getPlaylists().add(this);
    }

    /**
     * creates a new playlist
     * @param command given command
     * @return result formatted for output
     */
    public static GeneralResult create(final Command command) {
        if (command.getPlaylistName() == null) {
            return null;
        }

        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        LibrarySingleton library = LibrarySingleton.getInstance();
        Listener listener = library.findListenerByUsername(command.getUsername());
        if (listener == null) {
            result.setMessage("User doesn't exist.");
            return result;
        }
        ArrayList<Playlist> playlists = listener.getPlaylists();
        for (Playlist playlist: playlists) {
            if (playlist.getName().equals(command.getPlaylistName())) {
                result.setMessage("A playlist with the same name already exists.");
                return result;
            }
        }
        new Playlist(command);
        result.setMessage("Playlist created successfully.");
        return result;
    }
    @Override
    public boolean matchesFilter(final Filter filters) {
        if (filters.getName() != null && !name.startsWith(filters.getName())) {
            return false;
        }
        if (filters.getOwner() != null && !owner.equals(filters.getOwner())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public boolean isOwnedBy(final User user) {
        if (user.getUsername().equals(owner)) {
            return true;
        }
        for (Song song: songs) {
            if (user.getUsername().equals(song.getArtist())) {
                return true;
            }
        }
        return false;
    }
}
