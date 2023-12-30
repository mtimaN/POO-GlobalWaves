package app.audio;

import app.persons.User;
import app.player.Filter;
import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.ArrayList;

@Getter @Setter
public final class Album extends SongCollection {
    private String description;

    public Album(final Command command) {
        setSongs(new ArrayList<>());
        setName(command.getName());
        setOwner(command.getUsername());
    }
    @Override
    public boolean matchesFilter(final Filter filters) {
        if (filters.getName() != null && !name.startsWith(filters.getName())) {
            return false;
        }
        if (filters.getOwner() != null && !owner.startsWith(filters.getOwner())) {
            return false;
        }
        if (filters.getDescription() != null && !description.startsWith(filters.getDescription())) {
            return false;
        }
        return true;
    }

    /**
     * deletes the Album.
     * NOTE: check whether it is safe to delete before calling this function
     */
    public void delete() {
        LibrarySingleton library = LibrarySingleton.getInstance();
        library.getAlbums().remove(this);
        for (Song song: songs) {
            library.getSongs().remove(song);
        }
        for (Playlist playlist: library.getPlaylists()) {
            playlist.getSongs().removeIf(song -> song.getAlbum().equals(name));
        }
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOwnedBy(final User user) {
        return user.getUsername().equals(owner);
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

}
