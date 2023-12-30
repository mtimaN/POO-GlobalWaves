package app.persons;

import app.audio.LibrarySingleton;
import app.audio.Playlist;
import app.audio.Podcast;
import app.audio.Song;
import app.player.AudioPlayer;
import app.player.Page;
import app.results.PlaylistOutput;
import app.results.PrintCurrentPageResult;
import app.results.ShowPlaylistsResult;
import app.results.ShowPreferredSongsResult;
import app.results.SwitchVisibilityResult;
import fileio.input.UserInput;
import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.ArrayList;
import java.util.HashMap;

@Getter @Setter
public final class Listener extends User {

    private final ArrayList<Playlist> playlists;
    private final ArrayList<Song> likedSongs;
    private final HashMap<Podcast, Integer> podcastListenTime;
    private boolean online;
    private Page currentPage;

    public Listener(final Command command) {
        super(command);
        this.playlists = new ArrayList<>();
        this.likedSongs = new ArrayList<>();
        podcastListenTime = new HashMap<>();
        online = true;
        currentPage = new Page(this);
    }
    public Listener(final UserInput input) {
        super(input);
        this.playlists = new ArrayList<>();
        this.likedSongs = new ArrayList<>();
        podcastListenTime = new HashMap<>();
        online = true;
        currentPage = new Page(this);
    }

    @Override
    public boolean canSwitchConnectionStatus() {
        return true;
    }

    /**
     * Shows all liked songs by user
     * @param command the given command
     * @return result formatted for output
     */
    public ShowPreferredSongsResult showPreferredSongs(final Command command) {
        ShowPreferredSongsResult result = new ShowPreferredSongsResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
        for (Song song: likedSongs) {
            result.getResult().add(song.getName());
        }
        return result;
    }

    /**
     * shows all playlists created by the user
     * @param command the given command
     * @return the result formatted for output
     */
    public ShowPlaylistsResult showPlaylists(final Command command) {
        ShowPlaylistsResult result = new ShowPlaylistsResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
        for (Playlist playlist: playlists) {
            result.getResult().add(new PlaylistOutput(playlist));
        }
        return result;
    }

    /**
     * switches the visibility of a playlist based on id
     * @param command the given command
     * @return the result formatted for output
     */
    public SwitchVisibilityResult switchVisibility(final Command command) {
        SwitchVisibilityResult result = new SwitchVisibilityResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
        if (command.getPlaylistId() > playlists.size()) {
            result.setMessage("The specified playlist ID is too high.");
            return result;
        }
        Playlist playlist = playlists.get(command.getPlaylistId() - 1);
        if (playlist.getVisibility().equals("public")) {
            playlist.setVisibility("private");
        } else {
            playlist.setVisibility("public");
        }
        result.setMessage("Visibility status updated successfully to "
                            + playlist.getVisibility() + ".");
        return result;
    }

    /**
     * @param command the given command
     * @return the current page, formatted for output
     */
    public PrintCurrentPageResult printCurrentPage(final Command command) {
        PrintCurrentPageResult result = new PrintCurrentPageResult();
        result.setUser(getUsername());
        result.setTimestamp(command.getTimestamp());

        if (!isOnline()) {
            result.setMessage(getUsername() + " is offline.");
            return result;
        }
        currentPage.generateCurrentPage();
        result.setMessage(currentPage.getContent());
        return result;
    }

    @Override
    public boolean canAddArtistItems() {
        return false;
    }

    @Override
    public boolean canAddHostItems() {
        return false;
    }

    @Override
    public String delete(final Command command) {
        LibrarySingleton library = LibrarySingleton.getInstance();
        for (AudioPlayer player: library.getAudioPlayers().values()) {
            player.setCurrentFile(player.updateStatus(command));
            if (player.getCurrentItem() == null) {
                continue;
            }
            if (player.getCurrentItem().getClass().equals(Playlist.class)) {
                if (((Playlist) player.getCurrentItem()).getOwner().equals(getUsername())) {
                    return getUsername() + " can't be deleted.";
                }
            }
        }
        library.getListeners().remove(this);
        library.getAudioPlayers().remove(getUsername());
        for (Playlist playlist: playlists) {
            library.getPlaylists().remove(playlist);
        }
        for (Playlist playlist: library.getPlaylists()) {
            playlist.getFollowers().remove(this);
        }
        return getUsername() + " was successfully deleted.";
    }
}
