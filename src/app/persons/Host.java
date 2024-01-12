package app.persons;

import app.audio.Episode;
import app.audio.LibrarySingleton;
import app.audio.Podcast;
import app.player.Announcement;
import app.player.AudioPlayer;
import app.player.Filter;
import app.player.Searchable;
import app.output.results.WrappedResult;
import fileio.input.UserInput;
import lombok.Getter;
import main.Command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public final class Host extends User implements Searchable {
    private final ArrayList<Podcast> podcasts;
    private final ArrayList<Announcement> announcements;
    private ArrayList<Listener> subscribers;

    public Host(final Command command) {
        super(command);
        podcasts = new ArrayList<>();
        announcements = new ArrayList<>();
        for (Podcast podcast: LibrarySingleton.getInstance().getPodcasts()) {
            if (podcast.isOwnedBy(this)) {
                podcasts.add(podcast);
            }
        }
    }
    public Host(final UserInput user) {
        super(user);
        podcasts = new ArrayList<>();
        announcements = new ArrayList<>();
    }

    @Override
    public boolean canSwitchConnectionStatus() {
        return false;
    }

    @Override
    public boolean matchesFilter(final Filter filters) {
        return getUsername().startsWith(filters.getName());
    }

    @Override
    public String getName() {
        return getUsername();
    }

    @Override
    public boolean isPlayable() {
        return false;
    }

    @Override
    public boolean canAddArtistItems() {
        return false;
    }

    @Override
    public boolean canAddHostItems() {
        return true;
    }

    @Override
    public String delete(final Command command) {
        LibrarySingleton library = LibrarySingleton.getInstance();
        for (AudioPlayer player: library.getAudioPlayers().values()) {
            player.setCurrentFile(player.updateStatus(command));
            if (player.getCurrentItem() == null) {
                continue;
            }
            if (player.getCurrentItem().isOwnedBy(this)) {
                return getUsername() + " can't be deleted.";
            }
        }
        for (Listener listener: library.getListeners()) {
            if (listener.getCurrentPage().getPageOwner().equals(this)) {
                return getUsername() + " can't be deleted.";
            }
        }
        library.getPodcasts().removeIf(podcast -> podcast.getOwner().equals(getName()));
        library.getHosts().remove(this);
        return getUsername() + " was successfully deleted.";
    }

    @Override
    public WrappedResult wrapped(final Command command) {
        final int topSize = 5;
        WrappedResult result = new WrappedResult.Builder(this)
                .timestamp(command.getTimestamp())
                .build();

        if (result.getMessage() != null) {
            return result;
        }

        Map<String, Integer> episodeListenCounts = new HashMap<>();
        int listeners = 0;
        for (Listener listener: LibrarySingleton.getInstance().getListeners()) {
            boolean listened = false;
            for (Map.Entry<Episode, Integer> entry: listener.getEpisodeListens().entrySet()) {
                if (entry.getKey().getOwner().equals(getName())) {
                    episodeListenCounts.put(entry.getKey().getName(),
                            episodeListenCounts.getOrDefault(entry.getKey().getName(), 0)
                                    + entry.getValue());
                    listened = true;
                }
            }
            if (listened) {
                listeners++;
            }
        }
        LinkedHashMap<String, Integer> top5Episodes = episodeListenCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topSize)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        result.getResult().put("topEpisodes", top5Episodes);
        result.getResult().put("listeners", listeners);

        if (listeners == 0) {
            result.setResult(null);
            result.setMessage("No data to show for host " + getUsername() + ".");
        }
        return result;
    }

}
