package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SwitchConnectionStatusResult extends Result {
    private String message;

    public SwitchConnectionStatusResult() {
        super();
        super.setCommand("switchConnectionStatus");
    }
}
