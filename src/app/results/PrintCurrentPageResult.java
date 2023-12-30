package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrintCurrentPageResult extends Result {

    private String message;

    public PrintCurrentPageResult() {
        super();
        super.setCommand("printCurrentPage");
    }
}
