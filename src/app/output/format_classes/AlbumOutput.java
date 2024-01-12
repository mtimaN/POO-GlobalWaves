package app.output.format_classes;

import app.audio.Album;
import app.audio.Song;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class AlbumOutput {
    private String name;
    private ArrayList<String> songs;
    public AlbumOutput(final Album album) {
        name = album.getName();
        songs = new ArrayList<>();
        for (Song song: album.getSongs()) {
            songs.add(song.getName());
        }
    }
}
