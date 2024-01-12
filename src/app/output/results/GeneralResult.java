package app.output.results;

import app.player.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import app.player.Notification;

@Getter @Setter
public final class GeneralResult extends Result {
    private String message;
    private ArrayList<Notification> notifications;
    private ArrayList<String> result;
    private Status stats;

    public static final class Builder {
        private final int timestamp;
        private String username;
        private String message;
        private final String command;
        private ArrayList<Notification> notifications;
        private Status stats;
        private ArrayList<String> result;

        public Builder(final String command, final int timestamp) {
            this.command = command;
            this.timestamp = timestamp;
        }

        /** */
        public Builder username(final String givenUsername) {
            this.username = givenUsername;
            return this;
        }

        /** */
        public Builder message(final String givenMessage) {
            this.message = givenMessage;
            return this;
        }

        /** */
        public Builder notifications() {
            notifications = new ArrayList<>();
            return this;
        }

        /** */
        public Builder stats(final Status status) {
            stats = status;
            return this;
        }

        /** */
        public Builder result() {
            result = new ArrayList<>();
            return this;
        }

        /** */
        public GeneralResult build() {
            return new GeneralResult(this);
        }
    }

    private GeneralResult(final Builder builder) {
        this.message = builder.message;
        this.setTimestamp(builder.timestamp);
        this.setCommand(builder.command);
        this.setUser(builder.username);
        this.setNotifications(builder.notifications);
        this.setResult(builder.result);
        this.setStats(builder.stats);
    }
}
