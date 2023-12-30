package app.results;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class ShowPodcastsResult extends Result {
    private ArrayList<PodcastOutput> result;

    public ShowPodcastsResult() {
        super();
        result = new ArrayList<>();
        setCommand("showPodcasts");
    }
}
