package app.player;

import app.audio.LibrarySingleton;
import app.audio.Playlist;
import app.persons.Listener;
import lombok.Getter;
import lombok.Setter;
import main.Command;
import app.results.SearchResult;
import app.results.SelectResult;

import java.util.ArrayList;

@Getter @Setter
public final class SearchBar {
    private String type;
    private Filter filters;

    private ArrayList<String> results = null;
    private ArrayList<Searchable> resultSearchable = null;
    private Searchable selection = null;

    public SearchBar() {
        type = null;
        filters = null;
        results = null;
    }

    public SearchBar(final Command command) {
        if (command == null) {
            return;
        }
        type = command.getType();
        filters = command.getFilters();
    }

    void fetchSearchResults(final Command command) {
        final int maxSize = 5;
        ArrayList<Searchable> searchables;
        LibrarySingleton library = LibrarySingleton.getInstance();
        resultSearchable = new ArrayList<>();
        Listener searchListener = library.findListenerByUsername(command.getUsername());
        if (searchListener == null) {
            System.err.println("User doesn't exist.");
            return;
        }
        searchables = switch (type) {
            case "song" -> new ArrayList<>(library.getSongs());
            case "playlist" -> new ArrayList<>(searchListener.getPlaylists());
            case "artist" -> new ArrayList<>(library.getArtists());
            case "host" -> new ArrayList<>(library.getHosts());
            case "album" -> new ArrayList<>(library.getAlbums());
            default -> new ArrayList<>(library.getPodcasts());
        };
        results = new ArrayList<>();
        for (Searchable searchable : searchables) {
            if (searchable.matchesFilter(filters)) {
                results.add(searchable.getName());
                resultSearchable.add(searchable);
            }

            if (results.size() == maxSize) {
                return;
            }
        }
        if (type.equals("playlist")) {
            for (Listener listener : library.getListeners()) {
                if (listener.getUsername().equals(command.getUsername())) {
                    continue;
                }
                for (Playlist playlist : listener.getPlaylists()) {
                    if (playlist.getVisibility().equals("public")
                            && playlist.matchesFilter(filters)) {
                        results.add(playlist.getName());
                        resultSearchable.add(playlist);
                    }
                    if (results.size() == maxSize) {
                        return;
                    }
                }
            }
        }
    }


    /**
     * returns the result of a search
     *
     * @param command the given command
     * @return result formatted for output
     */
    public SearchResult search(final Command command) {
        SearchResult searchResult = new SearchResult();
        searchResult.setUser(command.getUsername());
        searchResult.setTimestamp(command.getTimestamp());

        Listener listener = LibrarySingleton.getInstance()
                .findListenerByUsername(command.getUsername());
        if (listener == null) {
            return searchResult;
        }
        if (!listener.isOnline()) {
            searchResult.setMessage(listener.getUsername() + " is offline.");
            searchResult.setResults(new ArrayList<>());
            return searchResult;
        }

        fetchSearchResults(command);
        if (results == null) {
            System.out.println("No results found!");
            return searchResult;
        }
        searchResult.setMessage("Search returned " + results.size() + " results");
        searchResult.setResults(results);

        return searchResult;
    }

    /**
     * selects an item
     *
     * @param command the given command
     * @return the result formatted for output
     */
    public SelectResult select(final Command command) {
        SelectResult selectResult = new SelectResult();
        selectResult.setUser(command.getUsername());
        selectResult.setTimestamp(command.getTimestamp());
        if (results == null) {
            selectResult.setMessage("Please conduct a search before making a selection.");
        } else if (command.getItemNumber() > results.size()) {
            selectResult.setMessage("The selected ID is too high.");
        } else {
            selection = resultSearchable.get(command.getItemNumber() - 1);
            if (selection.isPlayable()) {
                selectResult.setMessage("Successfully selected "
                                        + results.get(command.getItemNumber() - 1) + ".");
            } else {
                selectResult.setMessage("Successfully selected "
                        + results.get(command.getItemNumber() - 1) + "'s page.");
            }
            results = null;
        }
        return selectResult;
    }
}
