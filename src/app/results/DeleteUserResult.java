package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeleteUserResult extends Result {
    private String message;

    public DeleteUserResult() {
        super();
        super.setCommand("deleteUser");
    }
}
