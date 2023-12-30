package app.results;

import app.audio.Episode;
import app.audio.Podcast;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class PodcastOutput {
    private String name;
    private ArrayList<String> episodes;

    public PodcastOutput(final Podcast podcast) {
        name = podcast.getName();
        episodes = new ArrayList<>();
        for (Episode episode: podcast.getEpisodes()) {
            episodes.add(episode.getName());
        }
    }
}
