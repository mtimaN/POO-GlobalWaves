package app.audio;

import fileio.input.EpisodeInput;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Episode extends AudioFile {
    private String description;

    public Episode(final EpisodeInput input) {
        this.setName(input.getName());
        this.setDuration(input.getDuration());
        this.setDescription(input.getDescription());
    }

    @Override
    public boolean isSong() {
        return false;
    }
}
