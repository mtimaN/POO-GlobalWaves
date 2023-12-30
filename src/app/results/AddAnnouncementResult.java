package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddAnnouncementResult extends Result {

    private String message;

    public AddAnnouncementResult() {
        super();
        super.setCommand("addAnnouncement");
    }
}
