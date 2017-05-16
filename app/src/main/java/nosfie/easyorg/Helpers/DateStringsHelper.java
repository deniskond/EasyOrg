package nosfie.easyorg.Helpers;

public class DateStringsHelper {
    public static void getHumanDateString(int year, int month, int date) {

    }
    public static String getHumanMonthName(int month) {
        return getHumanMonthName(month, true);
    }
    public static String getHumanMonthName(int month, boolean startWithCapital) {
        String result = "";
        switch (month) {
            case 1:
                result = "Январь"; break;
            case 2:
                result = "Февраль"; break;
            case 3:
                result = "Март"; break;
            case 4:
                result = "Апрель"; break;
            case 5:
                result = "Май"; break;
            case 6:
                result = "Июнь"; break;
            case 7:
                result = "Июль"; break;
            case 8:
                result = "Август"; break;
            case 9:
                result = "Сентябрь"; break;
            case 10:
                result = "Октябрь"; break;
            case 11:
                result = "Ноябрь"; break;
            case 12:
                result = "Декабрь"; break;
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
                result = "Января"; break;
            case 2:
                result = "Февраля"; break;
            case 3:
                result = "Марта"; break;
            case 4:
                result = "Апреля"; break;
            case 5:
                result = "Мая"; break;
            case 6:
                result = "Июня"; break;
            case 7:
                result = "Июля"; break;
            case 8:
                result = "Августа"; break;
            case 9:
                result = "Сентября"; break;
            case 10:
                result = "Октября"; break;
            case 11:
                result = "Ноября"; break;
            case 12:
                result = "Декабря"; break;
        }
        if (!startWithCapital)
            result = result.toLowerCase();
        return result;
    }
}
