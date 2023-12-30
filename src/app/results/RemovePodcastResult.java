package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RemovePodcastResult extends Result {
    private String message;

    public RemovePodcastResult() {
        super();
        super.setCommand("removePodcast");
    }
}
