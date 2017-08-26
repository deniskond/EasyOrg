package nosfie.easyorg.DataStructures;

import static nosfie.easyorg.Helpers.DateStringsHelper.getDayOfWeekStr;
import static nosfie.easyorg.Helpers.DateStringsHelper.getHumanMonthNameGenitive;

public class CustomDate {
    public int day;
    public int month;
    public int year;
    public CustomDate() {
        this.day = 0;
        this.month = 0;
        this.year = 0;
    }
    public CustomDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public String toString() {
        return String.format("%04d", this.year) + "."
            + String.format("%02d", this.month) + "."
            + String.format("%02d", this.day);
    }

    public String toHumanString() {
        return day + " " + getHumanMonthNameGenitive(month) + " " + year +
                " (" + getDayOfWeekStr(year, month, day) + ")";
    }

}
