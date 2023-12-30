package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddPodcastResult extends Result {
    private String message;

    public AddPodcastResult() {
        super();
        super.setCommand("addPodcast");
    }
}
