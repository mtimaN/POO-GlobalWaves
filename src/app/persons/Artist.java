package app.persons;

import app.audio.Album;
import app.audio.LibrarySingleton;
import app.audio.Song;
import app.audio.Playlist;
import app.player.AudioPlayer;
import app.player.Merch;
import app.player.Event;
import app.player.Searchable;
import app.player.Filter;
import app.output.format_classes.AlbumOutput;
import app.output.results.ShowAlbumsResult;
import app.output.results.WrappedResult;
import fileio.input.UserInput;
import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter @Setter
public final class Artist extends User implements Searchable {
    private ArrayList<Album> albums;
    private ArrayList<Event> events;
    private ArrayList<Merch> merchItems;
    private double merchRevenue;
    private double streamsRevenue;
    private HashMap<String, Double> songProfits;
    private float plays;
    private ArrayList<Listener> subscribers;

    public Artist(final Command command) {
        super(command);
        merchRevenue = 0;
        plays = 0;
        subscribers = new ArrayList<>();
        albums = new ArrayList<>();
        events = new ArrayList<>();
        merchItems = new ArrayList<>();
        songProfits = new HashMap<>();
    }
    public Artist(final UserInput user) {
        super(user);
        merchRevenue = 0;
        plays = 0;
        subscribers = new ArrayList<>();
        albums = new ArrayList<>();
        events = new ArrayList<>();
        merchItems = new ArrayList<>();
        songProfits = new HashMap<>();
    }

    /**
     * @param command the given command
     * @return result formatted for output
     */
    public ShowAlbumsResult showAlbums(final Command command) {
        ShowAlbumsResult result = new ShowAlbumsResult();
        result.setTimestamp(command.getTimestamp());
        result.setUser(command.getUsername());
        ArrayList<AlbumOutput> outputs = new ArrayList<>();
        for (Album album: albums) {
            AlbumOutput output = new AlbumOutput(album);
            outputs.add(output);
        }
        result.setResult(outputs);
        return result;
    }

    /**
     * @param name the name of the merch
     * @return true if merch with given name already exists
     */
    public boolean hasMerchWithName(final String name) {
        for (Merch merch: merchItems) {
            if (merch.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canSwitchConnectionStatus() {
        return false;
    }

    @Override
    public boolean matchesFilter(final Filter filters) {
        return getUsername().startsWith(filters.getName());
    }

    @Override
    public String getName() {
        return getUsername();
    }

    @Override
    public boolean isPlayable() {
        return false;
    }

    @Override
    public boolean canAddArtistItems() {
        return true;
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
            if (player.getCurrentItem().isOwnedBy(this)) {
                return getUsername() + " can't be deleted.";
            }
        }
        for (Listener listener: library.getListeners()) {
            if (listener.getCurrentPage().getPageOwner().equals(this)) {
                return getUsername() + " can't be deleted.";
            }
        }

        for (Album album: albums) {
            for (Song song: album.getSongs()) {
                library.getSongs().remove(song);
            }
            library.getAlbums().remove(album);
        }
        for (Listener listener: library.getListeners()) {
            listener.getLikedSongs().removeIf(song -> song.getArtist().equals(getName()));

            for (Playlist playlist: listener.getPlaylists()) {
                playlist.getSongs().removeIf(song -> song.getArtist().equals(getName()));
            }
        }
        library.getArtists().remove(this);
        return getUsername() + " was successfully deleted.";
    }

    @Override
    public WrappedResult wrapped(final Command command) {
        final int topSize = 5;
        WrappedResult result = new WrappedResult.Builder(this)
                .timestamp(command.getTimestamp())
                .build();

        if (result.getMessage() != null) {
            return result;
        }

        HashMap<String, AudioPlayer> audioPlayers =
                LibrarySingleton.getInstance().getAudioPlayers();
        Map<String, Integer> albumListenCounts = new HashMap<>();
        Map<String, Integer> songListenCounts = new HashMap<>();
        Map<String, Integer> fansListenCounts = new HashMap<>();
        for (Listener listener: LibrarySingleton.getInstance().getListeners()) {
            int listenCounter = 0;
            if (audioPlayers.containsKey(listener.getUsername())) {
                AudioPlayer player = audioPlayers.get(listener.getUsername());
                player.setCurrentFile(player.updateStatus(command));
            }

            for (Map.Entry<Song, Integer> entry: listener.getSongListens().entrySet()) {
                if (entry.getKey().getArtist().equals(getUsername())) {
                    String songName = entry.getKey().getName();
                    String albumName = entry.getKey().getAlbum();
                    int listens = entry.getValue();
                    listenCounter += listens;
                    albumListenCounts.put(albumName,
                            albumListenCounts.getOrDefault(albumName, 0) + listens);
                    songListenCounts.put(songName,
                            songListenCounts.getOrDefault(songName, 0) + listens);
                }
            }
            if (listenCounter > 0) {
                fansListenCounts.put(listener.getUsername(), listenCounter);
            }
        }

        LinkedHashMap<String, Integer> top5Albums = albumListenCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topSize)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        result.getResult().put("topAlbums", top5Albums);

        LinkedHashMap<String, Integer> top5Songs = songListenCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topSize)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        result.getResult().put("topSongs", top5Songs);


        ArrayList<String> top5Fans = fansListenCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topSize)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));

        result.getResult().put("topFans", top5Fans);

        result.getResult().put("listeners", fansListenCounts.size());

        if (fansListenCounts.isEmpty()) {
            result.setResult(null);
            result.setMessage("No data to show for artist " + getUsername() + ".");
        }
        return result;
    }

}
