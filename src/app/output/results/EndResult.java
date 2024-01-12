package app.output.results;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter @Setter
public final class EndResult {
    private String command;
    private LinkedHashMap<String, LinkedHashMap<String, Object>> result;

    public static final class Builder {
        private LinkedHashMap<String, LinkedHashMap<String, Object>> result = null;
        public Builder() {
            result = new LinkedHashMap<>();
        }

        /** */
        public EndResult build() {
            return new EndResult(this);
        }
    }

    private EndResult(final Builder builder) {
        command = "endProgram";
        this.result = builder.result;
    }

}
