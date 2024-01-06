package app.audio;

import app.persons.Artist;
import app.persons.Host;
import app.player.AudioPlayer;
import app.results.*;
import fileio.input.LibraryInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import fileio.input.UserInput;
import lombok.Getter;
import main.Command;
import app.persons.Listener;
import app.persons.User;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public final class LibrarySingleton {
    private static LibrarySingleton instance = null;
    private ArrayList<Song> songs;
    private ArrayList<Podcast> podcasts;
    private ArrayList<Album> albums = new ArrayList<>();
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private ArrayList<Listener> listeners;
    private ArrayList<Artist> artists = new ArrayList<>();
    private ArrayList<Host> hosts = new ArrayList<>();
    private HashMap<String, AudioPlayer> audioPlayers = new HashMap<>();
    private LibrarySingleton() { }

    /**
     * @return the instance of Library
     */
    public static LibrarySingleton getInstance() {
        if (instance == null) {
            instance = new LibrarySingleton();
        }
        return instance;
    }

    /**
     * completely frees the library
     */
    public void deleteLibrary() {
        instance = null;
        songs = null;
        podcasts = null;
        listeners = null;
        albums = new ArrayList<>();
        playlists = new ArrayList<>();
        artists = new ArrayList<>();
        hosts = new ArrayList<>();
        audioPlayers = new HashMap<>();
    }

    /**
     * @param library the library in LibraryInput format
     */
    public void getInput(final LibraryInput library) {
        if (instance == null) {
            System.out.println("Instantiate first!");
            return;
        }
        songs = new ArrayList<>();
        for (SongInput songInput : library.getSongs()) {
            Song song = new Song(songInput);
            songs.add(song);
        }
        podcasts = new ArrayList<>();
        for (PodcastInput podcastInput: library.getPodcasts()) {
            Podcast podcast = new Podcast(podcastInput);
            podcasts.add(podcast);
        }
        listeners = new ArrayList<>();
        for (UserInput userInput: library.getUsers()) {
            Listener listener = new Listener(userInput);
            listeners.add(listener);
        }
    }

    /**
     * @param username the sought username
     * @return the User object with the desired name
     */
    public Listener findListenerByUsername(final String username) {
        for (Listener listener: listeners) {
            if (listener.getUsername().equals(username)) {
                return listener;
            }
        }
        return null;
    }

    /**
     * @param username the given host username
     * @return the Host object with the given name
     */
    public Host findHostByName(final String username) {
        for (Host host: hosts) {
            if (host.getUsername().equals(username)) {
                return host;
            }
        }
        return null;
    }

    /**
     * @param username the given artist username
     * @return the Artist object with the given name
     */
    public Artist findArtistByName(final String username) {
        for (Artist artist: artists) {
            if (artist.getUsername().equals(username)) {
                return artist;
            }
        }
        return null;
    }

    /**
     * @param username the given username
     * @return the User object with the given username
     */
    public User findUserByUsername(final String username) {
        User user = findListenerByUsername(username);
        if (user != null) {
            return user;
        }
        user = findArtistByName(username);
        if (user != null) {
            return user;
        }
        user = findHostByName(username);
        return user;
    }
    /**
     * @param name the sought song name
     * @return the Song object with the desired name
     */
    public Song findSongByName(final String name) {
        for (Song song: songs) {
            if (song.getName().equals(name)) {
                return song;
            }
        }
        return null;
    }

    /**
     * @param command the given command
     * @return the result formatted for output
     */
    public GetTop5SongsResult getTop5Songs(final Command command) {
        final int topSize = 5;
        GetTop5SongsResult result = new GetTop5SongsResult();
        result.setTimestamp(command.getTimestamp());

        ArrayList<String> top5Songs = songs.stream()
                .sorted(Comparator.comparingInt(Song::getLikes).reversed()).limit(topSize)
                .map(Song::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        result.setResult(top5Songs);
        return result;
    }

    /**
     * @param command the given command
     * @return the result formatted for output
     */
    public GetTop5PlaylistsResult getTop5Playlists(final Command command) {
        final int topSize = 5;
        GetTop5PlaylistsResult result = new GetTop5PlaylistsResult();
        result.setTimestamp(command.getTimestamp());

        ArrayList<String> top5Playlists = playlists.stream()
                .sorted((playlist1, playlist2) -> {
                    if (playlist1.getFollowers().size() == playlist2.getFollowers().size()) {
                        return Integer.compare(playlist1.getTimestamp(), playlist2.getTimestamp());
                    }
                    return Integer.compare(playlist2.getFollowers().size(),
                            playlist1.getFollowers().size());
                })
                .limit(topSize)
                .map(Playlist::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        result.setResult(top5Playlists);
        return result;
    }


    /**
     * @param album the given album
     * @return the total number of likes
     */
    public int computeAlbumLikeCount(final Album album) {
        int result = 0;
        for (Song song: album.getSongs()) {
            result += song.getLikes();
        }
        return result;
    }

    /**
     * @param command the given command
     * @return statistics formatted for output
     */
    public StatisticsResult getTop5Albums(final Command command) {
        final int topSize = 5;
        StatisticsResult result = new StatisticsResult(command);
        ArrayList<String> top5Albums = albums.stream()
                .sorted((album1, album2) -> {
                    int album1LikeCount = computeAlbumLikeCount(album1);
                    int album2LikeCount = computeAlbumLikeCount(album2);
                    if (album1LikeCount == album2LikeCount) {
                        return album1.getName().compareTo(album2.getName());
                    }
                    return Integer.compare(album2LikeCount, album1LikeCount);
                })
                .limit(topSize)
                .map(Album::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        result.setResult(top5Albums);
        return result;
    }


    /**
     * @param artist the given artist
     * @return cumulative number of likes
     */
    public int computeArtistLikeCount(final Artist artist) {
        int result = 0;
        for (Album album: artist.getAlbums()) {
            result += computeAlbumLikeCount(album);
        }
        return result;
    }

    /**
     * @param command the given command
     * @return statistics of the top 5 artists
     */
    public StatisticsResult getTop5Artists(final Command command) {
        final int topSize = 5;
        StatisticsResult result = new StatisticsResult(command);

        ArrayList<String> top5Artists = artists.stream()
                .sorted((artist1, artist2) -> {
                    int artist1LikeCount = computeArtistLikeCount(artist1);
                    int artist2LikeCount = computeArtistLikeCount(artist2);
                    return Integer.compare(artist2LikeCount, artist1LikeCount);
                })
                .limit(topSize)
                .map(Artist::getName)
                .collect(Collectors.toCollection(ArrayList::new));

        result.setResult(top5Artists);
        return result;
    }

    /**
     * get the username of all users
     * @param command the given command
     * @return the result formatted for output
     */
    public GetAllUsersResult getAllUsers(final Command command) {
        GetAllUsersResult result = new GetAllUsersResult();
        result.setTimestamp(command.getTimestamp());
        ArrayList<String> resultArray = result.getResult();
        for (Listener listener: listeners) {
            resultArray.add(listener.getUsername());
        }
        for (Artist artist: artists) {
            resultArray.add(artist.getUsername());
        }
        for (Host host: hosts) {
            resultArray.add(host.getUsername());
        }
        result.setResult(resultArray);
        return result;
    }

    /**
     * get the usernames of all online users
     * @param command the given command
     * @return the result formatted for output
     */
    public GetOnlineUsersResult getOnlineUsers(final Command command) {
        GetOnlineUsersResult result = new GetOnlineUsersResult();
        result.setTimestamp(command.getTimestamp());
        ArrayList<String> resultArray = result.getResult();
        for (Listener listener: listeners) {
            if (listener.isOnline()) {
                resultArray.add(listener.getUsername());
            }
        }
        result.setResult(resultArray);
        return result;
    }

    /**
     * @param username a given username
     * @return true if it is taken, false otherwise
     */
    public boolean usernameAlreadyExists(final String username) {
        for (Listener listener: listeners) {
            if (listener.getUsername().equals(username)) {
                return true;
            }
        }
        for (Artist artist: artists) {
            if (artist.getUsername().equals(username)) {
                return true;
            }
        }
        for (Host host: hosts) {
            if (host.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new User and adds it into the Library
     * @param command the given command
     * @return the result of the command formatted for output
     */
    public AddUserResult addUser(final Command command) {
        AddUserResult result = new AddUserResult();
        AudioPlayer player = audioPlayers.get(command.getUsername());
        result.setUser(command.getUsername());
        result.setTimestamp(command.getTimestamp());
        if (usernameAlreadyExists(command.getUsername())) {
            result.setMessage("The username " + command.getUsername() + " is already taken.");
            return result;
        }
        switch (command.getType()) {
            case "user" -> {
                Listener listener = new Listener(command);
                listeners.add(listener);
                player.setUser(listener);
            }
            case "artist" -> {
                Artist artist = new Artist(command);
                artists.add(artist);
                player.setUser(artist);
            }
            case "host" -> {
                Host host = new Host(command);
                hosts.add(host);
                player.setUser(host);
            }
            default -> System.err.println("Invalid user type.");
        }
        result.setMessage("The username " + command.getUsername()
                + " has been added successfully.");
        return result;
    }

    public EndResult endProgram(final Command command) {
        EndResult result = new EndResult.Builder().build();
        for (Listener listener: listeners) {
            AudioPlayer player = audioPlayers.get(listener.getUsername());
            if (player == null) {
                continue;
            }
            player.setCurrentFile(player.updateStatus(command));
            listener.splitMoney();
        }
        artists.forEach(artist -> artist.setStreamsRevenue(artist.getSongProfits().values().stream()
                .mapToDouble(Double::doubleValue).sum()));
        ArrayList<Artist> filteredArtists = artists.stream()
                .filter(artist -> artist.getPlays() > 0)
                .sorted(
                        Comparator
                                .<Artist>comparingDouble(artist -> artist.getMerchRevenue() + artist.getStreamsRevenue())
                                .reversed()
                                .thenComparing(Artist::getName)
                )
                .collect(Collectors.toCollection(ArrayList::new));
        int rank = 0;
        for (Artist artist: filteredArtists) {
            LinkedHashMap<String, Object> stats = new LinkedHashMap<>();
            stats.put("songRevenue", Math.round(artist.getStreamsRevenue() * 100.0) / 100.0);
            stats.put("merchRevenue", artist.getMerchRevenue());
            stats.put("ranking", ++rank);
            stats.put("mostProfitableSong", artist.getSongProfits().entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A"));
            result.getResult().put(artist.getName(), stats);
        }
        return result;
    }
}
