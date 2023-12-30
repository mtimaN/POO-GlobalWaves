package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RemoveAnnouncementResult extends Result {
    private String message;

    public RemoveAnnouncementResult() {
        super();
        super.setCommand("removeAnnouncement");
    }
}
