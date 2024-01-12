package app.output.format_classes;

import app.audio.Playlist;
import app.audio.Song;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class PlaylistOutput {
    private int followers;
    private String name;
    private ArrayList<String> songs;
    private String visibility;

    public PlaylistOutput(final Playlist playlist) {
        followers = playlist.getFollowers().size();
        name = playlist.getName();
        visibility = playlist.getVisibility();
        songs = new ArrayList<>();
        for (Song song: playlist.getSongs()) {
            songs.add(song.getName());
        }
    }

    public void setFollowers(final int followers) {
        this.followers = followers;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setSongs(final ArrayList<String> songs) {
        this.songs = songs;
    }

    public void setVisibility(final String visibility) {
        this.visibility = visibility;
    }
}
