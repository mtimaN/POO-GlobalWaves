package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddMerchResult extends Result {
    private String message;

    public AddMerchResult() {
        super();
        super.setCommand("addMerch");
    }
}
