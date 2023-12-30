package app.player;

import app.audio.Album;
import app.audio.Episode;
import app.audio.LibrarySingleton;
import app.audio.Playlist;
import app.audio.Podcast;
import app.audio.Song;
import app.persons.Artist;
import app.persons.Host;
import app.persons.Listener;
import app.persons.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public final class Page {
    enum Type {
        HOME,
        LIKEDCONTENT,
        ARTIST,
        HOST
    }
    private User pageOwner;
    private Type pageType;
    private String content;
    public Page(final Listener listener) {
        changeToHome(listener);
    }

    /**
     * changes current page to home
     * @param listener the owner of the page
     */
    public void changeToHome(final Listener listener) {
        pageOwner = listener;
        pageType = Type.HOME;
    }

    /**
     * generates the home page of the user
     */
    public void generateHomePage() {
        Listener listener = (Listener) pageOwner;
        ArrayList<Song> likedSongs = new ArrayList<>(listener.getLikedSongs());
        likedSongs.sort((song1, song2) -> Integer.compare(song2.getLikes(), song1.getLikes()));
        final int maxSize = 5;
        int size = Math.min(likedSongs.size(), maxSize);
        StringBuilder contentBuilder = new StringBuilder("Liked songs:\n\t[");
        for (int i = 0; i < size; ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            contentBuilder.append(likedSongs.get(i).getName());
        }

        contentBuilder.append("]\n\nFollowed playlists:\n\t[");
        ArrayList<Playlist> followedPlaylists = new ArrayList<>();
        for (Playlist playlist: LibrarySingleton.getInstance().getPlaylists()) {
            if (playlist.getFollowers().contains(listener)) {
                followedPlaylists.add(playlist);
            }
        }
        size = Math.min(followedPlaylists.size(), maxSize);
        for (int i = 0; i < size; ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            contentBuilder.append(followedPlaylists.get(i).getName());
        }
        contentBuilder.append("]");
        content = contentBuilder.toString();
    }

    /**
     * changes page to liked content of the given listener
     * @param listener the owner of the page
     */
    public void changeToLikedContent(final Listener listener) {
        pageOwner = listener;
        pageType = Type.LIKEDCONTENT;
    }

    /**
     * generates the liked content page
     */
    public void generateLikedContent() {
        Listener listener = (Listener) pageOwner;
        ArrayList<Song> likedSongs = listener.getLikedSongs();
        StringBuilder contentBuilder = new StringBuilder("Liked songs:\n\t[");
        for (int i = 0; i < likedSongs.size(); ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            contentBuilder.append(likedSongs.get(i).getName())
                    .append(" - ").append(likedSongs.get(i).getArtist());
        }

        contentBuilder.append("]\n\nFollowed playlists:\n\t[");

        ArrayList<Playlist> followedPlaylists = new ArrayList<>();
        for (Playlist playlist: LibrarySingleton.getInstance().getPlaylists()) {
            if (playlist.getFollowers().contains(listener)) {
                followedPlaylists.add(playlist);
            }
        }
        for (int i = 0; i < followedPlaylists.size(); ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            contentBuilder.append(followedPlaylists.get(i).getName()).append(" - ")
                    .append(followedPlaylists.get(i).getOwner());
        }
        contentBuilder.append("]");
        content = contentBuilder.toString();
    }

    /**
     * changes page to that of the given artist
     * @param artist the owner of the page
     */
    public void changeToArtist(final Artist artist) {
        pageOwner = artist;
        pageType = Type.ARTIST;
    }

    /**
     * generates the artist page
     */
    public void generateArtistPage() {
        Artist artist = (Artist) pageOwner;
        StringBuilder contentBuilder = new StringBuilder("Albums:\n\t[");
        ArrayList<Album> albums = artist.getAlbums();
        for (int i = 0; i < albums.size(); ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            contentBuilder.append(albums.get(i).getName());
        }
        contentBuilder.append("]\n\nMerch:\n\t[");
        ArrayList<Merch> merchItems = artist.getMerchItems();
        for (int i = 0; i < merchItems.size(); ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            contentBuilder.append(merchItems.get(i).getName()).append(" - ")
                    .append(merchItems.get(i).getPrice()).append(":\n\t")
                    .append(merchItems.get(i).getDescription());
        }
        contentBuilder.append("]\n\nEvents:\n\t[");
        ArrayList<Event> events = artist.getEvents();
        for (int i = 0; i < events.size(); ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            contentBuilder.append(events.get(i).getName()).append(" - ")
                    .append(events.get(i).getDate()).append(":\n\t")
                    .append(events.get(i).getDescription());
        }
        contentBuilder.append("]");
        content = contentBuilder.toString();
    }

    /**
     * changes the page to the one of the given host
     * @param host the owner of the page
     */
    public void changeToHost(final Host host) {
        pageOwner = host;
        pageType = Type.HOST;
    }

    /**
     * generates the page of a host
     */
    public void generateHostPage() {
        Host host = (Host) pageOwner;
        StringBuilder contentBuilder = new StringBuilder("Podcasts:\n\t[");
        ArrayList<Podcast> podcasts = host.getPodcasts();
        for (int i = 0; i < podcasts.size(); ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            Podcast podcast = podcasts.get(i);
            contentBuilder.append(podcast.getName()).append(":\n\t[");
            ArrayList<Episode> episodes = podcast.getEpisodes();

            for (int j = 0; j < episodes.size(); ++j) {
                if (j != 0) {
                    contentBuilder.append(", ");
                }
                Episode episode = episodes.get(j);
                contentBuilder.append(episode.getName()).append(" - ")
                        .append(episode.getDescription());
            }
            contentBuilder.append("]\n");
        }
        contentBuilder.append("]\n\nAnnouncements:\n\t[");
        ArrayList<Announcement> announcements = host.getAnnouncements();
        for (int i = 0; i < announcements.size(); ++i) {
            if (i != 0) {
                contentBuilder.append(", ");
            }
            contentBuilder.append(announcements.get(i).getName()).append(":\n\t")
                    .append(announcements.get(i).getDescription()).append("\n");
        }
        contentBuilder.append("]");
        content = contentBuilder.toString();
    }

    /**
     * checks the page format and generates it
     */
    public void generateCurrentPage() {
        switch (pageType) {
            case HOME -> generateHomePage();
            case LIKEDCONTENT -> generateLikedContent();
            case ARTIST -> generateArtistPage();
            case HOST -> generateHostPage();
            default -> System.err.println("Unrecognized page type");
        }
    }
}
