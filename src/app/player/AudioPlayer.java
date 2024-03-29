package app.player;

import app.audio.Album;
import app.audio.AudioFile;
import app.audio.AudioItem;
import app.audio.LibrarySingleton;
import app.audio.Playlist;
import app.audio.Podcast;
import app.audio.Song;
import app.output.format_classes.PodcastOutput;
import app.output.results.GeneralResult;
import app.output.results.ShowPodcastsResult;
import app.persons.Artist;
import app.persons.Host;
import app.persons.Listener;
import app.persons.User;

import fileio.input.EpisodeInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

@Getter @Setter
public final class AudioPlayer {
    private SearchBar searchBar;
    private User user;
    private AudioItem currentItem;
    private AudioFile currentFile;
    private int playTimestamp;
    private int elapsedTime;
    private Status status;
    private int trackId;
    private int seed;
    private boolean adBreakNext;
    private Memento memento;

    public AudioPlayer(final Command command) {
        user = LibrarySingleton.getInstance().findUserByUsername(command.getUsername());
        searchBar = new SearchBar();
        currentItem = null;
        status = new Status();
    }

    /**
     * updates the status up to that command and
     * empties the player
     *
     * @param command given command
     */
    public void emptyPlayer(final Command command) {
        currentFile = updateStatus(command);
        setCurrentItem(null);
        setCurrentFile(null);
        status.empty();
    }

    /**
     * loads the selected item into the player
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult load(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        Listener listener = (Listener) user;

        if (searchBar.getSelection() == null || !searchBar.getSelection().isPlayable()) {
            result.setMessage("Please select a source before attempting to load.");
            return result;
        }

        status.setRepeat("No Repeat");
        status.setPaused(false);
        status.setShuffle(false);
        currentItem = (AudioItem) searchBar.getSelection();
        playTimestamp = command.getTimestamp();
        elapsedTime = 0;
        trackId = 0;

        if (currentItem.savesProgress()) {
            if (listener.getPodcastListenTime().containsKey((Podcast) currentItem)) {
                elapsedTime = listener.getPodcastListenTime().get((Podcast) currentItem);
            } else {
                listener.getPodcastListenTime().put((Podcast) currentItem, 0);
            }
        }

        result.setMessage("Playback loaded successfully.");
        searchBar.setSelection(null);
        adBreakNext = false;
        return result;
    }

    /**
     * plays or pauses the audio
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult playPause(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        currentFile = updateStatus(command);
        if (currentItem == null) {
            result.setMessage(
                    "Please load a source before attempting to pause or resume playback.");
        } else {
            status.setPaused(!status.isPaused());
            if (status.isPaused()) {
                elapsedTime += command.getTimestamp() - playTimestamp;
                result.setMessage("Playback paused successfully.");
            } else {
                playTimestamp = command.getTimestamp();
                result.setMessage("Playback resumed successfully.");
            }
        }
        return result;
    }

    /**
     * It nulls the status if there's nothing playing. Otherwise, it sets the
     * timestamp of the last play and calls play.
     *
     * @param command the given command
     * @return the currently played file
     */
    public AudioFile updateStatus(final Command command) {
        if (user == null) {
            return null;
        }

        if (!user.canSwitchConnectionStatus()) {
            return null;
        }

        Listener listener = (Listener) user;
        if (currentItem == null) {
            status.empty();
            return null;
        }

        if (!status.isPaused() && listener.isOnline()) {
            elapsedTime += command.getTimestamp() - playTimestamp;
            playTimestamp = command.getTimestamp();
        }

        return currentItem.play(this, command);
    }

    /**
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult status(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        if (user == null) {
            return null;
        }

        currentFile = updateStatus(command);

        if (currentFile == null) {
            currentItem = null;
        }
        result.setStats(status);
        return result;
    }

    /**
     * add or remove current song from playlist
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult addRemoveInPlaylist(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        Listener listener = (Listener) user;

        if (currentItem == null) {
            result.setMessage(
                    "Please load a source before adding to or removing from the playlist.");
            return result;
        }

        int playlistId = command.getPlaylistId();
        if (playlistId > listener.getPlaylists().size()) {
            result.setMessage("The specified playlist does not exist.");
            return result;
        }

        currentFile = updateStatus(command);
        if (currentFile == null || !currentFile.isSong()) {
            result.setMessage("The loaded source is not a song.");
            return result;
        }

        ArrayList<Song> songs = listener.getPlaylists().get(playlistId - 1).getSongs();
        for (Song song : songs) {
            if (song.getName().equals(currentFile.getName())) {
                songs.remove(song);
                result.setMessage("Successfully removed from playlist.");
                return result;
            }
        }

        songs.add(LibrarySingleton.getInstance().findSongByName(currentFile.getName()));
        result.setMessage("Successfully added to playlist.");
        return result;
    }

    /**
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult like(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        Listener listener = (Listener) user;
        if (user == null) {
            return null;
        }

        if (!listener.isOnline()) {
            result.setMessage(user.getUsername() + " is offline.");
            return result;
        }

        if (currentItem == null) {
            result.setMessage("Please load a source before liking or unliking.");
            return result;
        }

        currentFile = updateStatus(command);
        if (currentFile == null || !currentFile.isSong()) {
            result.setMessage("The loaded source is not a song.");
            return result;
        }

        ArrayList<Song> songs = listener.getLikedSongs();
        for (Song song : songs) {
            if (song.getName().equals(currentFile.getName())) {
                songs.remove(song);
                song.unlike();
                result.setMessage("Unlike registered successfully.");
                return result;
            }
        }

        songs.add((Song) currentFile);
        ((Song) currentFile).like();
        result.setMessage("Like registered successfully.");
        return result;
    }

    /**
     * switch the repeat status of the player
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult repeat(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        currentFile = updateStatus(command);

        if (currentItem == null) {
            result.setMessage("Please load a source before setting the repeat status.");
            return result;
        }

        if (currentItem.isSongCollection()) {
            switch (status.getRepeat()) {
                case "No Repeat" -> status.setRepeat("Repeat All");
                case "Repeat All" -> status.setRepeat("Repeat Current Song");
                case "Repeat Current Song" -> status.setRepeat("No Repeat");
                default -> System.err.println("Unrecognized Repeat Status");
            }
        } else {
            switch (status.getRepeat()) {
                case "No Repeat" -> status.setRepeat("Repeat Once");
                case "Repeat Once" -> status.setRepeat("Repeat Infinite");
                case "Repeat Infinite" -> status.setRepeat("No Repeat");
                default -> System.err.println("Unrecognized Repeat Status");
            }
        }
        result.setMessage("Repeat mode changed to " + status.getRepeat().toLowerCase() + ".");
        return result;
    }

    /**
     * switch the shuffle status of the player
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult shuffle(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        currentFile = updateStatus(command);
        if (currentItem == null) {
            result.setMessage("Please load a source before using the shuffle function.");
            return result;
        }
        if (!currentItem.canBeShuffled()) {
            result.setMessage("The loaded source is not a playlist or an album.");
            return result;
        }
        if (status.isShuffle()) {
            result.setMessage("Shuffle function deactivated successfully.");
        } else {
            seed = command.getSeed();
            result.setMessage("Shuffle function activated successfully.");
        }
        status.setShuffle(!status.isShuffle());
        return result;
    }

    /**
     * skip forwardSize seconds from the audio
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult forward(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        final int forwardSize = 90;
        currentFile = updateStatus(command);

        if (currentItem == null) {
            result.setMessage("Please load a source before attempting to forward.");
            return result;
        }
        if (!currentItem.allowForwardBackward()) {
            result.setMessage("The loaded source is not a podcast.");
            return result;
        }
        elapsedTime += Math.min(status.getRemainedTime(), forwardSize);
        result.setMessage("Skipped forward successfully.");
        return result;
    }

    /**
     * go back backwardSize seconds
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult backward(final Command command) {
        final int backwardSize = 90;
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        currentFile = updateStatus(command);

        if (currentFile == null) {
            result.setMessage("Please select a source before rewinding.");
            return result;
        }
        if (!currentItem.allowForwardBackward()) {
            result.setMessage("The loaded source is not a podcast.");
            return result;
        }
        elapsedTime -= Math.min(currentFile.getDuration() - status.getRemainedTime(),
                backwardSize);
        currentFile = updateStatus(command);
        result.setMessage("Rewound successfully.");
        return result;
    }

    /**
     * skip to the next audio file
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult next(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        currentFile = updateStatus(command);

        if (currentFile == null) {
            result.setMessage("Please load a source before skipping to the next track.");
            return result;
        }
        elapsedTime += status.getRemainedTime();
        currentFile = updateStatus(command);
        if (status.isPaused()) {
            playPause(command);
        }
        if (currentFile != null) {
            result.setMessage("Skipped to next track successfully. The current track is "
                    + currentFile.getName() + ".");
        } else {
            result.setMessage("Please load a source before skipping to the next track.");
        }
        return result;
    }

    /**
     * gets the previous id of the song played in a shuffled playlist.
     *
     * @return previous id
     */
    public int getPrevShuffledTrack() {
        int id = trackId;
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < ((Playlist) currentItem).getSongs().size(); ++i) {
            positions.add(i, i);
        }
        Collections.shuffle(positions, new Random(seed));
        if (positions.get(0) == id) {
            return id;
        }
        int shuffledId = positions.indexOf(id);
        if (shuffledId == -1) {
            return 0;
        }
        return positions.get(shuffledId - 1);
    }

    /**
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult prev(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        currentFile = updateStatus(command);

        if (currentFile == null) {
            result.setMessage("Please load a source before returning to the previous track.");
            return result;
        }
        if (status.getRemainedTime() != currentFile.getDuration()) {
            elapsedTime += status.getRemainedTime() - currentFile.getDuration();
        } else {
            // repeatState is used so that prev will ignore the repeating pattern
            String repeatState = status.getRepeat();
            status.setRepeat("No Repeat");
            elapsedTime -= 2;
            if (status.isShuffle()) {
                trackId = getPrevShuffledTrack();
            } else {
                trackId = trackId > 0 ? trackId - 1 : 0;
            }

            currentFile = updateStatus(command);
            status.setRepeat(repeatState);
            if (currentFile != null) {
                elapsedTime += status.getRemainedTime() - currentFile.getDuration();
            } else {
                elapsedTime = 0;
                currentFile = updateStatus(command);
            }
        }
        if (status.isPaused()) {
            playPause(command);
        }
        if (currentFile == null) {
            return result;
        }
        result.setMessage("Returned to previous track successfully. The current track is "
                + currentFile.getName() + ".");
        return result;
    }

    /**
     * @param command given command
     * @return result formatted for output
     */
    public GeneralResult follow(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        if (searchBar.getSelection() == null) {
            result.setMessage("Please select a source before following or unfollowing.");
            return result;
        }

        Playlist playlist;

        try {
            playlist = (Playlist) searchBar.getSelection();
        } catch (Exception e) {
            result.setMessage("The selected source is not a playlist.");
            return result;
        }

        if (playlist.getOwner().equals(command.getUsername())) {
            result.setMessage("You cannot follow or unfollow your own playlist.");
            return result;
        }
        ArrayList<Listener> followers = playlist.getFollowers();
        for (Listener follower : followers) {
            if (user.equals(follower)) {
                result.setMessage("Playlist unfollowed successfully.");
                followers.remove(follower);
                return result;
            }
        }
        result.setMessage("Playlist followed successfully.");
        followers.add((Listener) user);
        return result;
    }


    /**
     * changes the page accessed by the listener
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult changePage(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        if (user == null) {
            return null;
        }

        currentFile = updateStatus(command);
        Listener listener = (Listener) user;

        switch (command.getNextPage()) {
            case "Home" -> {
                listener.getPreviousPages().push(listener.getCurrentPage().takeSnapshot());
                listener.setNextPages(new Stack<>());
                listener.getCurrentPage().changeToHome((Listener) user);
                result.setMessage(user.getUsername() + " accessed Home successfully.");
            }
            case "LikedContent" -> {
                listener.getPreviousPages().push(listener.getCurrentPage().takeSnapshot());
                listener.setNextPages(new Stack<>());
                listener.getCurrentPage().changeToLikedContent((Listener) user);
                result.setMessage(user.getUsername() + " accessed LikedContent successfully.");
            }
            case "Artist" -> {
                listener.getPreviousPages().push(listener.getCurrentPage().takeSnapshot());
                listener.setNextPages(new Stack<>());
                Artist artist = LibrarySingleton.getInstance()
                        .findArtistByName(((Song) currentFile).getArtist());

                listener.getCurrentPage().changeToArtist(artist);
                result.setMessage(user.getUsername() + " accessed Artist successfully.");
            }
            case "Host" -> {
                listener.getPreviousPages().push(listener.getCurrentPage().takeSnapshot());
                listener.setNextPages(new Stack<>());
                Host host = LibrarySingleton.getInstance()
                        .findHostByName(((Podcast) currentItem).getOwner());

                listener.getCurrentPage().changeToHost(host);
                result.setMessage(user.getUsername() + " accessed Host successfully.");
            }
            default -> result.setMessage(user.getUsername()
                    + " is trying to access a non-existent page.");
        }

        return result;
    }

    /**
     * change page to one with a different owner
     */
    public void changePageOwner() {
        Listener listener = (Listener) user;
        User selectedUser = (User) searchBar.getSelection();

        listener.getPreviousPages().push(listener.getCurrentPage().takeSnapshot());
        if (selectedUser.canAddArtistItems()) {
            listener.getCurrentPage().changeToArtist((Artist) selectedUser);
        } else {
            listener.getCurrentPage().changeToHost((Host) selectedUser);
        }
    }

    /**
     * load the player with the last recommended audio item
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult loadRecommendations(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        currentFile = updateStatus(command);

        Listener listener = (Listener) user;
        if (!listener.isOnline()) {
            result.setMessage(command.getUsername() + " is offline.");
            return result;
        }

        if (listener.getRecommendation() == null) {
            result.setMessage("No recommendations available.");
            return result;
        }

        status.setRepeat("No Repeat");
        status.setPaused(false);
        status.setShuffle(false);
        currentItem = listener.getRecommendation();
        playTimestamp = command.getTimestamp();
        elapsedTime = 0;
        trackId = 0;

        result.setMessage("Playback loaded successfully.");
        adBreakNext = false;
        return result;

    }
    /**
     * switches the connection status of a listener
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult switchConnectionStatus(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        currentFile = updateStatus(command);

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }

        if (!user.canSwitchConnectionStatus()) {
            result.setMessage(command.getUsername() + " is not a normal user.");
            return result;
        }

        Listener listener = (Listener) user;
        listener.setOnline(!listener.isOnline());
        if (listener.isOnline()) {
            playTimestamp = command.getTimestamp();
        } else {
            elapsedTime += command.getTimestamp() - playTimestamp;
        }
        result.setMessage(command.getUsername() + " has changed status successfully.");
        return result;
    }


    /**
     * add a new album to the library
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult addAlbum(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }

        if (!user.canAddArtistItems()) {
            result.setMessage(command.getUsername() + " is not an artist.");
            return result;
        }
        Artist artist = (Artist) user;
        for (Album album : artist.getAlbums()) {
            if (album.getName().equals(command.getName())) {
                result.setMessage(command.getUsername()
                        + " has another album with the same name.");
                return result;
            }
        }
        for (int i = 0; i < command.getSongs().size() - 1; ++i) {
            for (int j = i + 1; j < command.getSongs().size(); ++j) {
                if (command.getSongs().get(i).getName()
                        .equals(command.getSongs().get(j).getName())) {
                    result.setMessage(command.getUsername()
                            + " has the same song at least twice in this album.");
                    return result;
                }
            }
        }

        ArrayList<Song> librarySongs = LibrarySingleton.getInstance().getSongs();
        Album album = new Album(command);
        for (SongInput songInput : command.getSongs()) {
            Song song = new Song(songInput);
            album.getSongs().add(song);
            librarySongs.add(song);
        }
        artist.getAlbums().add(album);
        for (Listener subscriber: artist.getSubscribers()) {
            subscriber.getNotifications().add(new Notification("New Album",
                    "New Album from " + artist.getUsername() + "."));
        }
        LibrarySingleton.getInstance().getAlbums().add(album);
        result.setMessage(command.getUsername() + " has added new album successfully.");
        return result;
    }

    /**
     * @param eventName name of an event
     * @return true if event with given name already exists
     */
    public boolean eventAlreadyExists(final String eventName) {
        Artist artist = (Artist) user;
        for (Event event : artist.getEvents()) {
            if (event.getName().equals(eventName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * add event described by command
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult addEvent(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }

        if (!user.canAddArtistItems()) {
            result.setMessage(user.getUsername() + " is not an artist.");
            return result;
        }
        if (eventAlreadyExists(command.getName())) {
            result.setMessage(user.getUsername() + " has another event with the same name.");
            return result;
        }
        if (!Event.isDateValid(command.getDate())) {
            result.setMessage("Event for " + user.getUsername() + " does not have a valid date.");
            return result;
        }
        Event event = new Event(command);
        Artist artist = (Artist) user;
        artist.getEvents().add(event);
        for (Listener subscriber: artist.getSubscribers()) {
            subscriber.getNotifications().add(new Notification("New Event",
                    "New Event from " + artist.getName() + "."));
        }
        result.setMessage(user.getUsername() + " has added new event successfully.");
        return result;
    }

    /**
     * add merch described by command, if possible
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult addMerch(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        if (!user.canAddArtistItems()) {
            result.setMessage(user.getUsername() + " is not an artist.");
            return result;
        }

        Artist artist = (Artist) user;
        if (artist.hasMerchWithName(command.getName())) {
            result.setMessage(user.getUsername() + " has merchandise with the same name.");
            return result;
        }
        if (command.getPrice() < 0) {
            result.setMessage("Price for merchandise can not be negative.");
            return result;
        }
        Merch merch = new Merch(command);
        artist.getMerchItems().add(merch);
        for (Listener subscriber: artist.getSubscribers()) {
            subscriber.getNotifications().add(new Notification("New Merchandise",
                    "New Merchandise from " + artist.getName() + "."));
        }
        result.setMessage(user.getUsername() + " has added new merchandise successfully.");
        return result;
    }

    /**
     * delete the user given through command, if possible
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult deleteUser(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        result.setMessage(user.delete(command));
        return result;
    }

    /**
     * add the podcast described by command
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult addPodcast(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        if (!user.canAddHostItems()) {
            result.setMessage(command.getUsername() + " is not a host.");
            return result;
        }
        Host host = (Host) user;
        for (Podcast podcast : host.getPodcasts()) {
            if (podcast.getName().equals(command.getName())) {
                result.setMessage(command.getUsername()
                        + " has another podcast with the same name.");
                return result;
            }
        }

        ArrayList<EpisodeInput> episodeInputs = command.getEpisodes();
        for (int i = 0; i < episodeInputs.size() - 1; ++i) {
            for (int j = i + 1; j < episodeInputs.size(); ++j) {
                if (episodeInputs.get(i).getName().equals(episodeInputs.get(j).getName())) {
                    result.setMessage(command.getUsername()
                            + " has the same episode twice in this podcast.");
                    return result;
                }
            }
        }

        Podcast podcast = new Podcast(episodeInputs, command.getName(), command.getUsername());
        LibrarySingleton.getInstance().getPodcasts().add(podcast);
        host.getPodcasts().add(podcast);
        result.setMessage(command.getUsername() + " has added new podcast successfully.");
        return result;
    }

    /**
     * add the announcement described by command
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult addAnnouncement(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        if (!user.canAddHostItems()) {
            result.setMessage(command.getUsername() + " is not a host.");
            return result;
        }
        Host host = (Host) user;
        for (Announcement announcement : host.getAnnouncements()) {
            if (announcement.getName().equals(command.getName())) {
                result.setMessage(command.getUsername()
                        + " has already added an announcement with this name");
                return result;
            }
        }
        Announcement announcement = new Announcement(command.getName(), command.getDescription());
        host.getAnnouncements().add(announcement);
        for (Listener subscriber: host.getSubscribers()) {
            subscriber.getNotifications().add(new Notification("New Announcement",
                    "New Announcement from " + host.getName() + "."));
        }
        result.setMessage(command.getUsername() + " has successfully added new announcement.");
        return result;
    }

    /**
     * removes the announcement given through command, if possible
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult removeAnnouncement(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        if (!user.canAddHostItems()) {
            result.setMessage(command.getUsername() + " is not a host.");
            return result;
        }
        Host host = (Host) user;

        for (Announcement announcement : host.getAnnouncements()) {
            if (announcement.getName().equals(command.getName())) {
                host.getAnnouncements().remove(announcement);
                result.setMessage(command.getUsername()
                        + " has successfully deleted the announcement.");
                return result;
            }
        }
        result.setMessage(command.getUsername() + " has no announcement with the given name.");
        return result;
    }

    /**
     * shows all the podcasts of the User, if they are a host
     *
     * @param command the given command
     * @return formatted output
     */
    public ShowPodcastsResult showPodcasts(final Command command) {
        ShowPodcastsResult result = new ShowPodcastsResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

        Host host = (Host) user;
        for (Podcast podcast : host.getPodcasts()) {
            PodcastOutput podcastOutput = new PodcastOutput(podcast);
            result.getResult().add(podcastOutput);
        }
        return result;
    }

    /**
     * removes the album given by command, if possible
     *
     * @param command the given command
     * @return formatted output
     */
    public GeneralResult removeAlbum(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        if (!user.canAddArtistItems()) {
            result.setMessage(command.getUsername() + " is not an artist.");
            return result;
        }
        Artist artist = (Artist) user;
        Optional<Album> optionalAlbum = artist.getAlbums().stream()
                .filter(album -> album.getName().equals(command.getName())).findFirst();

        if (optionalAlbum.isEmpty()) {
            result.setMessage(command.getUsername()
                    + " doesn't have an album with the given name.");
            return result;
        }

        Album toBeDeletedAlbum = optionalAlbum.get();
        LibrarySingleton library = LibrarySingleton.getInstance();
        for (AudioPlayer audioPlayer : library.getAudioPlayers().values()) {
            audioPlayer.setCurrentFile(audioPlayer.updateStatus(command));
            if (audioPlayer.getCurrentItem() == null) {
                continue;
            }
            if (audioPlayer.getCurrentItem().containsSongsFrom(toBeDeletedAlbum)) {
                result.setMessage(command.getUsername() + " can't delete this album.");
                return result;
            }
        }

        artist.getAlbums().remove(toBeDeletedAlbum);
        toBeDeletedAlbum.delete();
        result.setMessage(command.getUsername() + " deleted the album successfully.");
        return result;
    }

    /**
     * removes the podcast given by command, if possible
     *
     * @param command the given command
     * @return the formatted output
     */
    public GeneralResult removePodcast(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        if (!user.canAddHostItems()) {
            result.setMessage(command.getUsername() + "is not a host.");
            return result;
        }
        Host host = (Host) user;
        Optional<Podcast> podcastOptional = host.getPodcasts().stream()
                .filter(podcast -> podcast.getName().equals(command.getName())).findFirst();

        if (podcastOptional.isEmpty()) {
            result.setMessage(command.getUsername()
                    + " doesn't have a podcast with the given name.");
            return result;
        }

        Podcast toBeDeletedPodcast = podcastOptional.get();
        for (Podcast podcast : host.getPodcasts()) {
            if (podcast.getName().equals(command.getName())) {
                toBeDeletedPodcast = podcast;
                break;
            }
        }

        LibrarySingleton library = LibrarySingleton.getInstance();
        for (AudioPlayer audioPlayer : library.getAudioPlayers().values()) {
            audioPlayer.setCurrentFile(audioPlayer.updateStatus(command));
            if (audioPlayer.getCurrentItem() == null) {
                continue;
            }
            if (audioPlayer.getCurrentItem().equals(toBeDeletedPodcast)) {
                result.setMessage(command.getUsername() + " can't delete this podcast.");
                return result;
            }
        }
        host.getPodcasts().remove(toBeDeletedPodcast);
        toBeDeletedPodcast.delete();
        result.setMessage(command.getUsername() + " deleted the podcast successfully.");
        return result;
    }

    /**
     * removes the event given by command, if possible
     *
     * @param command the given command
     * @return the formatted output
     */
    public GeneralResult removeEvent(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        if (!user.canAddArtistItems()) {
            result.setMessage(command.getUsername() + " is not an artist.");
            return result;
        }

        Artist artist = (Artist) user;
        for (Event event : artist.getEvents()) {
            if (event.getName().equals(command.getName())) {
                artist.getEvents().remove(event);
                result.setMessage(command.getUsername() + " deleted the event successfully.");
                return result;
            }
        }

        result.setMessage(command.getUsername() + " doesn't have an event with the given name");
        return result;
    }

    /**
     * buy merch from an artist
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult buyMerch(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }

        Listener listener = (Listener) user;

        if (listener.getCurrentPage().getPageType() != Page.PageType.ARTIST) {
            result.setMessage("Cannot buy merch from this page.");
            return result;
        }

        Artist artist = (Artist) listener.getCurrentPage().getPageOwner();
        for (Merch merch : artist.getMerchItems()) {
            if (merch.getName().equals(command.getName())) {
                artist.setMerchRevenue(artist.getMerchRevenue() + merch.getPrice());
                listener.getBoughtMerch().add(merch.getName());
                result.setMessage(listener.getUsername() + " has added new merch successfully.");
                return result;
            }
        }

        result.setMessage("The merch " + command.getName() + " doesn't exist.");
        return result;
    }

    /**
     * see the merch bought by the user
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult seeMerch(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .result()
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            result.setResult(null);
            return result;
        }

        Listener listener = (Listener) user;
        for (String merch: listener.getBoughtMerch()) {
            result.getResult().add(merch);
        }
        return result;
    }

    /**
     * buy premium for the user
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult buyPremium(final Command command) {
        final double premiumRevenue = 1e6;
        currentFile = updateStatus(command);
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + "doesn't exist.");
            return result;
        }
        Listener listener = (Listener) user;
        if (listener.isPremium()) {
            result.setMessage(command.getUsername() + " is already a premium user.");
            return result;
        }

        listener.setPremium(true);
        listener.setRevenue(premiumRevenue);
        result.setMessage(command.getUsername() + " bought the subscription successfully.");
        return result;
    }

    /**
     * cancel the premium subscription
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult cancelPremium(final Command command) {
        currentFile = updateStatus(command);
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + "doesn't exist.");
            return result;
        }

        Listener listener = (Listener) user;
        if (!listener.isPremium()) {
            result.setMessage(command.getUsername() + " is not a premium user.");
            return result;
        }
        listener.splitMoney();
        listener.setPremium(false);
        result.setMessage(command.getUsername() + " cancelled the subscription successfully.");
        return result;
    }


    /**
     * take a snapshot of the player
     * @return a new memento of the current state
     */
    public Memento takeSnapshot() {
        return new Memento(status, currentItem);
    }

    /**
     * restore the player to the memento value
     */
    public void restore() {
        status = memento.getStatus();
        currentItem = memento.getCurrentItem();
    }

    @Getter
    public static final class Memento {
        private final Status status;
        private final AudioItem currentItem;

        public Memento(final Status status, final AudioItem currentItem) {
            this.status = status;
            this.currentItem = currentItem;
        }
    }

    /**
     * insert an adBreak
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult adBreak(final Command command) {
        currentFile = updateStatus(command);
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + "doesn't exist.");
            return result;
        }

        if (currentFile == null || adBreakNext) {
            result.setMessage(command.getUsername() + " is not playing any music.");
            return result;
        }

        adBreakNext = true;
        ((Listener) user).setRevenue(command.getPrice());
        result.setMessage("Ad inserted successfully.");
        return result;
    }

    /**
     * subscribe to the current page owner
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult subscribe(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }

        Listener listener = (Listener) user;
        if (listener.getCurrentPage().getPageType() != Page.PageType.ARTIST
                && listener.getCurrentPage().getPageType() != Page.PageType.HOST) {
            result.setMessage("To subscribe you need to be on the page of an artist or host.");
            return result;
        }

        if (listener.isSubscribedToPageOwner()) {
            listener.unsubscribeFromPageOwner();
            result.setMessage(command.getUsername() + " unsubscribed from "
                    + listener.getCurrentPage().getPageOwner().getUsername() + " successfully.");
        } else {
            listener.subscribeToPageOwner();
            result.setMessage(command.getUsername() + " subscribed to "
                    + listener.getCurrentPage().getPageOwner().getUsername() + " successfully.");
        }
        return result;
    }

    /**
     * get the notifications of the user
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult getNotifications(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .notifications().build();
        for (Notification notification: user.getNotifications()) {
            result.getNotifications().add(notification);
        }
        user.setNotifications(new ArrayList<>());
        return result;
    }

    /**
     * update the recommendations of the listener
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult updateRecommendations(final Command command) {
        final int topSize = 5;
        final int topGenreSize = 3;
        final int minimumSecondsPassed = 30;
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }

        Listener listener;
        try {
            listener = (Listener) user;
        } catch (Exception e) {
            result.setMessage(command.getUsername() + " is not a normal user.");
            return result;
        }

        currentFile = updateStatus(command);

        if (currentFile == null) {
            result.setMessage("No new recommendations were found");
            return result;
        }
        switch (command.getRecommendationType()) {
            case "random_song" -> {
                int passedTime = currentFile.getDuration() - status.getRemainedTime();
                if (passedTime < minimumSecondsPassed) {
                    result.setMessage("No new recommendations were found");
                    return result;
                }
                Song currentSong = (Song) currentFile;
                ArrayList<Song> genreSongs = new ArrayList<>();
                for (Song song: LibrarySingleton.getInstance().getSongs()) {
                    if (song.getGenre().equals(currentSong.getGenre())) {
                        genreSongs.add(song);
                    }
                }
                listener.setRecommendation(genreSongs
                        .get(new Random(passedTime).nextInt(genreSongs.size())));
                listener.getSongRecommendations()
                        .add((Song) listener.getRecommendation());
            }
            case "random_playlist" -> {
                HashMap<String, Integer> genreListenCounts = new HashMap<>();

                ArrayList<Song> usedSongs = new ArrayList<>(listener.getLikedSongs());
                for (Playlist playlist: listener.getPlaylists()) {
                    usedSongs.addAll(playlist.getSongs());
                }
                for (Playlist playlist: LibrarySingleton.getInstance().getPlaylists()) {
                    if (playlist.getFollowers().contains(listener)) {
                        usedSongs.addAll(playlist.getSongs());
                    }
                }

                for (Song song: usedSongs) {
                    String genre = song.getGenre();
                    genreListenCounts.put(genre, genreListenCounts.getOrDefault(genre, 0) + 1);
                }

                ArrayList<String> top3Genres = genreListenCounts.entrySet().stream()
                        .sorted(Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                                .thenComparing(Entry.comparingByKey()))
                        .limit(topGenreSize)
                        .map(Entry::getKey)
                        .collect(Collectors.toCollection(ArrayList::new));

                ArrayList<Song> playlistSongs = usedSongs.stream()
                        .filter(song -> song.getGenre().equals(top3Genres.get(0)))
                        .sorted(Comparator.comparingInt(Song::getLikes).reversed())
                        .limit(topSize)
                        .collect(Collectors.toCollection(ArrayList::new));

                if (top3Genres.size() > 1) {
                    playlistSongs.addAll(usedSongs.stream()
                            .filter(song -> song.getGenre().equals(top3Genres.get(1)))
                            .sorted(Comparator.comparingInt(Song::getLikes).reversed())
                            .limit(topGenreSize)
                            .collect(Collectors.toCollection(ArrayList::new)));
                }
                if (top3Genres.size() == topGenreSize) {
                    playlistSongs.addAll(usedSongs.stream()
                            .filter(song -> song.getGenre().equals(top3Genres.get(2)))
                            .sorted(Comparator.comparingInt(Song::getLikes).reversed())
                            .limit(2)
                            .collect(Collectors.toCollection(ArrayList::new)));
                }

                if (playlistSongs.isEmpty()) {
                    result.setMessage("No new recommendations were found");
                    return result;
                }

                Playlist randomPlaylist =
                        new Playlist(listener.getUsername() + "'s recommendations", playlistSongs);
                listener.setRecommendation(randomPlaylist);
                listener.getPlaylistRecommendations().add(randomPlaylist);
            }
            case "fans_playlist" -> {
                Artist artist = LibrarySingleton.getInstance()
                        .findArtistByName(((Song) currentFile).getArtist());
                if (artist == null) {
                    return null;
                }
                Map<Listener, Integer> fansListenCounts = new HashMap<>();
                for (Listener fan : LibrarySingleton.getInstance().getListeners()) {
                    int listenCounter = getFanListens(listener, artist);
                    if (listenCounter > 0) {
                        fansListenCounts.put(fan, listenCounter);
                    }
                }
                ArrayList<Listener> top5Fans = fansListenCounts.entrySet().stream()
                        .sorted(Entry.<Listener, Integer>comparingByValue().reversed()
                                .thenComparing(entry -> entry.getKey().getUsername()))
                        .limit(topSize)
                        .map(Entry::getKey)
                        .collect(Collectors.toCollection(ArrayList::new));

                ArrayList<Song> fanSongs = new ArrayList<>();

                top5Fans.forEach(fan ->
                        fanSongs.addAll(fan.getLikedSongs().stream()
                                .sorted(Comparator.comparingInt(Song::getLikes).reversed())
                                .limit(topSize)
                                .collect(Collectors.toCollection(ArrayList::new))
                        )
                );

                if (fanSongs.isEmpty()) {
                    result.setMessage("No new recommendations were found");
                    return result;
                }

                Playlist fansPlaylist = new Playlist(artist.getName()
                        + " Fan Club recommendations", fanSongs);

                listener.setRecommendation(fansPlaylist);
                listener.getPlaylistRecommendations().add(fansPlaylist);
            }
            default -> { }
        }
        result.setMessage("The recommendations for user "
                + listener.getUsername() + " have been updated successfully.");
        return result;
    }

    private static int getFanListens(final Listener listener, final Artist artist) {
        int listenCounter = 0;
        for (Map.Entry<Song, Integer> entry : listener.getSongListens().entrySet()) {
            if (entry.getKey().getArtist().equals(artist.getUsername())) {
                int listens = entry.getValue();
                listenCounter += listens;
            }
        }
        return listenCounter;
    }


    /**
     * return to the previous page
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult previousPage(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        Listener listener = (Listener) user;
        if (listener.getPreviousPages().isEmpty()) {
            result.setMessage("There are no pages left to go back.");
            return result;
        }

        listener.getNextPages().push(listener.getCurrentPage().takeSnapshot());
        listener.getCurrentPage().restore(listener.getPreviousPages().pop());
        result.setMessage("The user " + command.getUsername()
                + " has navigated successfully to the previous page.");
        return result;
    }

    /**
     * return to the page before previousPage was called
     * @param command the given command
     * @return result formatted for output
     */
    public GeneralResult nextPage(final Command command) {
        GeneralResult result = new GeneralResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();

        Listener listener = (Listener) user;
        if (listener.getNextPages().isEmpty()) {
            result.setMessage("There are no pages left to go forward.");
            return result;
        }

        listener.getPreviousPages().push(listener.getCurrentPage().takeSnapshot());
        listener.getCurrentPage().restore(listener.getNextPages().pop());
        result.setMessage("The user " + command.getUsername()
                + " has navigated successfully to the next page.");
        return result;
    }
}
