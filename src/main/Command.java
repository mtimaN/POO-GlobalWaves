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
            case "search" -> {
                if (currentPlayer.getUser() == null) {
                    break;
                }
                currentPlayer.setCurrentFile(currentPlayer.updateStatus(this));
                currentPlayer.setSearchBar(new SearchBar(this));
                currentPlayer.emptyPlayer(this);
                outputs.add(objectMapper.valueToTree(currentPlayer.getSearchBar().search(this)));
            }
            case "select" -> {
                outputs.add(objectMapper.valueToTree(currentPlayer.getSearchBar().select(this)));
                if (currentPlayer.getSearchBar().getSelection() != null
                        && !currentPlayer.getSearchBar().getSelection().isPlayable()) {
                    currentPlayer.changePageOwner();
                }
            }
            case "load" -> outputs.add(objectMapper.valueToTree(currentPlayer.load(this)));
            case "playPause" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.playPause(this)));
            case "status" -> outputs.add(objectMapper.valueToTree(currentPlayer.status(this)));
            case "createPlaylist" -> outputs.add(objectMapper.valueToTree(Playlist.create(this)));
            case "addRemoveInPlaylist" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.addRemoveInPlaylist(this)));
            case "like" -> outputs.add(objectMapper.valueToTree(currentPlayer.like(this)));
            case "showPreferredSongs" -> {
                listener = myLibrary.findListenerByUsername(this.getUsername());
                if (listener == null) {
                    break;
                }
                outputs.add(objectMapper.valueToTree(listener.showPreferredSongs(this)));
            }
            case "showPlaylists" -> {
                listener = myLibrary.findListenerByUsername(this.getUsername());
                assert listener != null;
                outputs.add(objectMapper.valueToTree(listener.showPlaylists(this)));
            }
            case "repeat" -> outputs.add(objectMapper.valueToTree(currentPlayer.repeat(this)));
            case "shuffle" -> outputs.add(objectMapper.valueToTree(currentPlayer.shuffle(this)));
            case "forward" -> outputs.add(objectMapper.valueToTree(currentPlayer.forward(this)));
            case "backward" -> outputs.add(objectMapper.valueToTree(currentPlayer.backward(this)));
            case "next" -> outputs.add(objectMapper.valueToTree(currentPlayer.next(this)));
            case "prev" -> outputs.add(objectMapper.valueToTree(currentPlayer.prev(this)));
            case "follow" -> outputs.add(objectMapper.valueToTree(currentPlayer.follow(this)));
            case "switchVisibility" -> {
                listener = myLibrary.findListenerByUsername(this.getUsername());
                if (listener == null) {
                    break;
                }
                outputs.add(objectMapper.valueToTree(listener.switchVisibility(this)));
            }
            case "getTop5Songs" ->
                    outputs.add(objectMapper.valueToTree(myLibrary.getTop5Songs(this)));
            case "getTop5Playlists" ->
                    outputs.add(objectMapper.valueToTree(myLibrary.getTop5Playlists(this)));
            case "getTop5Artists" ->
                    outputs.add(objectMapper.valueToTree(myLibrary.getTop5Artists(this)));
            case "switchConnectionStatus" ->
                    outputs.add(objectMapper
                            .valueToTree(currentPlayer.switchConnectionStatus(this)));
            case "getAllUsers" ->
                    outputs.add(objectMapper.valueToTree(myLibrary.getAllUsers(this)));
            case "getOnlineUsers" ->
                    outputs.add(objectMapper.valueToTree(myLibrary.getOnlineUsers(this)));
            case "addUser" -> outputs.add(objectMapper.valueToTree(myLibrary.addUser(this)));
            case "addAlbum" -> outputs.add(objectMapper.valueToTree(currentPlayer.addAlbum(this)));
            case "showAlbums" -> {
                artist = myLibrary.findArtistByName(username);
                if (artist == null) {
                    System.err.println("Artist does not exist.");
                    break;
                }
                outputs.add(objectMapper.valueToTree(artist.showAlbums(this)));
            }
            case "removeAlbum" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.removeAlbum(this)));
            case "changePage" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.changePage(this)));
            case "printCurrentPage" -> {
                listener = myLibrary.findListenerByUsername(username);
                if (listener == null) {
                    System.out.println("Invalid user");
                    break;
                }
                outputs.add(objectMapper.valueToTree(listener.printCurrentPage(this)));
            }
            case "addEvent" -> outputs.add(objectMapper.valueToTree(currentPlayer.addEvent(this)));
            case "addMerch" -> outputs.add(objectMapper.valueToTree(currentPlayer.addMerch(this)));
            case "addPodcast" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.addPodcast(this)));
            case "removePodcast" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.removePodcast(this)));
            case "deleteUser" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.deleteUser(this)));
            case "addAnnouncement" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.addAnnouncement(this)));
            case "removeAnnouncement" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.removeAnnouncement(this)));
            case "removeEvent" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.removeEvent(this)));
            case "showPodcasts" ->
                    outputs.add(objectMapper.valueToTree(currentPlayer.showPodcasts(this)));
            case "getTop5Albums" ->
                    outputs.add(objectMapper.valueToTree(myLibrary.getTop5Albums(this)));
            case "wrapped" -> {
                currentPlayer.setCurrentFile(currentPlayer.updateStatus(this));
                outputs.add(objectMapper.valueToTree(currentPlayer.getUser().wrapped(this)));
            }
            case "buyMerch" -> outputs.add(objectMapper.valueToTree(currentPlayer.buyMerch(this)));
            case "buyPremium" -> outputs.add(objectMapper.valueToTree(currentPlayer.buyPremium(this)));
            case "cancelPremium" -> outputs.add(objectMapper.valueToTree(currentPlayer.cancelPremium(this)));
            case "adBreak" -> outputs.add(objectMapper.valueToTree(currentPlayer.adBreak(this)));
            default -> System.err.println("Invalid command");
        }
    }
}
