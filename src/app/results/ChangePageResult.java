package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePageResult extends Result {
    private String message;

    public ChangePageResult() {
        super();
        super.setCommand("changePage");
    }
}
