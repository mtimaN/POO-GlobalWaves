package app.output.results;

import app.persons.Artist;
import app.persons.Host;
import app.persons.Listener;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Setter
public final class WrappedResult extends Result {
    private String message;
    private Map<String, Object> result;

    public static final class Builder {
        private int timestamp;
        private final String username;
        private String message = null;
        private Map<String, Object> result = null;
        public Builder(final Listener listener) {
            username = listener.getUsername();
            if (listener.getSongListens().isEmpty() && listener.getEpisodeListens().isEmpty()) {
                message = "No data to show for user " + listener.getUsername() + ".";
            } else {
                result = new LinkedHashMap<>();
            }
        }

        public Builder(final Artist artist) {
            username = artist.getUsername();
            if (artist.getAlbums().isEmpty()) {
                message = "No data to show for artist " + artist.getUsername() + ".";
            } else {
                result = new LinkedHashMap<>();
            }
        }

        public Builder(final Host host) {
            username = host.getUsername();
            if (host.getPodcasts().isEmpty()) {
                message = "No data to show for host " + host.getUsername() + ".";
            } else {
                result = new LinkedHashMap<>();
            }
        }

        /** */
        public Builder timestamp(final int givenTimestamp) {
            this.timestamp = givenTimestamp;
            return this;
        }

        /** */
        public WrappedResult build() {
            return new WrappedResult(this);
        }
    }

    private WrappedResult(final Builder builder) {
        this.message = builder.message;
        this.result = builder.result;
        this.setTimestamp(builder.timestamp);
        this.setCommand("wrapped");
        this.setUser(builder.username);
    }
}
