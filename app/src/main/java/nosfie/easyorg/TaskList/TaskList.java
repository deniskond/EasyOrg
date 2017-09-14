package nosfie.easyorg.TaskList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.DayValues;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.NewTask.NewTaskFirstScreen;
import nosfie.easyorg.R;

import static nosfie.easyorg.Database.Queries.getTasksForTaskListFromDB;

public class TaskList extends AppCompatActivity {

    TasksConnector tasksConnector;
    ArrayList<Task> tasks = new ArrayList<>();
    float scale;
    LinearLayout taskList, taskListShadow;
    LinearLayout timespanButton, addTaskButton;
    LinearLayout timespanSelector, cancelTimespanSelector;
    TextView timespanText;
    Timespan timespan = Timespan.TODAY;
    TextView progressBarText;
    ProgressBar progressBar;
    ImageView addTaskImage;
    int lockedID = -1;
    Timespan lockedTimespan = Timespan.TODAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        setContentView(R.layout.task_list);

        // Setting global variables values
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        scale = getApplicationContext().getResources().getDisplayMetrics().density;

        // Setting up view elements
        taskList = (LinearLayout)findViewById(R.id.task_list);
        taskListShadow = (LinearLayout)findViewById(R.id.task_list_shadow);
        timespanSelector = (LinearLayout)findViewById(R.id.timespan_selector);
        timespanText = (TextView)findViewById(R.id.timespan_text);
        progressBarText = (TextView)findViewById(R.id.progressBarText);
        progressBar = (ProgressBar)findViewById(R.id.mprogressBar);
        cancelTimespanSelector = (LinearLayout)findViewById(R.id.cancel_timespan_selector);
        timespanButton = (LinearLayout)findViewById(R.id.timespan_button);
        addTaskButton = (LinearLayout)findViewById(R.id.addTaskButton);
        addTaskImage = (ImageView)findViewById(R.id.addTaskImage);

        // Setting "Add task" button onTouchListener
        addTaskButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        addTaskImage.setImageResource(R.drawable.plus_dark);
                        break;
                    case MotionEvent.ACTION_UP:
                        addTaskImage.setImageResource(R.drawable.plus);
                        Intent intent = new Intent(TaskList.this, NewTaskFirstScreen.class);
                        String timespanStr = "";
                        switch (timespan) {
                            case TODAY:
                                timespanStr = "DAY";
                                break;
                            case WEEK:
                                timespanStr = "WEEK";
                                break;
                            case MONTH:
                                timespanStr = "MONTH";
                                break;
                            case YEAR:
                                timespanStr = "YEAR";
                                break;
                            case UNLIMITED:
                                timespanStr = "NONE";
                                break;
                        }
                        intent.putExtra("predefinedTimespan", timespanStr);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        // Setting timespan onClickListeners (toggle + each of timespans)
        timespanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedTimespanImage();
                timespanSelector.setVisibility(View.VISIBLE);
            }
        });
        timespanSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timespanSelector.getVisibility() == View.VISIBLE)
                    timespanSelector.setVisibility(View.GONE);
            }
        });
        setTimespanClickListeners();

        // Drawing tasks
        getTasks();
    }

    protected void getTasks() {
        tasks.clear();
        taskList.removeAllViews();
        tasks = getTasksForTaskListFromDB(TaskList.this, timespan);
        tasks = filterTasksByTimespan(tasks, timespan);
        int num = 1;
        for (final Task task: tasks) {
            LinearLayout taskRow = TaskView.getTaskRow(
                    TaskList.this, num, task, true, true, timespan, new Callable() {
                        @Override
                        public Object call() throws Exception {
                            lockedID = -1;
                            redrawProgressBar();
                            getTasks();
                            return null;
                        }
                    }, new Callable() {
                        @Override
                        public Object call() throws Exception {
                            lockedTimespan = timespan;
                            lockedID = task.id;
                            redrawProgressBar();
                            getTasks();
                            return null;
                        }
                    });
            taskList.addView(taskRow);
            num++;
        }
        redrawProgressBar();
        if (tasks.size() == 0)
            taskListShadow.setVisibility(View.GONE);
        else
            taskListShadow.setVisibility(View.VISIBLE);
    }

    protected void setTimespanClickListeners() {
        LinearLayout timespanOptionToday = (LinearLayout)findViewById(R.id.timespanOptionToday);
        timespanOptionToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText(getResources().getString(R.string.today_tasks));
                timespan = Timespan.TODAY;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionWeek = (LinearLayout)findViewById(R.id.timespanOptionWeek);
        timespanOptionWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText(getResources().getString(R.string.week_tasks));
                timespan = Timespan.WEEK;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionMonth = (LinearLayout)findViewById(R.id.timespanOptionMonth);
        timespanOptionMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText(getResources().getString(R.string.month_tasks));
                timespan = Timespan.MONTH;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionYear = (LinearLayout)findViewById(R.id.timespanOptionYear);
        timespanOptionYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText(getResources().getString(R.string.year_tasks));
                timespan = Timespan.YEAR;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionUnlimited = (LinearLayout)findViewById(R.id.timespanOptionUnlimited);
        timespanOptionUnlimited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText(getResources().getString(R.string.perpetual_tasks));
                timespan = Timespan.UNLIMITED;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

    }

    protected void setSelectedTimespanImage() {
        ImageView optionTodayImage = (ImageView)findViewById(R.id.optionTodayImage);
        ImageView optionWeekImage = (ImageView)findViewById(R.id.optionWeekImage);
        ImageView optionMonthImage = (ImageView)findViewById(R.id.optionMonthImage);
        ImageView optionYearImage = (ImageView)findViewById(R.id.optionYearImage);
        ImageView optionUnlimitedImage = (ImageView)findViewById(R.id.optionUnlimitedImage);

        if (timespan == Timespan.TODAY)
            optionTodayImage.setImageResource(R.drawable.tick_icon);
        else
            optionTodayImage.setImageResource(R.drawable.empty_tick_icon);

        if (timespan == Timespan.WEEK)
            optionWeekImage.setImageResource(R.drawable.tick_icon);
        else
            optionWeekImage.setImageResource(R.drawable.empty_tick_icon);

        if (timespan == Timespan.MONTH)
            optionMonthImage.setImageResource(R.drawable.tick_icon);
        else
            optionMonthImage.setImageResource(R.drawable.empty_tick_icon);

        if (timespan == Timespan.YEAR)
            optionYearImage.setImageResource(R.drawable.tick_icon);
        else
            optionYearImage.setImageResource(R.drawable.empty_tick_icon);

        if (timespan == Timespan.UNLIMITED)
            optionUnlimitedImage.setImageResource(R.drawable.tick_icon);
        else
            optionUnlimitedImage.setImageResource(R.drawable.empty_tick_icon);
    }

    protected void redrawProgressBar() {
        int taskCount = tasks.size();
        int tasksDone = 0;
        for (Task task: tasks)
            if (task.status == Task.STATUS.DONE)
                tasksDone++;
        progressBarText.setText(Integer.toString(tasksDone) + "/" + Integer.toString(taskCount));
        progressBar.setProgress((int)((double)tasksDone / (double)taskCount * 100));
    }

    protected ArrayList<Task> filterTasksByTimespan(ArrayList<Task> tasks, Timespan timespan) {
        if (timespan == Timespan.UNLIMITED) {
            ArrayList<Task> unlimitedTasks = new ArrayList<>();
            for (Task task: tasks)
                if (task.customEndDate.year == 0)
                    unlimitedTasks.add(task);
            return unlimitedTasks;
        }

        ArrayList<Task> todayTasks = new ArrayList<>();
        DayValues dayValues = new DayValues(TaskList.this);
        for (Task task: tasks) {
            String startDate = task.customStartDate.toString();
            String endDate = task.customEndDate.toString();
            String today = dayValues.today.toString();
            if ((startDate.equals(today) && endDate.equals(today))
                ||
                (endDate.equals(today) &&
                 task.status != Task.STATUS.DONE &&
                 task.status != Task.STATUS.NOT_DONE &&
                 task.status != Task.STATUS.IN_PROCESS
                )
                ||
                (lockedTimespan == Timespan.TODAY && lockedID == task.id)
               )
                todayTasks.add(task);
        }
        if (timespan == Timespan.TODAY)
            return todayTasks;

        ArrayList<Task> weekTasks = new ArrayList<>();
        for (Task task: tasks) {
            String startDate = task.customStartDate.toString();
            String endDate = task.customEndDate.toString();
            String startOfWeek = dayValues.startOfWeek.toString();
            String endOfWeek = dayValues.endOfWeek.toString();
            if ((endDate.equals(endOfWeek))
                 ||
                (startDate.compareTo(startOfWeek) > 0 &&
                 startDate.compareTo(endOfWeek) <= 0 &&
                 endDate.compareTo(startOfWeek) > 0 &&
                 endDate.compareTo(endOfWeek) <= 0 &&
                 task.status != Task.STATUS.DONE &&
                 task.status != Task.STATUS.NOT_DONE &&
                 task.status != Task.STATUS.IN_PROCESS)
                 ||
                (endDate.compareTo(startOfWeek) > 0 &&
                 endDate.compareTo(endOfWeek) <= 0 &&
                 startDate.compareTo(startOfWeek) > 0 &&
                 startDate.compareTo(endOfWeek) <= 0 &&
                 !startDate.equals(endDate)
                )
                 ||
                (lockedTimespan == Timespan.WEEK && lockedID == task.id)
               ) {
                   if (!todayTasks.contains(task))
                       weekTasks.add(task);
            }
        }
        if (timespan == Timespan.WEEK)
            return weekTasks;

        ArrayList<Task> monthTasks = new ArrayList<>();
        for (Task task: tasks) {
            String startDate = task.customStartDate.toString();
            String endDate = task.customEndDate.toString();
            String startOfMonth = dayValues.startOfMonth.toString();
            String endOfMonth = dayValues.endOfMonth.toString();
            if (endDate.equals(endOfMonth)
                ||
                (startDate.compareTo(startOfMonth) >= 0 &&
                 startDate.compareTo(endOfMonth) <= 0 &&
                 endDate.compareTo(startOfMonth) >= 0 &&
                 endDate.compareTo(endOfMonth) <= 0 &&
                 task.status != Task.STATUS.DONE &&
                 task.status != Task.STATUS.NOT_DONE &&
                 task.status != Task.STATUS.IN_PROCESS
                )
                ||
                (lockedTimespan == Timespan.MONTH && lockedID == task.id)
               ) {
                   if (!todayTasks.contains(task) && !weekTasks.contains(task))
                       monthTasks.add(task);
            }
        }
        if (timespan == Timespan.MONTH)
            return monthTasks;

        ArrayList<Task> yearTasks = new ArrayList<>();
        for (Task task: tasks) {
            String endDate = task.customEndDate.toString();
            String endOfYear = dayValues.endOfYear.toString();
            String startOfYear = dayValues.startOfYear.toString();
            if (endDate.equals(endOfYear)
                ||
                (endDate.compareTo(startOfYear) >= 0 &&
                 endDate.compareTo(endOfYear) <= 0 &&
                 task.status != Task.STATUS.DONE &&
                 task.status != Task.STATUS.NOT_DONE &&
                 task.status != Task.STATUS.IN_PROCESS
                )
                ||
                (lockedTimespan == Timespan.YEAR && lockedID == task.id)
            ) {
                if (!todayTasks.contains(task) && !weekTasks.contains(task) && !monthTasks.contains(task))
                    yearTasks.add(task);
            }
        }
        return yearTasks;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTasks();
        redrawProgressBar();
    }
}