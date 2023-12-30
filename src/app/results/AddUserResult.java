package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddUserResult extends Result {
    private String message;

    public AddUserResult() {
        super();
        super.setCommand("addUser");
    }
}
