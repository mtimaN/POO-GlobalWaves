package app.player;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Announcement {
    private String name;
    private String description;

    public Announcement(final String name, final String description) {
        this.name = name;
        this.description = description;
    }
}
