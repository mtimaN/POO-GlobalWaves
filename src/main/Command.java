package main;

import app.audio.LibrarySingleton;
import app.audio.Playlist;
import app.persons.Artist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.input.EpisodeInput;
import fileio.input.SongInput;
import lombok.Getter;
import lombok.Setter;
import app.player.AudioPlayer;
import app.player.Filter;
import app.player.SearchBar;
import app.persons.Listener;

import java.util.ArrayList;

@Getter @Setter
public final class Command {
    private String command;
    private String username;
    private int timestamp;
    private String type;
    private Filter filters;
    private int itemNumber;
    private String playlistName;
    private int playlistId;
    private int seed;
    private int age;
    private String city;
    private String name;
    private String releaseYear;
    private String description;
    private ArrayList<SongInput> songs;
    private String date;
    private int price;
    private ArrayList<EpisodeInput> episodes;
    private String nextPage;
    private String recommendationType;

    /**
     * matches the type of the command and calls the fitting methods.
     * @param outputs the array of objects which is then printed to output
     * @param currentPlayer the audio player of the current user
     * @param objectMapper object used to map the output to JSON
     */
    public void execute(final ArrayNode outputs, final AudioPlayer currentPlayer,
                        final ObjectMapper objectMapper) {
        LibrarySingleton myLibrary = LibrarySingleton.getInstance();
        Listener listener;
        Artist artist;
        switch (command) {
            case "search":
                if (currentPlayer.getUser() == null) {
                    break;
                }
                currentPlayer.setSearchBar(new SearchBar(this));
                currentPlayer.emptyPlayer(this);
                outputs.add(objectMapper.valueToTree(currentPlayer.getSearchBar().search(this)));
                break;
            case "select":
                outputs.add(objectMapper.valueToTree(currentPlayer.getSearchBar().select(this)));
                if (currentPlayer.getSearchBar().getSelection() != null
                        && !currentPlayer.getSearchBar().getSelection().isPlayable()) {
                    currentPlayer.changePageOwner();
                }
                break;
            case "load":
                outputs.add(objectMapper.valueToTree(currentPlayer.load(this)));
                break;
            case "playPause":
                outputs.add(objectMapper.valueToTree(currentPlayer.playPause(this)));
                break;
            case "status":
                outputs.add(objectMapper.valueToTree(currentPlayer.status(this)));
                break;
            case "createPlaylist":
                outputs.add(objectMapper.valueToTree(Playlist.create(this)));
                break;
            case "addRemoveInPlaylist":
                outputs.add(objectMapper.valueToTree(currentPlayer.addRemoveInPlaylist(this)));
                break;
            case "like":
                outputs.add(objectMapper.valueToTree(currentPlayer.like(this)));
                break;
            case "showPreferredSongs":
                listener = myLibrary.findListenerByUsername(this.getUsername());
                if (listener == null) {
                    break;
                }
                outputs.add(objectMapper.valueToTree(listener.showPreferredSongs(this)));
                break;
            case "showPlaylists":
                listener = myLibrary.findListenerByUsername(this.getUsername());
                assert listener != null;
                outputs.add(objectMapper.valueToTree(listener.showPlaylists(this)));
                break;
            case "repeat":
                outputs.add(objectMapper.valueToTree(currentPlayer.repeat(this)));
                break;
            case "shuffle":
                outputs.add(objectMapper.valueToTree(currentPlayer.shuffle(this)));
                break;
            case "forward":
                outputs.add(objectMapper.valueToTree(currentPlayer.forward(this)));
                break;
            case "backward":
                outputs.add(objectMapper.valueToTree(currentPlayer.backward(this)));
                break;
            case "next":
                outputs.add(objectMapper.valueToTree(currentPlayer.next(this)));
                break;
            case "prev":
                outputs.add(objectMapper.valueToTree(currentPlayer.prev(this)));
                break;
            case "follow":
                outputs.add(objectMapper.valueToTree(currentPlayer.follow(this)));
                break;
            case "switchVisibility":
                listener = myLibrary.findListenerByUsername(this.getUsername());
                if (listener == null) {
                    break;
                }
                outputs.add(objectMapper.valueToTree(listener.switchVisibility(this)));
                break;
            case "getTop5Songs":
                outputs.add(objectMapper.valueToTree(myLibrary.getTop5Songs(this)));
                break;
            case "getTop5Playlists":
                outputs.add(objectMapper.valueToTree(myLibrary.getTop5Playlists(this)));
                break;
            case "getTop5Artists":
                outputs.add(objectMapper.valueToTree(myLibrary.getTop5Artists(this)));
                break;
            case "switchConnectionStatus":
                outputs.add(objectMapper.valueToTree(currentPlayer.switchConnectionStatus(this)));
                break;
            case "getAllUsers":
                outputs.add(objectMapper.valueToTree(myLibrary.getAllUsers(this)));
                break;
            case "getOnlineUsers":
                outputs.add(objectMapper.valueToTree(myLibrary.getOnlineUsers(this)));
                break;
            case "addUser":
                outputs.add(objectMapper.valueToTree(myLibrary.addUser(this)));
                break;
            case "addAlbum":
                outputs.add(objectMapper.valueToTree(currentPlayer.addAlbum(this)));
                break;
            case "showAlbums":
                artist = myLibrary.findArtistByName(username);
                if (artist == null) {
                    System.err.println("Artist does not exist.");
                    break;
                }
                outputs.add(objectMapper.valueToTree(artist.showAlbums(this)));
                break;
            case "removeAlbum":
                outputs.add(objectMapper.valueToTree(currentPlayer.removeAlbum(this)));
                break;
            case "changePage":
                outputs.add(objectMapper.valueToTree(currentPlayer.changePage(this)));
                break;
            case "printCurrentPage":
                listener = myLibrary.findListenerByUsername(username);
                if (listener == null) {
                    System.out.println("Invalid user");
                    break;
                }
                outputs.add(objectMapper.valueToTree(listener.printCurrentPage(this)));
                break;
            case "addEvent":
                outputs.add(objectMapper.valueToTree(currentPlayer.addEvent(this)));
                break;
            case "addMerch":
                outputs.add(objectMapper.valueToTree(currentPlayer.addMerch(this)));
                break;
            case "addPodcast":
                outputs.add(objectMapper.valueToTree(currentPlayer.addPodcast(this)));
                break;
            case "removePodcast":
                outputs.add(objectMapper.valueToTree(currentPlayer.removePodcast(this)));
                break;
            case "deleteUser":
                outputs.add(objectMapper.valueToTree(currentPlayer.deleteUser(this)));
                break;
            case "addAnnouncement":
                outputs.add(objectMapper.valueToTree(currentPlayer.addAnnouncement(this)));
                break;
            case "removeAnnouncement":
                outputs.add(objectMapper.valueToTree(currentPlayer.removeAnnouncement(this)));
                break;
            case "removeEvent":
                outputs.add(objectMapper.valueToTree(currentPlayer.removeEvent(this)));
                break;
            case "showPodcasts":
                outputs.add(objectMapper.valueToTree(currentPlayer.showPodcasts(this)));
                break;
            case "getTop5Albums":
                outputs.add(objectMapper.valueToTree(myLibrary.getTop5Albums(this)));
                break;
            default:
                System.err.println("Invalid command");
                break;
        }
    }
}
