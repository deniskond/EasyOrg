package nosfie.easyorg.TaskCalendar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.CustomDate;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.R;
import nosfie.easyorg.TaskList.TaskView;

import static nosfie.easyorg.Helpers.DateStringsHelper.*;

public class DayTasks extends AppCompatActivity {

    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    LinearLayout taskList;
    ArrayList<Task> tasks = new ArrayList<>();
    TextView progressBarText, timespanText;
    ProgressBar progressBar;
    int day = 0, month = 0, year = 0;
    LinearLayout buttonAdd, buttonClose;
    LinearLayout taskListShadow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_tasks);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Setting up view elements
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        taskList = (LinearLayout)findViewById(R.id.taskList);
        progressBarText = (TextView)findViewById(R.id.progressBarText);
        progressBar = (ProgressBar)findViewById(R.id.mprogressBar);
        timespanText = (TextView)findViewById(R.id.timespanText);
        buttonAdd = (LinearLayout)findViewById(R.id.buttonAdd);
        buttonClose = (LinearLayout)findViewById(R.id.buttonClose);
        taskListShadow = (LinearLayout)findViewById(R.id.taskListShadow);

        // Getting date info from previous view
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            day = extras.getInt("day");
            month = extras.getInt("month");
            year = extras.getInt("year");
        }

        // Processing "Close" button click
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Drawing task, etc.
        timespanText.setText(day + " " + getHumanMonthNameGenitive(month) + " " + year +
                " (" + getDayOfWeekStr(year, month, day) + ")");
        getTasks();
        redrawProgressBar();
    }

    protected void getTasks() {
        tasks.clear();
        taskList.removeAllViews();
        DB = tasksConnector.getReadableDatabase();
        CustomDate today = new CustomDate(year, month, day);
        String todayStr = today.toString();

        String columns[] = {"_id", "name", "type", "startDate", "startTime", "count",
                "reminder", "endDate", "shoppingList", "status", "currentCount", "shoppingListState"};

        Cursor cursor = DB.query("tasks", columns, "endDate = '" + todayStr + "'",
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
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        }
        DB.close();

        int num = 1;
        for (Task task: tasks) {
            LinearLayout taskRow = TaskView.getTaskRow(
                    DayTasks.this, num, task, true, true, Timespan.TODAY, new Callable() {
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
        if (tasks.size() == 0)
            taskListShadow.setVisibility(View.GONE);
        else
            taskListShadow.setVisibility(View.VISIBLE);
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

    @Override
    protected void onResume() {
        super.onResume();
        getTasks();
        redrawProgressBar();
    }
}
