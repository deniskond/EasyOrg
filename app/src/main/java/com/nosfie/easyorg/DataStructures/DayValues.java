package nosfie.easyorg.DataStructures;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class DayValues {

    public CustomDate today = new CustomDate();
    public CustomDate tomorrow = new CustomDate();
    public CustomDate startOfWeek = new CustomDate();
    public CustomDate endOfWeek = new CustomDate();
    public CustomDate startOfMonth = new CustomDate();
    public CustomDate endOfMonth = new CustomDate();
    public CustomDate startOfYear = new CustomDate();
    public CustomDate endOfYear = new CustomDate();

    public DayValues(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String[] timeSplit = preferences.getString("dayMargin", "").split(":");
        int hours = Integer.parseInt(timeSplit[0]);
        int minutes = Integer.parseInt(timeSplit[1]);

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR) < hours)
            calendar.add(Calendar.HOUR, -hours);
        if (calendar.get(Calendar.HOUR) == hours && (calendar.get(Calendar.MINUTE) < minutes)) {
            calendar.add(Calendar.HOUR, -hours);
            calendar.add(Calendar.MINUTE, -minutes);
        }

        // TODAY
        this.today.year = calendar.get(Calendar.YEAR);
        this.today.month = calendar.get(Calendar.MONTH) + 1;
        this.today.day = calendar.get(Calendar.DAY_OF_MONTH);

        // TOMORROW
        calendar.add(Calendar.DATE, 1);
        this.tomorrow.year = calendar.get(Calendar.YEAR);
        this.tomorrow.month = calendar.get(Calendar.MONTH) + 1;
        this.tomorrow.day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar = Calendar.getInstance();

        // WEEK
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int addition = 8 - dayOfWeek;
        if (addition == 7) addition = 0;
        calendar.add(Calendar.DATE, addition);
        this.endOfWeek.year = calendar.get(Calendar.YEAR);
        this.endOfWeek.month = calendar.get(Calendar.MONTH) + 1;
        this.endOfWeek.day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.DATE, -7);
        this.startOfWeek.year = calendar.get(Calendar.YEAR);
        this.startOfWeek.month = calendar.get(Calendar.MONTH) + 1;
        this.startOfWeek.day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar = Calendar.getInstance();

        // MONTH
        this.endOfMonth.year = calendar.get(Calendar.YEAR);
        this.endOfMonth.month = calendar.get(Calendar.MONTH) + 1;
        this.endOfMonth.day = calendar.getActualMaximum(Calendar.DATE);
        this.startOfMonth.year = calendar.get(Calendar.YEAR);
        this.startOfMonth.month = calendar.get(Calendar.MONTH) + 1;
        this.startOfMonth.day = 1;

        // YEAR
        this.startOfYear.year = calendar.get(Calendar.YEAR);
        this.startOfYear.month = 1;
        this.startOfYear.day = 1;
        this.endOfYear.year = calendar.get(Calendar.YEAR);
        this.endOfYear.month = 12;
        this.endOfYear.day = 31;

    }
}