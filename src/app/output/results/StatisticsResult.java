package app.output.results;

import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.ArrayList;

@Getter @Setter
public class StatisticsResult {
        private ArrayList<String> result;
        private String command;
        private int timestamp;

        public StatisticsResult(final Command command) {
            result = new ArrayList<>();
            this.command = command.getCommand();
            timestamp = command.getTimestamp();
        }
}

