package app.results;

import app.persons.Artist;
import app.persons.Host;
import app.persons.Listener;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class WrappedResult extends Result {
    private String message;
    private Map<String, Map<String, Integer>> result;

    public static class Builder {
        private int timestamp;
        private final String username;
        private String message = null;
        private Map<String, Map<String, Integer>> result = null;
        public Builder(Listener listener) {
            username = listener.getUsername();
            if (listener.getSongListens().isEmpty()) {
                message = "No data to show for user " + listener.getUsername() + ".";
            } else {
                result = new LinkedHashMap<>();
            }
        }

        public Builder(Artist artist) {
            username = artist.getUsername();
            if (artist.getAlbums().isEmpty()) {
                message = "No data to show for user " + artist.getUsername() + ".";
            } else {
                result = new LinkedHashMap<>();
            }
        }

        public Builder(Host host) {
            username = host.getUsername();
            if (host.getPodcasts().isEmpty()) {
                message = "No data to show for user " + host.getUsername() + ".";
            } else {
                result = new LinkedHashMap<>();
            }
        }

        public Builder timestamp(int timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public WrappedResult build() {
            return new WrappedResult(this);
        }
    }

    private WrappedResult(Builder builder) {
        this.message = builder.message;
        this.result = builder.result;
        this.setTimestamp(builder.timestamp);
        this.setCommand("wrapped");
        this.setUser(builder.username);
    }
}
