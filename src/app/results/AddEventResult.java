package app.results;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class AddEventResult extends Result {
    private String message;

    public AddEventResult() {
        super();
        super.setCommand("addEvent");
    }
}
