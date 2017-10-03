package nosfie.easyorg.TaskCalendar;

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

import nosfie.easyorg.DataStructures.CustomDate;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;
import nosfie.easyorg.NewTask.NewTaskFirstScreen;
import nosfie.easyorg.R;
import nosfie.easyorg.TaskList.TaskView;

import static nosfie.easyorg.Database.Queries.getDayTasksFromDB;
import static nosfie.easyorg.Helpers.DateStringsHelper.*;

public class DayTasks extends AppCompatActivity {

    LinearLayout taskList;
    ArrayList<Task> tasks = new ArrayList<>();
    TextView progressBarText, timespanText;
    ProgressBar progressBar;
    int day = 0, month = 0, year = 0;
    LinearLayout addTaskButton, buttonClose;
    LinearLayout taskListShadow;
    ImageView addTaskImage;
    String humanDayString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_tasks);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Setting up view elements
        taskList = (LinearLayout)findViewById(R.id.taskList);
        progressBarText = (TextView)findViewById(R.id.progressBarText);
        progressBar = (ProgressBar)findViewById(R.id.mprogressBar);
        timespanText = (TextView)findViewById(R.id.timespanText);
        addTaskButton = (LinearLayout)findViewById(R.id.addTaskButton);
        buttonClose = (LinearLayout)findViewById(R.id.buttonClose);
        taskListShadow = (LinearLayout)findViewById(R.id.taskListShadow);
        addTaskImage = (ImageView)findViewById(R.id.addTaskImage);

        // Getting date info from previous view
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            day = extras.getInt("day");
            month = extras.getInt("month");
            year = extras.getInt("year");
            humanDayString = day + " " + getHumanMonthNameGenitive(month) + " " + year +
                    " (" + getDayOfWeekStr(year, month, day) + ")";
        }

        // Processing "Close" button click
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // "Add task" button onClickListener
        addTaskButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        addTaskImage.setImageResource(R.drawable.plus_dark);
                        break;
                    case MotionEvent.ACTION_UP:
                        addTaskImage.setImageResource(R.drawable.plus);
                        Intent intent = new Intent(DayTasks.this, NewTaskFirstScreen.class);
                        CustomDate date = new CustomDate(year, month, day);
                        intent.putExtra("predefinedDate", date.toString());
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        // Drawing task, etc.
        timespanText.setText(humanDayString);
        getTasks();
        redrawProgressBar();
    }

    protected void getTasks() {
        tasks.clear();
        taskList.removeAllViews();
        CustomDate today = new CustomDate(year, month, day);
        String todayStr = today.toString();
        tasks = getDayTasksFromDB(this, todayStr);

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
                    }, new Callable() {
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
