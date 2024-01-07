package app.player;

import app.audio.*;
import app.persons.Artist;
import app.persons.Host;
import app.persons.Listener;
import app.persons.User;
import app.results.*;
import fileio.input.EpisodeInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.*;

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
    private AdBreakSave adBreakSave;

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
     * @param command given command
     * @return result formatted for output
     */
    public LoadResult load(final Command command) {
        Listener listener = (Listener) user;
        LoadResult result = new LoadResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());

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
     * @param command given command
     * @return result formatted for output
     */
    public PlayPauseResult playPause(final Command command) {
        PlayPauseResult result = new PlayPauseResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());

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
    public StatusResult status(final Command command) {
        if (user == null) {
            return null;
        }

        StatusResult result = new StatusResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
        currentFile = updateStatus(command);

        if (currentFile == null) {
            currentItem = null;
        }
        result.setStats(status);
        return result;
    }

    /**
     * @param command given command
     * @return result formatted for output
     */
    public AddRemoveInPlaylistResult addRemoveInPlaylist(final Command command) {
        Listener listener = (Listener) user;
        AddRemoveInPlaylistResult result = new AddRemoveInPlaylistResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());

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
    public LikeResult like(final Command command) {
        Listener listener = (Listener) user;
        if (user == null) {
            return null;
        }

        LikeResult result = new LikeResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
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
     * @param command given command
     * @return result formatted for output
     */
    public RepeatResult repeat(final Command command) {
        currentFile = updateStatus(command);
        RepeatResult result = new RepeatResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());

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
     * @param command given command
     * @return result formatted for output
     */
    public ShuffleResult shuffle(final Command command) {
        ShuffleResult result = new ShuffleResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());

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
     * @param command given command
     * @return result formatted for output
     */
    public ForwardResult forward(final Command command) {
        final int forwardSize = 90;
        ForwardResult result = new ForwardResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
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
     * @param command given command
     * @return result formatted for output
     */
    public BackwardResult backward(final Command command) {
        final int backwardSize = 90;
        BackwardResult result = new BackwardResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
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
     * @param command given command
     * @return result formatted for output
     */
    public NextResult next(final Command command) {
        NextResult result = new NextResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
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
    public PrevResult prev(final Command command) {
        PrevResult result = new PrevResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
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
    public FollowResult follow(final Command command) {
        FollowResult result = new FollowResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
        if (searchBar.getSelection() == null) {
            result.setMessage("Please select a source before following or unfollowing.");
            return result;
        }

        // Here I experimented with some ways to remove instanceof
        // I'm not sure if this work well, none of the Stage 2 tests attempt this
        try {
            Playlist playlist = (Playlist) searchBar.getSelection();
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
        } catch (Exception e) {
            result.setMessage("The selected source is not a playlist.");
            return result;
        }
    }


    /**
     * changes the page accessed by the listener
     *
     * @param command the given command
     * @return formatted output
     */
    public ChangePageResult changePage(final Command command) {
        ChangePageResult result = new ChangePageResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
        if (user == null) {
            return null;
        }

        if (command.getNextPage().equals("Home")) {
            ((Listener) user).getCurrentPage().changeToHome((Listener) user);
            result.setMessage(user.getUsername() + " accessed Home successfully.");
        } else if (command.getNextPage().equals("LikedContent")) {
            ((Listener) user).getCurrentPage().changeToLikedContent((Listener) user);
            result.setMessage(user.getUsername() + " accessed LikedContent successfully.");
        } else {
            result.setMessage(user.getUsername() + " is trying to access a non-existent page.");
        }
        return result;
    }

    /**
     * change page to one with a different owner
     */
    public void changePageOwner() {
        Listener listener = (Listener) user;
        User selectedUser = (User) searchBar.getSelection();

        if (selectedUser.canAddArtistItems()) {
            listener.getCurrentPage().changeToArtist((Artist) selectedUser);
        } else {
            listener.getCurrentPage().changeToHost((Host) selectedUser);
        }
    }

    /**
     * switches the connection status of a listener
     *
     * @param command the given command
     * @return formatted output
     */
    public SwitchConnectionStatusResult switchConnectionStatus(final Command command) {
        currentFile = updateStatus(command);
        SwitchConnectionStatusResult result = new SwitchConnectionStatusResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());

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
    public AddAlbumResult addAlbum(final Command command) {
        AddAlbumResult result = new AddAlbumResult();
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());

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
    public AddEventResult addEvent(final Command command) {
        AddEventResult result = new AddEventResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

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
        ((Artist) user).getEvents().add(event);
        result.setMessage(user.getUsername() + " has added new event successfully.");
        return result;
    }

    /**
     * add merch described by command, if possible
     *
     * @param command the given command
     * @return formatted output
     */
    public AddMerchResult addMerch(final Command command) {
        AddMerchResult result = new AddMerchResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }
        if (!user.canAddArtistItems()) {
            result.setMessage(user.getUsername() + " is not an artist.");
            return result;
        }
        if (((Artist) user).hasMerchWithName(command.getName())) {
            result.setMessage(user.getUsername() + " has merchandise with the same name.");
            return result;
        }
        if (command.getPrice() < 0) {
            result.setMessage("Price for merchandise can not be negative.");
            return result;
        }
        Merch merch = new Merch(command);
        ((Artist) user).getMerchItems().add(merch);
        result.setMessage(user.getUsername() + " has added new merchandise successfully.");
        return result;
    }

    /**
     * delete the user given through command, if possible
     *
     * @param command the given command
     * @return formatted output
     */
    public DeleteUserResult deleteUser(final Command command) {
        DeleteUserResult result = new DeleteUserResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

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
    public AddPodcastResult addPodcast(final Command command) {
        AddPodcastResult result = new AddPodcastResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

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
    public AddAnnouncementResult addAnnouncement(final Command command) {
        AddAnnouncementResult result = new AddAnnouncementResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

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
        result.setMessage(command.getUsername() + " has successfully added new announcement.");
        return result;
    }

    /**
     * removes the announcement given through command, if possible
     *
     * @param command the given command
     * @return formatted output
     */
    public RemoveAnnouncementResult removeAnnouncement(final Command command) {
        RemoveAnnouncementResult result = new RemoveAnnouncementResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

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
    public RemoveAlbumResult removeAlbum(final Command command) {
        RemoveAlbumResult result = new RemoveAlbumResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

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
    public RemovePodcastResult removePodcast(final Command command) {
        RemovePodcastResult result = new RemovePodcastResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

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
    public RemoveEventResult removeEvent(final Command command) {
        RemoveEventResult result = new RemoveEventResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());

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

    public MessageResult buyMerch(final Command command) {
        MessageResult result = new MessageResult
                .Builder(command.getCommand(), command.getTimestamp())
                .username(command.getUsername())
                .build();
        if (user == null) {
            result.setMessage("The username " + command.getUsername() + " doesn't exist.");
            return result;
        }

        Listener listener = (Listener) user;

        if (listener.getCurrentPage().getPageType() != Page.Type.ARTIST) {
            result.setMessage("Cannot buy merch from this page.");
            return result;
        }

        Artist artist = (Artist) listener.getCurrentPage().getPageOwner();
        for (Merch merch : artist.getMerchItems()) {
            if (merch.getName().equals(command.getName())) {
                artist.setMerchRevenue(artist.getMerchRevenue() + merch.getPrice());
                result.setMessage(listener.getUsername() + " has added new merch successfully.");
                return result;
            }
        }

        result.setMessage("The merch " + command.getName() + " doesn't exist.");
        return result;
    }

    public MessageResult buyPremium(final Command command) {
        currentFile = updateStatus(command);
        MessageResult result = new MessageResult
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

        listener.splitMoney();
        listener.setSongsRevenueShare(new HashMap<>());
        listener.setRevenueSongs(0);
        listener.setPremium(true);
        listener.setRevenue(1e6);
        result.setMessage(command.getUsername() + " bought the subscription successfully.");
        return result;
    }

    public MessageResult cancelPremium(final Command command) {
        currentFile = updateStatus(command);
        MessageResult result = new MessageResult
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
        listener.setSongsRevenueShare(new HashMap<>());
        listener.setRevenueSongs(0);
        listener.setRevenue(0);
        listener.setPremium(false);
        result.setMessage(command.getUsername() + " cancelled the subscription successfully.");
        return result;
    }

    public MessageResult adBreak(final Command command) {
        currentFile = updateStatus(command);
        MessageResult result = new MessageResult
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
        ((Listener)getUser()).setRevenue(command.getPrice());
        result.setMessage("Ad inserted successfully.");
        return result;
    }
}