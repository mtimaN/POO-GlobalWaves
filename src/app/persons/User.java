package app.persons;

import app.player.Notification;
import app.output.results.WrappedResult;
import fileio.input.UserInput;
import lombok.Getter;
import lombok.Setter;
import main.Command;

import java.util.ArrayList;

@Getter @Setter
public abstract class User {
    private String username;
    private int age;
    private String city;
    private ArrayList<Notification> notifications;

    public User(final Command command) {
        username = command.getUsername();
        age = command.getAge();
        city = command.getCity();
        notifications = new ArrayList<>();
    }

    public User(final UserInput input) {
        this.username = input.getUsername();
        this.age = input.getAge();
        this.city = input.getCity();
        notifications = new ArrayList<>();
    }

    /**
     * @return true if the user can switch connection status
     */
    public abstract boolean canSwitchConnectionStatus();

    /**
     * @return true if the User can add artist items
     */
    public abstract boolean canAddArtistItems();

    /**
     * @return true if the User can add host items
     */
    public abstract boolean canAddHostItems();

    /**
     * @param command the given command
     * @return a string informing whether the delete was successful
     */
    public abstract String delete(Command command);

    /**
     * give statistics of the user's activity
     * @param command the given command
     * @return result formatted for output
     */
    public abstract WrappedResult wrapped(Command command);
}
