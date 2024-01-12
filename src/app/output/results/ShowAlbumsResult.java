package app.output.results;

import app.output.format_classes.AlbumOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class ShowAlbumsResult extends Result {
    private ArrayList<AlbumOutput> result;
    public ShowAlbumsResult() {
        super();
        setCommand("showAlbums");
    }
}
