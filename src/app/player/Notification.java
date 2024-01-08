package app.player;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Notification {
    private String name;
    private String description;

    public Notification(final String name, final String description) {
        this.name = name;
        this.description = description;
    }
}
