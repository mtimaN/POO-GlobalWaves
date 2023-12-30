package app.results;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class GetAllUsersResult {
    private ArrayList<String> result;
    private String command;
    private int timestamp;

    public GetAllUsersResult() {
        command = "getAllUsers";
        result = new ArrayList<>();
    }
}
