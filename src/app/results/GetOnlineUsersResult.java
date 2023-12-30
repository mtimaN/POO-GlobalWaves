package app.results;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class GetOnlineUsersResult {
    private ArrayList<String> result;
    private String command;
    private int timestamp;

    public GetOnlineUsersResult() {
        command = "getOnlineUsers";
        result = new ArrayList<>();
    }
}
