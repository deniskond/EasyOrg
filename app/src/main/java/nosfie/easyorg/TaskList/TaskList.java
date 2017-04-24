package nosfie.easyorg.TaskList;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.DayValues;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.R;

public class TaskList extends AppCompatActivity {

    public enum TIMESPAN {
        TODAY, WEEK, MONTH, YEAR, UNLIMITED
    }

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
    TIMESPAN timespan = TIMESPAN.TODAY;
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
        result.setText("");
        DB = tasksConnector.getReadableDatabase();

        String columns[] = {"_id", "name", "type", "startDate", "startTime", "count",
                "reminder", "endDate", "shoppingList", "status", "currentcount"};

        Cursor cursor = DB.query("tasks", columns, null, null, null, null, "_id");

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
                        cursor.getInt(10)
                    );
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        }
        DB.close();

        tasks = filterTasksByTimespan(tasks, timespan);
        int num = 1;
        for (Task task: tasks) {
            addTaskRow(num, task);
            num++;
        }
        redrawProgressBar();
    }

    protected void addTaskRow(int num, final Task task) {

        // Main row
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
        params.height = convertDpToPixels(TASK_ROW_HEIGHT);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        row.setLayoutParams(params);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundColor(0xFFAAAAAA);
        row.setWeightSum(100);
        row.setPadding(0, 0, 0, 1);

        // Number Row
        LinearLayout numberRow = new LinearLayout(this);
        LinearLayout.LayoutParams numberRowParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        numberRowParams.setMargins(0, 0, 1, 0);
        numberRowParams.weight = 10;
        numberRow.setLayoutParams(numberRowParams);
        numberRow.setBackgroundColor(0xFFFFFFFF);
        numberRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView number = new TextView(this);
        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        number.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        number.setText(Integer.toString(num));
        number.setGravity(Gravity.CENTER);
        number.setLayoutParams(numberParams);

        numberRow.addView(number);

        row.addView(numberRow);

        // Task name row
        LinearLayout taskNameRow = new LinearLayout(this);
        LinearLayout.LayoutParams taskNameParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        taskNameParams.setMargins(0, 0, 1, 0);
        taskNameParams.weight = 65;
        taskNameRow.setLayoutParams(taskNameParams);

        int color = 0;
        switch (task.status) {
            case ACTUAL:
                color = R.color.colorTaskActual;
                break;
            case DONE:
                color = R.color.colorTaskDone;
                break;
            case NOT_DONE:
                color = R.color.colorTaskDone;
                break;
            case IN_PROCESS:
                color = R.color.colorTaskInProcess;
                break;
            case POSTPONED:
                color = R.color.colorTaskPostponed;
                break;
        }
        taskNameRow.setBackgroundColor(getResources().getColor(color));

        taskNameRow.setOrientation(LinearLayout.HORIZONTAL);
        taskNameRow.setPadding(convertDpToPixels(10), 0, 0, 0);
        taskNameRow.setId(task.id);

        taskNameRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processTaskNameClick(task);
            }
        });

        TextView name = new TextView(this);
        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        name.setText(task.name);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setLayoutParams(nameParams);


        taskNameRow.addView(name);

        row.addView(taskNameRow);

        // Buttons row
        LinearLayout buttonsRow = new LinearLayout(this);
        LinearLayout.LayoutParams buttonsRowParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        buttonsRowParams.setMargins(0, 0, 1, 0);
        buttonsRowParams.weight = 25;
        buttonsRow.setLayoutParams(buttonsRowParams);
        buttonsRow.setBackgroundColor(0xFFFFFFFF);
        buttonsRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonsRow.setWeightSum(100);
        buttonsRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView editImage = new ImageView(this);
        LinearLayout.LayoutParams editImageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        editImageParams.height = convertDpToPixels(25);
        editImageParams.weight = 50;
        editImage.setLayoutParams(editImageParams);
        editImage.setPadding(5, 0, 0, 0);
        editImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        editImage.setAdjustViewBounds(true);
        editImage.setImageResource(R.drawable.edit_icon_small);

        buttonsRow.addView(editImage);

        ImageView deleteImage = new ImageView(this);
        LinearLayout.LayoutParams deleteImageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        deleteImageParams.height = convertDpToPixels(25);
        deleteImageParams.weight = 50;
        deleteImage.setLayoutParams(deleteImageParams);
        deleteImage.setPadding(0, 0, 5, 0);
        deleteImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        deleteImage.setAdjustViewBounds(true);
        deleteImage.setImageResource(R.drawable.delete_icon_small);

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processDeleteImageClick(task);
            }
        });

        buttonsRow.addView(deleteImage);

        row.addView(buttonsRow);
        row.setId(1000 + task.id);

        taskList.addView(row);

    }

    protected int convertDpToPixels(int dp) {
        return (int) (dp * scale + 0.5f);
    }

    protected void processTaskNameClick(final Task task) {

        int status = 0;
        for (Task t: tasks) {
            if (t.id == task.id) {
                switch (task.status) {
                    case ACTUAL:
                        status = 0;
                        break;
                    case DONE:
                        status = 1;
                        break;
                    case NOT_DONE:
                        status = 2;
                        break;
                    case IN_PROCESS:
                        status = 3;
                        break;
                    case POSTPONED:
                        status = 4;
                        break;
                }
                break;
            }
        }

        final CharSequence[] items = {"Актуальна", "Выполнена", "Не будет выполнена", "Частично выполнена", "Отложена"};
        final AlertDialog levelDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Отметить статус задачи");

        builder.setSingleChoiceItems(items, status, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                int color = 0;
                switch (item)
                {
                    case 0:
                        color = R.color.colorTaskActual;
                        task.status = Task.STATUS.ACTUAL;
                        break;
                    case 1:
                        color = R.color.colorTaskDone;
                        task.status = Task.STATUS.DONE;
                        break;
                    case 2:
                        color = R.color.colorTaskFailed;
                        task.status = Task.STATUS.NOT_DONE;
                        break;
                    case 3:
                        color = R.color.colorTaskInProcess;
                        task.status = Task.STATUS.IN_PROCESS;
                        break;
                    case 4:
                        color = R.color.colorTaskPostponed;
                        task.status = Task.STATUS.POSTPONED;
                        break;
                }
                LinearLayout taskRow = (LinearLayout)findViewById(task.id);
                taskRow.setBackgroundColor(getResources().getColor(color));
                task.synchronize(getApplicationContext());
                redrawProgressBar();
                dialog.dismiss();
            }
        });
        levelDialog = builder.create();
        levelDialog.show();
    }

    protected void processDeleteImageClick(final Task task) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("Подтвердите удаление");  // заголовок
        ad.setMessage("Вы действительно хотите удалить задачу \"" + task.name + "\" ?"); // сообщение
        ad.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                task.delete(getApplicationContext());
                LinearLayout taskRow = (LinearLayout)findViewById(1000 + task.id);
                ((ViewManager)taskRow.getParent()).removeView(taskRow);
                Toast.makeText(getApplicationContext(), "Задача удалена", Toast.LENGTH_SHORT).show();
            }
        });
        ad.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                ///
            }
        });
        ad.setCancelable(true);
        ad.show();
    }

    protected void setTimespanClickListeners() {
        LinearLayout timespanOptionToday = (LinearLayout)findViewById(R.id.timespanOptionToday);
        timespanOptionToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Задачи на сегодня");
                timespan = TIMESPAN.TODAY;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionWeek = (LinearLayout)findViewById(R.id.timespanOptionWeek);
        timespanOptionWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Задачи на неделю");
                timespan = TIMESPAN.WEEK;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionMonth = (LinearLayout)findViewById(R.id.timespanOptionMonth);
        timespanOptionMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Задачи на месяц");
                timespan = TIMESPAN.MONTH;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionYear = (LinearLayout)findViewById(R.id.timespanOptionYear);
        timespanOptionYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Задачи на год");
                timespan = TIMESPAN.YEAR;
                getTasks();
                timespanSelector.setVisibility(View.INVISIBLE);
            }
        });

        LinearLayout timespanOptionUnlimited = (LinearLayout)findViewById(R.id.timespanOptionUnlimited);
        timespanOptionUnlimited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timespanText.setText("Бессрочные задачи");
                timespan = TIMESPAN.UNLIMITED;
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

        if (timespan == TIMESPAN.TODAY)
            optionTodayImage.setImageResource(R.drawable.tick_icon);
        else
            optionTodayImage.setImageResource(R.drawable.empty_tick_icon);

        if (timespan == TIMESPAN.WEEK)
            optionWeekImage.setImageResource(R.drawable.tick_icon);
        else
            optionWeekImage.setImageResource(R.drawable.empty_tick_icon);

        if (timespan == TIMESPAN.MONTH)
            optionMonthImage.setImageResource(R.drawable.tick_icon);
        else
            optionMonthImage.setImageResource(R.drawable.empty_tick_icon);

        if (timespan == TIMESPAN.YEAR)
            optionYearImage.setImageResource(R.drawable.tick_icon);
        else
            optionYearImage.setImageResource(R.drawable.empty_tick_icon);

        if (timespan == TIMESPAN.UNLIMITED)
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

    protected ArrayList<Task> filterTasksByTimespan(ArrayList<Task> tasks, TIMESPAN timespan) {
        if (timespan == TIMESPAN.UNLIMITED) {
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
        if (timespan == TIMESPAN.TODAY)
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
        if (timespan == TIMESPAN.WEEK)
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
        if (timespan == TIMESPAN.MONTH)
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

}