package app.audio;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class AudioFile {
    protected String name;
    protected int duration;

    /**
     * @return true if it a song
     */
    public abstract boolean isSong();
}
