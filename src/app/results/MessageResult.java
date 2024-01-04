package app.results;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageResult extends Result {
    private String message;

    public static class Builder {
        private final int timestamp;
        private String username;
        private String message;
        private String command;

        public Builder(String command, int timestamp) {
            this.username = command;
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

        public MessageResult build() {
            return new MessageResult(this);
        }
    }

    private MessageResult(Builder builder) {
        this.message = builder.message;
        this.setTimestamp(builder.timestamp);
        this.setCommand(builder.command);
        this.setUser(builder.username);
    }
}
