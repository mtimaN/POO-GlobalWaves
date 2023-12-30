package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RemoveAlbumResult extends Result {
    private String message;

    public RemoveAlbumResult() {
        super();
        super.setCommand("removeAlbum");
    }
}
