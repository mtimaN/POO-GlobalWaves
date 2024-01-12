package app.persons;

import app.audio.*;
import app.player.AudioPlayer;
import app.player.Page;
import app.results.*;
import fileio.input.UserInput;
import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

@Getter @Setter
public final class Listener extends User {

    private final ArrayList<Playlist> playlists;
    private final ArrayList<Song> likedSongs;
    private final HashMap<Podcast, Integer> podcastListenTime;
    private final HashMap<Song, Integer> songListens;
    private final HashMap<Episode, Integer> episodeListens;
    private HashMap<Song, Integer> premiumSongsRevenueShare;
    private HashMap<Song, Integer> adsSongsRevenueShare;
    private ArrayList<String> boughtMerch;
    private double revenue;
    private int premiumRevenueSongs;
    private int adsRevenueSongs;
    private boolean online;
    private Page currentPage;
    private boolean premium;
    private ArrayList<Song> songRecommendations;
    private ArrayList<Playlist> playlistRecommendations;
    private AudioItem recommendation;
    private Stack<Page.Memento> previousPages;
    private Stack<Page.Memento> nextPages;

    public Listener(final Command command) {
        super(command);
        playlists = new ArrayList<>();
        likedSongs = new ArrayList<>();

        podcastListenTime = new HashMap<>();
        songListens = new HashMap<>();
        episodeListens = new HashMap<>();
        premiumSongsRevenueShare = new HashMap<>();
        adsSongsRevenueShare = new HashMap<>();
        premiumRevenueSongs = 0;
        adsRevenueSongs = 0;

        boughtMerch = new ArrayList<>();
        songRecommendations = new ArrayList<>();
        playlistRecommendations = new ArrayList<>();

        previousPages = new Stack<>();
        nextPages = new Stack<>();
        online = true;
        premium = false;
        currentPage = new Page(this);
    }
    public Listener(final UserInput input) {
        super(input);
        this.playlists = new ArrayList<>();
        this.likedSongs = new ArrayList<>();

        podcastListenTime = new HashMap<>();
        songListens = new HashMap<>();
        episodeListens = new HashMap<>();
        premiumSongsRevenueShare = new HashMap<>();
        adsSongsRevenueShare = new HashMap<>();
        premiumRevenueSongs = 0;
        adsRevenueSongs = 0;

        boughtMerch = new ArrayList<>();
        songRecommendations = new ArrayList<>();
        playlistRecommendations = new ArrayList<>();

        previousPages = new Stack<>();
        nextPages = new Stack<>();
        online = true;
        premium = false;
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
            try {
                if (((Playlist) player.getCurrentItem()).getOwner().equals(getUsername())) {
                    return getUsername() + " can't be deleted.";
                }
            } catch (Exception ignored) {}
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

    public void addToSongListens(Song song, int listens) {
        Artist artist = LibrarySingleton.getInstance().findArtistByName(song.getArtist());
        if (artist != null) {
            artist.setPlays(artist.getPlays() + listens);
        }
        if (premium) {
            premiumSongsRevenueShare
                    .put(song, premiumSongsRevenueShare.getOrDefault(song, 0) + listens);
            premiumRevenueSongs += listens;
        } else {
            adsSongsRevenueShare
                    .put(song, adsSongsRevenueShare.getOrDefault(song, 0) + listens);
            adsRevenueSongs += listens;
        }

        songListens.put(song, songListens.getOrDefault(song, 0) + listens);
    }

    public void addToEpisodeListens(Episode episode, int listens) {
        episodeListens.put(episode, episodeListens.getOrDefault(episode, 0) + listens);
    }

    @Override
    public WrappedResult wrapped(Command command) {
        WrappedResult result = new WrappedResult.Builder(this)
                .timestamp(command.getTimestamp())
                .build();
        if (result.getMessage() != null) {
            return result;
        }

        HashMap<String, Integer> artistListenCounts = new HashMap<>();
        HashMap<String, Integer> genreListenCounts = new HashMap<>();
        HashMap<String, Integer> albumListenCounts = new HashMap<>();

        for (Map.Entry<Song, Integer> entry : songListens.entrySet()) {
            String artist = entry.getKey().getArtist();
            String genre = entry.getKey().getGenre();
            String album = entry.getKey().getAlbum();
            int listens = entry.getValue();
            artistListenCounts.put(artist, artistListenCounts.getOrDefault(artist, 0) + listens);
            genreListenCounts.put(genre, genreListenCounts.getOrDefault(genre, 0) + listens);
            albumListenCounts.put(album, albumListenCounts.getOrDefault(album, 0) + listens);
        }

        LinkedHashMap<String, Integer> top5Artists = artistListenCounts.entrySet().stream()
                .sorted(Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Entry.comparingByKey()))
                .limit(5)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        result.getResult().put("topArtists", top5Artists);

        LinkedHashMap<String, Integer> top5Genres = genreListenCounts.entrySet().stream()
                .sorted(Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Entry.comparingByKey()))
                .limit(5)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        result.getResult().put("topGenres", top5Genres);

        Map<String, Integer> top5Songs = songListens.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getName(),
                        Entry::getValue,
                        Integer::sum,
                        LinkedHashMap::new
                ))
                .entrySet().stream()
                .sorted(Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Entry.comparingByKey()))
                .limit(5)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        Integer::sum,
                        LinkedHashMap::new
                ));

        result.getResult().put("topSongs", top5Songs);

        LinkedHashMap<String, Integer> top5Albums = albumListenCounts.entrySet().stream()
                .sorted(Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Entry.comparingByKey()))
                .limit(5)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        result.getResult().put("topAlbums", top5Albums);

        Map<String, Integer> top5Episodes = episodeListens.entrySet().stream()
                .sorted(Entry.<Episode, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Entry.comparingByKey(Comparator.comparing(Episode::getName))))
                .limit(5)
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getName(),
                        Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        result.getResult().put("topEpisodes", top5Episodes);
        return result;
    }

    public void splitMoney() {
        HashMap<Song, Integer> songsRevenueShare = premium ?
                premiumSongsRevenueShare : adsSongsRevenueShare;
        int revenueSongs = premium ? premiumRevenueSongs : adsRevenueSongs;
        songsRevenueShare.forEach((song, share) -> {
            double songRevenue = revenue * share / revenueSongs;
            Artist artist = LibrarySingleton.getInstance().findArtistByName(song.getArtist());

            if (artist != null) {
                artist.getSongProfits().merge(song.getName(), songRevenue, Double::sum);
            }
        });
        songsRevenueShare.clear();

        if (premium) {
            premiumRevenueSongs = 0;
        } else {
            adsRevenueSongs = 0;
        }
        revenue = 0;
    }

    public boolean isSubscribedToPageOwner() {
        if (currentPage.getPageType() == Page.PageType.ARTIST) {
            Artist artist = (Artist) currentPage.getPageOwner();
            return artist.getSubscribers().contains(this);
        } else {
            Host host = (Host) currentPage.getPageOwner();
            return host.getSubscribers().contains(this);
        }
    }

    public void subscribeToPageOwner() {
        if (currentPage.getPageType() == Page.PageType.ARTIST) {
            Artist artist = (Artist) currentPage.getPageOwner();
            artist.getSubscribers().add(this);
        } else {
            Host host = (Host) currentPage.getPageOwner();
            host.getSubscribers().add(this);
        }
    }

    public void unsubscribeFromPageOwner() {
        if (currentPage.getPageType() == Page.PageType.ARTIST) {
            Artist artist = (Artist) currentPage.getPageOwner();
            artist.getSubscribers().remove(this);
        } else {
            Host host = (Host) currentPage.getPageOwner();
            host.getSubscribers().remove(this);
        }
    }

}
