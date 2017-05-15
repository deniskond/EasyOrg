package nosfie.easyorg.TaskCalendar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.R;

import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class TaskCalendar extends AppCompatActivity {

    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    LinearLayout tasksCalendar;
    int DP;
    TextView monthText;
    String monthGenitive = "";
    ArrayList<Task> allMonthTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        tasksCalendar = (LinearLayout)findViewById(R.id.tasks_calendar);
        monthText = (TextView)findViewById(R.id.month_text);
        DP = convertDpToPixels(this, 1);
        drawTasksCalendar();
    }

    protected void drawTasksCalendar() {
        tasksCalendar.removeAllViews();
        Calendar calendar = Calendar.getInstance();
        String monthStr = getMonthString(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        monthText.setText(monthStr);
        int monthMax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        String firstDayOfMonth = calendar.get(Calendar.YEAR) + "."
                + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "."
                + "01";

        String lastDayOfMonth = calendar.get(Calendar.YEAR) + "."
                + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "."
                + monthMax;

        DB = tasksConnector.getReadableDatabase();

        String columns[] = {"_id", "name", "type", "startDate", "startTime", "count",
                "reminder", "endDate", "shoppingList", "status", "currentCount", "shoppingListState"};

        Cursor cursor = DB.query("tasks", columns,
                "endDate >= '" + firstDayOfMonth + "' AND endDate <= '" + lastDayOfMonth + "'",
                null, null, null, "startTime");

        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getString(7),
                            cursor.getString(8),
                            cursor.getString(9),
                            cursor.getInt(10),
                            cursor.getString(11)
                    );
                    allMonthTasks.add(task);
                } while (cursor.moveToNext());
            }
        }
        DB.close();

        for (int i = 0; i < monthMax / 3; i++)
            addCalendarRow(i * 3 + 1, i * 3 + 2, i * 3 + 3,
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR));

        if (monthMax % 3 == 1)
            addCalendarRow(monthMax, 0, 0,
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR));
        else if (monthMax % 3 == 2)
            addCalendarRow(monthMax - 1, monthMax, 0,
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR));
    }

    protected void addCalendarRow(int firstDay, int secondDay, int thirdDay, int month, int year) {
        LinearLayout calendarRow = new LinearLayout(this);
        LinearLayout.LayoutParams calendarRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        calendarRowParams.height = 150 * DP;
        calendarRow.setLayoutParams(calendarRowParams);
        calendarRow.setOrientation(LinearLayout.HORIZONTAL);
        calendarRow.setWeightSum(100);

        LinearLayout firstDayColumn = new LinearLayout(this);
        LinearLayout.LayoutParams firstDayColumnParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT);
        firstDayColumnParams.setMargins(0, 0, 1, 1);
        firstDayColumnParams.weight = 33;
        firstDayColumn.setLayoutParams(firstDayColumnParams);
        firstDayColumn.setOrientation(LinearLayout.VERTICAL);
        firstDayColumn.setBackgroundColor(0xFFAAAAAA);
        firstDayColumn.setOrientation(LinearLayout.VERTICAL);
        addCalendarDay(firstDayColumn, firstDay, month, year);

        LinearLayout secondDayColumn = new LinearLayout(this);
        LinearLayout.LayoutParams secondDayColumnParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT);
        secondDayColumnParams.setMargins(0, 0, 1, 1);
        secondDayColumnParams.weight = 34;
        secondDayColumn.setLayoutParams(secondDayColumnParams);
        secondDayColumn.setOrientation(LinearLayout.VERTICAL);
        secondDayColumn.setBackgroundColor(0xFFAAAAAA);
        secondDayColumn.setOrientation(LinearLayout.VERTICAL);
        addCalendarDay(secondDayColumn, secondDay, month, year);

        LinearLayout thirdDayColumn = new LinearLayout(this);
        LinearLayout.LayoutParams thirdDayColumnParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT);
        thirdDayColumnParams.setMargins(0, 0, 0, 1);
        thirdDayColumnParams.weight = 33;
        thirdDayColumn.setLayoutParams(thirdDayColumnParams);
        thirdDayColumn.setOrientation(LinearLayout.VERTICAL);
        thirdDayColumn.setBackgroundColor(0xFFAAAAAA);
        thirdDayColumn.setOrientation(LinearLayout.VERTICAL);
        addCalendarDay(thirdDayColumn, thirdDay, month, year);

        calendarRow.addView(firstDayColumn);
        calendarRow.addView(secondDayColumn);
        calendarRow.addView(thirdDayColumn);

        tasksCalendar.addView(calendarRow);
    }

    protected void addCalendarDay(LinearLayout dayColumn, int day, int month, int year) {

        String todayStr = String.format("%04d", year) + "."
                + String.format("%02d", month) + "."
                + String.format("%02d", day);

        LinearLayout dayNameRow = new LinearLayout(this);
        LinearLayout.LayoutParams dayNameRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dayNameRowParams.height = 30 * DP;
        dayNameRowParams.setMargins(0, 0, 0, 1);
        dayNameRow.setOrientation(LinearLayout.HORIZONTAL);
        dayNameRow.setGravity(Gravity.CENTER);
        dayNameRow.setLayoutParams(dayNameRowParams);
        dayNameRow.setBackgroundColor(0xFFEFEFEF);

        TextView dayName = new TextView(this);
        LinearLayout.LayoutParams dayNameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dayName.setTextColor(0xFF000000);
        if (day != 0)
            dayName.setText(day + " " + monthGenitive);
        dayName.setTypeface(null, Typeface.BOLD);
        dayName.setLayoutParams(dayNameParams);
        dayNameRow.addView(dayName);

        LinearLayout dayTasks = new LinearLayout(this);
        LinearLayout.LayoutParams dayTasksParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dayTasks.setLayoutParams(dayTasksParams);
        dayTasks.setOrientation(LinearLayout.VERTICAL);
        dayTasks.setBackgroundColor(0xFFAAAAAA);

        if (day != 0)
            for (Task task: allMonthTasks)
                if (task.customEndDate.toString().equals(todayStr)) {
                    LinearLayout dayTaskRow = new LinearLayout(this);
                    LinearLayout.LayoutParams dayTaskParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    dayTaskParams.setMargins(0, 0, 0, 1);
                    dayTaskRow.setLayoutParams(dayTaskParams);
                    dayTaskRow.setOrientation(LinearLayout.HORIZONTAL);
                    dayTaskRow.setGravity(Gravity.CENTER_VERTICAL);
                    dayTaskRow.setMinimumHeight(10 * DP);
                    int color = 0;
                    switch (task.status) {
                        case ACTUAL:
                            if (task.type == Task.TYPE.SIMPLE)
                                color = R.color.colorTaskActual;
                            else {
                                if (task.currentCount == task.count)
                                    color = R.color.colorTaskDone;
                                else if (task.currentCount != 0)
                                    color = R.color.colorTaskInProcess;
                                else
                                    color = R.color.colorTaskActual;
                            }
                            break;
                        case DONE:
                            color = R.color.colorTaskDone;
                            break;
                        case NOT_DONE:
                            color = R.color.colorTaskFailed;
                            break;
                        case IN_PROCESS:
                            color = R.color.colorTaskInProcess;
                            break;
                        case POSTPONED:
                            color = R.color.colorTaskPostponed;
                            break;
                    }
                    dayTaskRow.setBackgroundColor(getResources().getColor(color));
                    dayTasks.addView(dayTaskRow);

                    TextView taskName = new TextView(this);
                    LinearLayout.LayoutParams taskNameParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    taskName.setLayoutParams(taskNameParams);
                    taskName.setText(task.name);
                    taskName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6);
                    taskName.setPadding(8 * DP, 0, 0, 0);
                    dayTaskRow.addView(taskName);
        }

        LinearLayout dayTasksFillup = new LinearLayout(this);
        LinearLayout.LayoutParams dayTasksFillupParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dayTasksFillup.setLayoutParams(dayTasksFillupParams);
        dayTasksFillup.setOrientation(LinearLayout.HORIZONTAL);
        dayTasksFillup.setBackgroundColor(0xFFFFFFFF);
        dayTasks.addView(dayTasksFillup);

        dayColumn.addView(dayNameRow);
        dayColumn.addView(dayTasks);
    }

    protected String getMonthString(int month, int year) {
        String result = "";
        switch (month) {
            case 0:
                result += "Январь";
                monthGenitive = "января";
                break;
            case 1:
                result += "Февраль";
                monthGenitive = "февраля";
                break;
            case 2:
                result += "Март";
                monthGenitive = "марта";
                break;
            case 3:
                result += "Апрель";
                monthGenitive = "апреля";
                break;
            case 4:
                result += "Май";
                monthGenitive = "мая";
                break;
            case 5:
                result += "Июнь";
                monthGenitive = "июня";
                break;
            case 6:
                result += "Июль";
                monthGenitive = "июля";
                break;
            case 7:
                result += "Август";
                monthGenitive = "августа";
                break;
            case 8:
                result += "Сентябрь";
                monthGenitive = "сентября";
                break;
            case 9:
                result += "Октябрь";
                monthGenitive = "октября";
                break;
            case 10:
                result += "Ноябрь";
                monthGenitive = "ноября";
                break;
            case 11:
                result += "Декабрь";
                monthGenitive = "декабря";
                break;
        }
        result += ", " + year;
        return result;
    }

}
