package app.player;

import lombok.Getter;
import lombok.Setter;
import main.Command;

@Getter @Setter
public final class Event {
    private String name;
    private String description;
    private String date;

    public Event(final Command command) {
        name = command.getName();
        description = command.getDescription();
        date = command.getDate();
    }

    /**
     * @param date a given date
     * @return true if the date is valid
     */
    public static boolean isDateValid(final String date) {
        final int dateFormatLength = 3;
        final int maxMonthValue = 12;
        final int daysFebruary = 28;
        final int maxDays = 31;
        final int minYear = 1900;
        final int maxYear = 2024;
        String[] splitDate = date.split("-");
        if (splitDate.length != dateFormatLength) {
            return false;
        }

        int day = Integer.parseInt(splitDate[0]);
        int month = Integer.parseInt(splitDate[1]);
        int year = Integer.parseInt(splitDate[2]);
        if (month > maxMonthValue) {
            return false;
        }
        if (month == 2 && day > daysFebruary) {
            return false;
        }
        if (day > maxDays) {
            return false;
        }
        return minYear < year && year < maxYear;
    }
}
