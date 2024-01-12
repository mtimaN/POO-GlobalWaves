package app.results;

import app.player.Status;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import app.player.Notification;

@Getter @Setter
public class GeneralResult extends Result {
    private String message;
    private ArrayList<Notification> notifications;
    private ArrayList<String> result;
    private Status stats;

    public static class Builder {
        private final int timestamp;
        private String username;
        private String message;
        private String command;
        private ArrayList<Notification> notifications;
        private Status stats;

        private ArrayList<String> result;

        public Builder(String command, int timestamp) {
            this.command = command;
            this.timestamp = timestamp;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder notifications() {
            notifications = new ArrayList<>();
            return this;
        }

        public Builder stats(Status status) {
            stats = status;
            return this;
        }

        public Builder result() {
            result = new ArrayList<>();
            return this;
        }

        public GeneralResult build() {
            return new GeneralResult(this);
        }
    }

    private GeneralResult(Builder builder) {
        this.message = builder.message;
        this.setTimestamp(builder.timestamp);
        this.setCommand(builder.command);
        this.setUser(builder.username);
        this.setNotifications(builder.notifications);
        this.setResult(builder.result);
    }
}
