package app.persons;

import app.audio.LibrarySingleton;
import app.audio.Podcast;
import app.player.Announcement;
import app.player.AudioPlayer;
import app.player.Filter;
import app.player.Searchable;
import fileio.input.UserInput;
import lombok.Getter;
import main.Command;

import java.util.ArrayList;

@Getter
public final class Host extends User implements Searchable {
    private final ArrayList<Podcast> podcasts;
    private final ArrayList<Announcement> announcements;

    public Host(final Command command) {
        super(command);
        podcasts = new ArrayList<>();
        announcements = new ArrayList<>();
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
}
