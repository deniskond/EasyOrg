package nosfie.easyorg.TaskCalendar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nosfie.easyorg.DataStructures.DayValues;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Dialogs.MonthYearPickerDialog;
import nosfie.easyorg.R;

import static nosfie.easyorg.Database.Queries.getCalendarTasksFromDB;
import static nosfie.easyorg.Helpers.ViewHelper.*;
import static nosfie.easyorg.Helpers.DateStringsHelper.*;

public class TaskCalendar extends AppCompatActivity {

    LinearLayout tasksCalendar;
    int DP;
    TextView monthText;
    ArrayList<Task> allMonthTasks = new ArrayList<>();
    LinearLayout buttonSelectMonth, buttonBack;
    int startMonth;
    LinearLayout currentDayLayout = null;
    ScrollView scrollView;
    int dayTasksYear = 0, dayTasksMonth = 0;
    ImageView currentPressed;
    Boolean needChangeOnCurrentPressed = false;
    int colorTaskActual = 0,
        colorTaskDone = 0,
        colorTaskFailed = 0,
        colorTaskInProcess = 0,
        colorTaskPostponed = 0;
    DayValues dayValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Setting variables primary values
        Calendar calendar = Calendar.getInstance();
        startMonth = calendar.get(Calendar.MONTH) + 1;
        DP = convertDpToPixels(this, 1);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TaskCalendar.this);
        colorTaskActual = preferences.getInt("colorTaskActual", -1);
        colorTaskDone = preferences.getInt("colorTaskDone", -1);
        colorTaskFailed = preferences.getInt("colorTaskFailed", -1);
        colorTaskInProcess = preferences.getInt("colorTaskInProcess", -1);
        colorTaskPostponed = preferences.getInt("colorTaskPostponed", -1);
        dayValues = new DayValues(this);

        // Setting up view elements
        tasksCalendar = (LinearLayout)findViewById(R.id.tasks_calendar);
        monthText = (TextView)findViewById(R.id.monthText);
        scrollView = (ScrollView)findViewById(R.id.tasks_calendar_scroll);
        buttonSelectMonth = (LinearLayout)findViewById(R.id.buttonSelectMonth);
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);

        // Processing "Change month" click
        buttonSelectMonth.setOnClickListener(new View.OnClickListener() {
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

        // Processing "Back" click
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (needChangeOnCurrentPressed) {
                    currentPressed.setImageResource(R.drawable.calendar_day_header_bg);
                    needChangeOnCurrentPressed = false;
                }
            }
        });

        // Drawing calendar for the current month
        drawTasksCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        if (currentDayLayout != null) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, currentDayLayout.getTop() + 5 * DP);
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
        allMonthTasks = getCalendarTasksFromDB(this, firstDayOfMonth, lastDayOfMonth);

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
        calendarRowParams.height = 150 * DP;
        calendarRow.setLayoutParams(calendarRowParams);
        calendarRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout firstDayColumnContainer = new LinearLayout(this);
        LinearLayout.LayoutParams firstDayColumnContainerParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT);
        firstDayColumnContainerParams.weight = 1;
        firstDayColumnContainer.setLayoutParams(firstDayColumnContainerParams);
        firstDayColumnContainer.setBackgroundColor(0xFFAAAAAA);

        LinearLayout firstDayColumn = new LinearLayout(this);
        LinearLayout.LayoutParams firstDayColumnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        firstDayColumnParams.setMargins(1, 1, 1, 1);
        firstDayColumn.setLayoutParams(firstDayColumnParams);
        firstDayColumn.setBackgroundColor(0xFFAAAAAA);
        firstDayColumn.setOrientation(LinearLayout.VERTICAL);
        addCalendarDay(firstDayColumn, firstDay, month, year);
        firstDayColumnContainer.addView(firstDayColumn);

        LinearLayout paddingLayout = new LinearLayout(this);
        LinearLayout.LayoutParams paddingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        paddingParams.width = 5 * DP;
        paddingLayout.setLayoutParams(paddingParams);

        LinearLayout secondDayColumnContainer = new LinearLayout(this);
        LinearLayout.LayoutParams secondDayColumnContainerParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT);
        secondDayColumnContainerParams.weight = 1;
        secondDayColumnContainer.setLayoutParams(secondDayColumnContainerParams);
        secondDayColumnContainer.setBackgroundColor(0xFFAAAAAA);

        LinearLayout secondDayColumn = new LinearLayout(this);
        LinearLayout.LayoutParams secondDayColumnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        secondDayColumnParams.setMargins(1, 1, 1, 1);
        secondDayColumn.setLayoutParams(secondDayColumnParams);
        secondDayColumn.setBackgroundColor(0xFFAAAAAA);
        secondDayColumn.setOrientation(LinearLayout.VERTICAL);
        addCalendarDay(secondDayColumn, secondDay, month, year);
        secondDayColumnContainer.addView(secondDayColumn);

        LinearLayout paddingLayout2 = new LinearLayout(this);
        paddingLayout2.setLayoutParams(paddingParams);

        LinearLayout thirdDayColumnContainer = new LinearLayout(this);
        LinearLayout.LayoutParams thirdDayColumnContainerParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT);
        thirdDayColumnContainerParams.weight = 1;
        thirdDayColumnContainer.setLayoutParams(thirdDayColumnContainerParams);
        thirdDayColumnContainer.setBackgroundColor(0xFFAAAAAA);

        LinearLayout thirdDayColumn = new LinearLayout(this);
        LinearLayout.LayoutParams thirdDayColumnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        thirdDayColumnParams.setMargins(1, 1, 1, 1);
        thirdDayColumn.setLayoutParams(thirdDayColumnParams);
        thirdDayColumn.setBackgroundColor(0xFFAAAAAA);
        thirdDayColumn.setOrientation(LinearLayout.VERTICAL);
        addCalendarDay(thirdDayColumn, thirdDay, month, year);
        thirdDayColumnContainer.addView(thirdDayColumn);

        calendarRow.addView(firstDayColumnContainer);
        calendarRow.addView(paddingLayout);
        calendarRow.addView(secondDayColumnContainer);
        calendarRow.addView(paddingLayout2);
        calendarRow.addView(thirdDayColumnContainer);

        if (secondDay == 0)
            secondDayColumnContainer.setVisibility(View.INVISIBLE);
        if (thirdDay == 0)
            thirdDayColumnContainer.setVisibility(View.INVISIBLE);

        tasksCalendar.addView(calendarRow);

        LinearLayout verticalPadding = new LinearLayout(this);
        LinearLayout.LayoutParams verticalPaddingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        verticalPaddingParams.height = 5 * DP;
        verticalPadding.setLayoutParams(verticalPaddingParams);
        tasksCalendar.addView(verticalPadding);

        Calendar calendar = Calendar.getInstance();
        if ((firstDay == dayValues.today.day
            || secondDay == dayValues.today.day
            || thirdDay == dayValues.today.day)
                && month == calendar.get(Calendar.MONTH) + 1
                && year == calendar.get(Calendar.YEAR)) {
            currentDayLayout = calendarRow;
        }
    }

    private void addCalendarDay(final LinearLayout dayColumn, final int day, final int month, final int year) {

        String dayStr = String.format("%04d", year) + "."
                + String.format("%02d", month) + "."
                + String.format("%02d", day);

        final LinearLayout dayNameRow = new LinearLayout(this);
        LinearLayout.LayoutParams dayNameRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dayNameRowParams.height = 25 * DP;
        dayNameRowParams.setMargins(0, 0, 0, 1);
        dayNameRow.setOrientation(LinearLayout.HORIZONTAL);
        dayNameRow.setGravity(Gravity.CENTER);
        dayNameRow.setLayoutParams(dayNameRowParams);

        RelativeLayout dayNameRelative = new RelativeLayout(this);
        LinearLayout.LayoutParams dayNameRelativeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dayNameRelative.setLayoutParams(dayNameRelativeParams);

        final ImageView dayNameBg = new ImageView(this);
        RelativeLayout.LayoutParams dayNameBgParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dayNameBg.setLayoutParams(dayNameBgParams);
        dayNameBg.setImageResource(R.drawable.calendar_day_header_bg);
        dayNameBg.setScaleType(ImageView.ScaleType.FIT_XY);

        TextView dayName = new TextView(this);
        RelativeLayout.LayoutParams dayNameParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dayNameParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        dayNameParams.addRule(RelativeLayout.CENTER_VERTICAL);
        dayName.setLayoutParams(dayNameParams);
        dayName.setTextColor(0xFF333333);
        dayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        if (day != 0)
            dayName.setText(day + " " + getHumanMonthNameGenitive(month) +
                    " (" + getDayOfWeekStr(year, month, day) + ")");
        dayName.setTypeface(null, Typeface.BOLD);

        dayNameRelative.addView(dayNameBg);
        dayNameRelative.addView(dayName);
        dayNameRow.addView(dayNameRelative);

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
                                color = colorTaskActual;
                            else {
                                if (task.currentCount == task.count)
                                    color = colorTaskDone;
                                else if (task.currentCount == 0)
                                    color = colorTaskActual;
                                else
                                    color = colorTaskInProcess;
                            }
                            break;
                        case DONE:
                            color = colorTaskDone;
                            break;
                        case NOT_DONE:
                            color = colorTaskFailed;
                            break;
                        case IN_PROCESS:
                            color = colorTaskInProcess;
                            break;
                        case POSTPONED:
                            color = colorTaskPostponed;
                            break;
                    }
                    dayTaskRow.setBackgroundColor(color);

                    TextView taskName = new TextView(this);
                    LinearLayout.LayoutParams taskNameParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    taskName.setLayoutParams(taskNameParams);
                    taskName.setText(task.name);
                    taskName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
                    taskName.setPadding(4 * DP, 0, 4 * DP, 0);
                    dayTaskRow.addView(taskName);

                    dayTasks.addView(dayTaskRow);
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
                        dayNameBg.setImageResource(R.drawable.calendar_day_header_bg_dark);
                        currentPressed = dayNameBg;
                        needChangeOnCurrentPressed = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        dayNameBg.setImageResource(R.drawable.calendar_day_header_bg);
                        needChangeOnCurrentPressed = false;
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
        }
    }

}
