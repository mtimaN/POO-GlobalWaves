package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RemoveEventResult extends Result {
    private String message;

    public RemoveEventResult() {
        super();
        super.setCommand("removeEvent");
    }
}
