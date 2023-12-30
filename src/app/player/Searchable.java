package app.player;

public interface Searchable {
    /**
     * @param filters given filters
     * @return true if it matches all the filters, false otherwise.
     */
    boolean matchesFilter(Filter filters);

    /**
     * @return name of the Searchable object
     */
    String getName();

    /**
     * @return true if the object is playable
     */
    boolean isPlayable();
}
