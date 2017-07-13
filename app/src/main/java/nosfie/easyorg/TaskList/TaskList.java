package nosfie.easyorg.TaskList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.DayValues;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.R;

public class TaskList extends AppCompatActivity {

    TextView result;
    Button deleteDbButton;
    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    ArrayList<Task> tasks = new ArrayList<>();
    float scale;
    LinearLayout taskList;
    final int TASK_ROW_HEIGHT = 45;
    LinearLayout timespanButton;
    LinearLayout timespanSelector;
    TextView timespanText;
    Timespan timespan = Timespan.TODAY;
    TextView progressBarText;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);

        taskList = (LinearLayout)findViewById(R.id.task_list);
        result = (TextView)findViewById(R.id.testTextView);
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        scale = getApplicationContext().getResources().getDisplayMetrics().density;
        timespanSelector = (LinearLayout)findViewById(R.id.timespan_selector);
        timespanText = (TextView)findViewById(R.id.timespan_text);
        progressBarText = (TextView)findViewById(R.id.progressBarText);
        progressBar = (ProgressBar)findViewById(R.id.mprogressBar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            timespan = Timespan.valueOf(extras.getString("timespan"));
            switch (timespan) {
                case TODAY:
                    timespanText.setText("Задачи на сегодня");
                    break;
                case WEEK:
                    timespanText.setText("Задачи на неделю");
                    break;
                case MONTH:
                    timespanText.setText("Задачи на месяц");
                    break;
                case YEAR:
                    timespanText.setText("Задачи на год");
                    break;
                case UNLIMITED:
                    timespanText.setText("Бессрочные задачи");
                    break;
            }
        }

        getTasks();
        setTimespanClickListeners();

        timespanButton = (LinearLayout)findViewById(R.id.timespan_button);
        timespanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedTimespanImage();
                timespanSelector.setVisibility(View.VISIBLE);
            }
        });

        deleteDbButton = (Button)findViewById(R.id.buttonDeleteDB);
        deleteDbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB = tasksConnector.getWritableDatabase();
                DB.execSQL("DROP TABLE IF EXISTS tasks");
                DB.close();
                result.setText("");
                Toast.makeText(getApplicationContext(), "Таблица удалена", Toast.LENGTH_SHORT).show();
            }
        });

    }

    protected void getTasks() {
        tasks.clear();
        taskList.removeAllViews();
        DB = tasksConnector.getReadableDatabase();

        String columns[] = {"_id", "name", "type", "startDate", "startTime", "count",
                "reminder", "endDate", "shoppingList", "status", "currentCount", "shoppingListState"};

        Cursor cursor = DB.query("tasks", columns, null, null, null, null, "startTime");

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
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        }
        DB.close();

        tasks = filterTasksByTimespan(tasks, timespan);
        int num = 1;
        for (Task task: tasks) {
            result.setText(result.getText() + task.name + " " + task.customStartDate);
            LinearLayout taskRow = TaskView.getTaskRow(
                    TaskList.this, num, task, new Callable() {
                        @Override
                        public Object call() throws Exception {
                            redrawProgressBar();
                            getTasks();
                            return null;
                        }
                    });
            taskList.addView(taskRow);
            num++;
        }
        redrawProgressBar();
    }

    protected void setTimespanClickListeners() {
        LinearLayout timespanOptionToday = (LinearLayout)findViewById(R.id.timespanOptionToday);
        timespanOptionToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Задачи на сегодня");
                timespan = Timespan.TODAY;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionWeek = (LinearLayout)findViewById(R.id.timespanOptionWeek);
        timespanOptionWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Задачи на неделю");
                timespan = Timespan.WEEK;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionMonth = (LinearLayout)findViewById(R.id.timespanOptionMonth);
        timespanOptionMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Задачи на месяц");
                timespan = Timespan.MONTH;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionYear = (LinearLayout)findViewById(R.id.timespanOptionYear);
        timespanOptionYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Задачи на год");
                timespan = Timespan.YEAR;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionUnlimited = (LinearLayout)findViewById(R.id.timespanOptionUnlimited);
        timespanOptionUnlimited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Бессрочные задачи");
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
        DayValues dayValues = new DayValues();
        for (Task task: tasks) {
            String startDate = task.customStartDate.toString();
            String endDate = task.customEndDate.toString();
            String today = dayValues.today.toString();
            if ((startDate.equals(today) && endDate.equals(today)) ||
                (endDate.equals(today) && task.status != Task.STATUS.DONE
                    && task.status != Task.STATUS.NOT_DONE))
                todayTasks.add(task);
        }
        if (timespan == Timespan.TODAY)
            return todayTasks;

        ArrayList<Task> weekTasks = new ArrayList<>();
        for (Task task: tasks) {
            String endDate = task.customEndDate.toString();
            String startOfWeek = dayValues.startOfWeek.toString();
            String endOfWeek = dayValues.endOfWeek.toString();
            if ((endDate.equals(endOfWeek)) ||
                (endDate.compareTo(startOfWeek) > 0 &&
                 endDate.compareTo(endOfWeek) <= 0 &&
                 task.status != Task.STATUS.DONE && task.status != Task.STATUS.NOT_DONE)) {
                if (!todayTasks.contains(task))
                    weekTasks.add(task);
            }
        }
        if (timespan == Timespan.WEEK)
            return weekTasks;

        ArrayList<Task> monthTasks = new ArrayList<>();
        for (Task task: tasks) {
            String endDate = task.customEndDate.toString();
            String startOfMonth = dayValues.startOfMonth.toString();
            String endOfMonth = dayValues.endOfMonth.toString();
            if (endDate.equals(endOfMonth) ||
                (endDate.compareTo(startOfMonth) >= 0 &&
                 endDate.compareTo(endOfMonth) <= 0 &&
                 task.status != Task.STATUS.DONE && task.status != Task.STATUS.NOT_DONE)) {
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
            if (endDate.equals(endOfYear) ||
                (endDate.compareTo(startOfYear) >= 0 &&
                 endDate.compareTo(endOfYear) <= 0 &&
                 task.status != Task.STATUS.DONE && task.status != Task.STATUS.NOT_DONE)) {
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