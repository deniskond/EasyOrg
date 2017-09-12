package nosfie.easyorg.Helpers;

import android.content.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;

import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;

public class DateStringsHelper {

    static Context context = MainActivity.getInstance();

    public static String getHumanMonthName(int month) {
        return getHumanMonthName(month, true);
    }
    public static String getHumanMonthName(int month, boolean startWithCapital) {
        String result = "";
        switch (month) {
            case 1:
                result = context.getResources().getString(R.string.january); break;
            case 2:
                result = context.getResources().getString(R.string.february); break;
            case 3:
                result = context.getResources().getString(R.string.march); break;
            case 4:
                result = context.getResources().getString(R.string.april); break;
            case 5:
                result = context.getResources().getString(R.string.may); break;
            case 6:
                result = context.getResources().getString(R.string.june); break;
            case 7:
                result = context.getResources().getString(R.string.july); break;
            case 8:
                result = context.getResources().getString(R.string.august); break;
            case 9:
                result = context.getResources().getString(R.string.september); break;
            case 10:
                result = context.getResources().getString(R.string.october); break;
            case 11:
                result = context.getResources().getString(R.string.november); break;
            case 12:
                result = context.getResources().getString(R.string.december); break;
        }
        if (!startWithCapital)
            result = result.toLowerCase();
        return result;
    }
    public static String getHumanMonthNameGenitive(int month) {
        return getHumanMonthNameGenitive(month, false);
    }
    public static String getHumanMonthNameGenitive(int month, boolean startWithCapital) {
        String result = "";
        switch (month) {
            case 1:
                result = context.getResources().getString(R.string.january_gen); break;
            case 2:
                result = context.getResources().getString(R.string.february_gen); break;
            case 3:
                result = context.getResources().getString(R.string.march_gen); break;
            case 4:
                result = context.getResources().getString(R.string.april_gen); break;
            case 5:
                result = context.getResources().getString(R.string.may_gen); break;
            case 6:
                result = context.getResources().getString(R.string.june_gen); break;
            case 7:
                result = context.getResources().getString(R.string.july_gen); break;
            case 8:
                result = context.getResources().getString(R.string.august_gen); break;
            case 9:
                result = context.getResources().getString(R.string.september_gen); break;
            case 10:
                result = context.getResources().getString(R.string.october_gen); break;
            case 11:
                result = context.getResources().getString(R.string.november_gen); break;
            case 12:
                result = context.getResources().getString(R.string.december_gen); break;
        }
        if (!startWithCapital)
            result = result.toLowerCase();
        return result;
    }

    public static String getDayOfWeekStr(int year, int month, int day) {
        month--;
        Calendar calendar = new GregorianCalendar(year, month, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        String dayOfWeekStr = "";
        switch (dayOfWeek) {
            case 1:
                dayOfWeekStr = context.getResources().getString(R.string.sunday_short);
                break;
            case 2:
                dayOfWeekStr = context.getResources().getString(R.string.monday_short);
                break;
            case 3:
                dayOfWeekStr = context.getResources().getString(R.string.tuesday_short);
                break;
            case 4:
                dayOfWeekStr = context.getResources().getString(R.string.wednesday_short);
                break;
            case 5:
                dayOfWeekStr = context.getResources().getString(R.string.thursday_short);
                break;
            case 6:
                dayOfWeekStr = context.getResources().getString(R.string.friday_short);
                break;
            case 7:
                dayOfWeekStr = context.getResources().getString(R.string.saturday_short);
                break;
        }
        return dayOfWeekStr;
    }

    public static String getShortMonthName(int month, boolean startUppercase) {
        String result = "";
        switch (month) {
            case 0:
                return context.getResources().getString(R.string.january_short);
            case 1:
                return context.getResources().getString(R.string.february_short);
            case 2:
                return context.getResources().getString(R.string.march_short);
            case 3:
                return context.getResources().getString(R.string.april_short);
            case 4:
                return context.getResources().getString(R.string.may_short);
            case 5:
                return context.getResources().getString(R.string.june_short);
            case 6:
                return context.getResources().getString(R.string.july_short);
            case 7:
                return context.getResources().getString(R.string.august_short);
            case 8:
                return context.getResources().getString(R.string.september_short);
            case 9:
                return context.getResources().getString(R.string.october_short);
            case 10:
                return context.getResources().getString(R.string.november_short);
            case 11:
                return context.getResources().getString(R.string.december_short);
        }
        if (!startUppercase)
            result = result.toLowerCase();
        return result;
    }
}
