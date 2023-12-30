package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddAlbumResult extends Result {
    private String message;

    public AddAlbumResult() {
        super();
        super.setCommand("addAlbum");
    }
}
