package nosfie.easyorg.TaskCalendar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.Dialogs.MonthYearPickerDialog;
import nosfie.easyorg.R;

import static nosfie.easyorg.Helpers.ViewHelper.*;
import static nosfie.easyorg.Helpers.DateStringsHelper.*;

public class TaskCalendar extends AppCompatActivity {

    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    LinearLayout tasksCalendar;
    int DP;
    TextView monthText;
    ArrayList<Task> allMonthTasks = new ArrayList<>();
    Button selectMonthButton;
    int startMonth;
    LinearLayout currentDayLayout = null;
    ScrollView scrollView;
    int dayTasksYear = 0, dayTasksMonth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        tasksCalendar = (LinearLayout)findViewById(R.id.tasks_calendar);
        monthText = (TextView)findViewById(R.id.month_text);
        Calendar calendar = Calendar.getInstance();
        startMonth = calendar.get(Calendar.MONTH) + 1;
        scrollView = (ScrollView)findViewById(R.id.tasks_calendar_scroll);
        selectMonthButton = (Button)findViewById(R.id.selectMonthButton);
        selectMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MonthYearPickerDialog monthYearPickerDialog = new MonthYearPickerDialog();
                monthYearPickerDialog.setStartMonth(startMonth);
                monthYearPickerDialog.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        drawTasksCalendar(year, month - 1);
                        startMonth = month;
                    }
                });
                monthYearPickerDialog.show(getFragmentManager(), "MonthYearPickerDialog");
            }
        });
        DP = convertDpToPixels(this, 1);
        drawTasksCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        if (currentDayLayout != null) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, currentDayLayout.getTop());
                }
            });
        }
    }

    private void drawTasksCalendar(int year, int month) {
        tasksCalendar.removeAllViews();
        allMonthTasks.clear();
        Calendar calendar = new GregorianCalendar(year, month, 1);
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

    private void addCalendarRow(int firstDay, int secondDay, int thirdDay, int month, int year) {
        LinearLayout calendarRow = new LinearLayout(this);
        LinearLayout.LayoutParams calendarRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        calendarRowParams.height = 200 * DP;
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

        Calendar calendar = Calendar.getInstance();
        if ((firstDay == calendar.get(Calendar.DAY_OF_MONTH)
            || secondDay == calendar.get(Calendar.DAY_OF_MONTH)
            || thirdDay == calendar.get(Calendar.DAY_OF_MONTH))
                && month == calendar.get(Calendar.MONTH) + 1
                && year == calendar.get(Calendar.YEAR))
            currentDayLayout = calendarRow;
    }

    private void addCalendarDay(final LinearLayout dayColumn, final int day, final int month, final int year) {

        String dayStr = String.format("%04d", year) + "."
                + String.format("%02d", month) + "."
                + String.format("%02d", day);

        final LinearLayout dayNameRow = new LinearLayout(this);
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
            dayName.setText(day + " " + getHumanMonthNameGenitive(month));
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
                if (task.customEndDate.toString().equals(dayStr)) {
                    LinearLayout dayTaskRow = new LinearLayout(this);
                    LinearLayout.LayoutParams dayTaskParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    dayTaskParams.setMargins(0, 0, 0, 1);
                    dayTaskRow.setLayoutParams(dayTaskParams);
                    dayTaskRow.setOrientation(LinearLayout.HORIZONTAL);
                    dayTaskRow.setGravity(Gravity.CENTER_VERTICAL);
                    dayTaskRow.setMinimumHeight(15 * DP);
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
                    taskName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
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
        dayColumn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dayNameRow.setBackgroundColor(0xFFC8E4FC);
                        break;
                    case MotionEvent.ACTION_UP:
                        dayNameRow.setBackgroundColor(0xFFEFEFEF);
                        dayTasksYear = year;
                        dayTasksMonth = month - 1;
                        currentDayLayout = (LinearLayout)view;
                        Intent intent = new Intent(TaskCalendar.this, DayTasks.class);
                        intent.putExtra("day", day);
                        intent.putExtra("month", month);
                        intent.putExtra("year", year);
                        startActivity(intent);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        dayNameRow.setBackgroundColor(0xFFEFEFEF);
                        break;
                }
                return true;
            }
        });
    }

    private String getMonthString(int month, int year) {
        String result = getHumanMonthName(month + 1) + ", " + year;
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dayTasksYear != 0 && dayTasksMonth != 0) {
            drawTasksCalendar(dayTasksYear, dayTasksMonth);
            //tasksCalendar.removeAllViews();
        }
    }
}
