package app.player;

import lombok.Getter;
import lombok.Setter;
import main.Command;

@Getter @Setter
public final class Merch {
    private String name;
    private String description;
    private int price;

    public Merch(final Command command) {
       name = command.getName();
       description = command.getDescription();
       price = command.getPrice();
    }
}
