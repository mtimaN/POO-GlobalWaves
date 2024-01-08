package app.audio;

import fileio.input.EpisodeInput;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Episode extends AudioFile {
    private String description;
    private String owner;

    public Episode(final EpisodeInput input, final String owner) {
        this.setName(input.getName());
        this.setDuration(input.getDuration());
        description = input.getDescription();
        this.owner = owner;
    }

    @Override
    public boolean isSong() {
        return false;
    }
}
