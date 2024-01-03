package app.results;

import app.persons.Artist;
import app.persons.Host;
import app.persons.Listener;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Getter @Setter
public class EndResult {
    private String command;
    private LinkedHashMap<String, LinkedHashMap<String, Object>> result;

    public static class Builder {
        private LinkedHashMap<String, LinkedHashMap<String, Object>> result = null;
        public Builder() {
            result = new LinkedHashMap<>();
        }

        public EndResult build() {
            return new EndResult(this);
        }
    }

    private EndResult(EndResult.Builder builder) {
        command = "endProgram";
        this.result = builder.result;
    }

}
