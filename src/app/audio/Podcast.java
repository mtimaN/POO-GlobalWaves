package app.audio;

import app.persons.User;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;
import lombok.Getter;
import lombok.Setter;
import main.Command;
import app.player.AudioPlayer;
import app.player.Filter;
import app.player.Status;
import app.persons.Listener;

import java.util.ArrayList;

@Getter @Setter
public final class Podcast implements AudioItem {
    private String name;
    private String owner;
    private ArrayList<Episode> episodes;

    public Podcast(final PodcastInput input) {
        name = input.getName();
        owner = input.getOwner();
        episodes = new ArrayList<>();
        for (EpisodeInput episodeInput: input.getEpisodes()) {
            Episode episode = new Episode(episodeInput);
            episodes.add(episode);
        }
    }

    public Podcast(final ArrayList<EpisodeInput> episodes, final String name, final String owner) {
        this.name = name;
        this.owner = owner;
        this.episodes = new ArrayList<>();
        for (EpisodeInput episodeInput: episodes) {
            Episode episode = new Episode(episodeInput);
            this.episodes.add(episode);
        }
    }

    /**
     * delete the given podcast
     * NOTE: it should be checked whether deleting the podcast is safe
     */
    public void delete() {
        LibrarySingleton library = LibrarySingleton.getInstance();
        library.getPodcasts().remove(this);
    }

    /**
     * @param filters given filters
     * @return true if all the filters are met, false otherwise
     */
    public boolean matchesFilter(final Filter filters) {
        if (filters.getName() != null && !this.getName().startsWith(filters.getName())) {
            return false;
        }
        if (filters.getOwner() != null && !this.getOwner().equals(filters.getOwner())) {
            return false;
        }
        return true;
    }

    /**
     * simulates playing the episodes
     * @param player the current app.player
     * @param command the given command
     * @return the current episode
     */
    public Episode play(final AudioPlayer player, final Command command) {
        int time = player.getElapsedTime();
        Status status = player.getStatus();
        LibrarySingleton library = LibrarySingleton.getInstance();
        Listener listener = library.findListenerByUsername(command.getUsername());
        if (listener == null) {
            return null;
        }
        listener.getPodcastListenTime().put(this, time + 1);
        for (Episode episode: getEpisodes()) {
            if (time >= episode.getDuration()) {
                time -= episode.getDuration();
            } else {
                status.setName(episode.getName());
                status.setRemainedTime(episode.getDuration() - time);
                return episode;
            }
        }
        if (time >= 0) {
            AudioItem.setNullStatus(player);
        }
        return null;
    }

    /**
     * @return the name of the podcast
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public boolean isOwnedBy(final User user) {
        return user.getUsername().equals(owner);
    }

    @Override
    public boolean canBeShuffled() {
        return false;
    }

    @Override
    public boolean allowForwardBackward() {
        return true;
    }

    @Override
    public boolean isSongCollection() {
        return false;
    }

    @Override
    public boolean containsSongsFrom(final Album album) {
        return false;
    }

    @Override
    public boolean savesProgress() {
        return true;
    }
}
