package nosfie.easyorg.TaskList;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.DayValues;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Database.TasksConnector;
import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;
import nosfie.easyorg.R;

public class TaskList extends AppCompatActivity {

    private enum TIMESPAN {
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
    boolean ignoreTimeSet = false;

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
            timespan = TIMESPAN.valueOf(extras.getString("timespan"));
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
        result.setText("");
        DB = tasksConnector.getReadableDatabase();

        String columns[] = {"_id", "name", "type", "startDate", "startTime", "count",
                "reminder", "endDate", "shoppingList", "status", "currentCount", "shoppingListState"};

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
            result.setText(result.getText() + task.name + " " + task.shoppingList);
            addTaskRow(num, task);
            num++;
        }
        redrawProgressBar();
    }

    protected void addTaskRow(int num, final Task task) {
        // Main row
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
        params.height = convertDpToPixels(this, TASK_ROW_HEIGHT);
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
        number.setTypeface(null, Typeface.BOLD);
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
                color = R.color.colorTaskFailed;
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
        taskNameRow.setPadding(convertDpToPixels(this, 10), 0, 0, 0);
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
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        name.setText(task.name);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setLayoutParams(nameParams);
        taskNameRow.addView(name);

        if (task.type == Task.TYPE.COUNTABLE || task.type == Task.TYPE.SHOPPING_LIST) {
            TextView count = new TextView(this);
            LinearLayout.LayoutParams countParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
            count.setTextColor(0xFFFF0000);
            count.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            count.setText(" (" + task.currentCount + "/" + task.count + ")");
            count.setGravity(Gravity.CENTER_VERTICAL);
            count.setLayoutParams(countParams);
            taskNameRow.addView(count);
        }

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
        editImageParams.height = convertDpToPixels(this, 25);
        editImageParams.weight = 50;
        editImage.setLayoutParams(editImageParams);
        editImage.setPadding(5, 0, 0, 0);
        editImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        editImage.setAdjustViewBounds(true);
        editImage.setImageResource(R.drawable.edit_icon_small);
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processEditTaskClick(task);
            }
        });

        buttonsRow.addView(editImage);

        ImageView deleteImage = new ImageView(this);
        LinearLayout.LayoutParams deleteImageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        deleteImageParams.height = convertDpToPixels(this, 25);
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

    protected void processTaskNameClick(final Task task) {

        switch (task.type) {
            case SIMPLE:
                showSimpleTaskDialog(task);
                break;
            case COUNTABLE:
                showCountableTaskDialog(task);
                break;
            case SHOPPING_LIST:
                Intent intent = new Intent(this, ShoppingList.class);
                intent.putExtra("id", task.id);
                intent.putExtra("taskName", task.name + " " + task.customEndDate.toString());
                intent.putExtra("shoppingList", task.shoppingList);
                intent.putExtra("shoppingListState", task.shoppingListState);
                intent.putExtra("timespan", timespan.toString());
                startActivity(intent);
        }
    }

    protected void showCountableTaskDialog(final Task task) {
        // retrieve display dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        final Dialog countableDialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.countable_dialog, (ViewGroup)findViewById(R.id.countable_dialog_root));
        countableDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        countableDialog.setContentView(layout);

        final RadioButton radioActual = (RadioButton)layout.findViewById(R.id.radioActual);
        final RadioButton radioDone = (RadioButton)layout.findViewById(R.id.radioDone);
        final RadioButton radioNotDone = (RadioButton)layout.findViewById(R.id.radioNotDone);
        final RadioButton radioPostponed = (RadioButton)layout.findViewById(R.id.radioPostponed);
        LinearLayout countableDialogRoot = (LinearLayout)layout.findViewById(R.id.countable_dialog_root);
        countableDialogRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        final SeekBar seekBar = (SeekBar)layout.findViewById(R.id.countableSeekbar);
        seekBar.setProgress(task.currentCount);
        seekBar.setMax(task.count);
        final TextView countText = (TextView)layout.findViewById(R.id.count_text);
        countText.setText(task.currentCount + "/" + task.count);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                countText.setText(i + "/" + task.count);
                if (i == task.count) {
                    radioDone.setChecked(true);
                    radioActual.setEnabled(false);
                    radioNotDone.setEnabled(false);
                    radioPostponed.setEnabled(false);
                }
                else {
                    radioActual.setChecked(true);
                    radioActual.setEnabled(true);
                    radioNotDone.setEnabled(true);
                    radioPostponed.setEnabled(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBar.setProgress(task.count);
            }
        });

        switch (task.status) {
            case ACTUAL:
                radioActual.setChecked(true);
                break;
            case DONE:
                radioDone.setChecked(true);
                break;
            case NOT_DONE:
                radioNotDone.setChecked(true);
                break;
            case POSTPONED:
                radioPostponed.setChecked(true);
                break;
        }
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countableDialog.dismiss();
            }
        });
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioActual.isChecked())
                    task.status = Task.STATUS.ACTUAL;
                if (radioNotDone.isChecked())
                    task.status = Task.STATUS.NOT_DONE;
                if (radioPostponed.isChecked())
                    task.status = Task.STATUS.POSTPONED;

                task.currentCount = seekBar.getProgress();
                if (task.currentCount == task.count)
                    task.status = Task.STATUS.DONE;
                task.synchronize(getApplicationContext());
                getTasks();
                countableDialog.dismiss();
            }
        });
        countableDialog.show();
    }

    protected void showSimpleTaskDialog(final Task task) {
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
        final AlertDialog simpleTaskDialog;
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
        simpleTaskDialog = builder.create();
        simpleTaskDialog.show();
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

    protected void processEditTaskClick(Task task) {
        switch (task.type) {
            case SIMPLE:
                showSimpleTaskEditDialog(task);
                break;
            case SHOPPING_LIST:
                showShoppingListTaskEditDialog(task);
                break;
            case COUNTABLE:
                showCountableTaskEditDialog(task);
                break;
        }
    }

    protected void showSimpleTaskEditDialog(final Task task) {
        final CharSequence[] items = {
                "Название",
                "Дату начала",
                "Время начала",
                "Дату окончания",
                "Напоминание"};
        final AlertDialog simpleTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        showEditTaskNameDialog(task);
                        break;
                    case 1:
                        showEditTaskStartDateDialog(task);
                        break;
                    case 2:
                        showEditTaskStartTimeDialog(task);
                        break;
                    case 3:
                        showEditTaskEndDateDialog(task);
                        break;
                    case 4:
                        toggleTaskReminder(task);
                        break;
                }
                dialog.dismiss();
            }
        });
        simpleTaskDialog = builder.create();
        simpleTaskDialog.show();
    }

    protected void showEditTaskNameDialog(final Task task) {
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog editTextDialog = new Dialog(TaskList.this);
        LayoutInflater inflater = (LayoutInflater)TaskList.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.edit_task_name_dialog, (ViewGroup)findViewById(R.id.edit_name_root));
        editTextDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editTextDialog.setContentView(layout);
        final EditText editTask = (EditText)layout.findViewById(R.id.task_name);
        editTask.setText(task.name);
        LinearLayout editTextRoot = (LinearLayout)layout.findViewById(R.id.edit_name_root);
        editTextRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.name = editTask.getText().toString();
                task.synchronize(TaskList.this);
                editTextDialog.dismiss();
                getTasks();
                Toast.makeText(getApplicationContext(), "Задача обновлена", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextDialog.dismiss();
            }
        });
        editTextDialog.show();
    }


    protected void showEditTaskStartDateDialog(final Task task) {
        Calendar calendar = new GregorianCalendar(
            task.customStartDate.year,
            task.customStartDate.month - 1,
            task.customStartDate.day
        );

        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                task.startDate = Task.START_DATE.CUSTOM;
                task.customStartDate.year = year;
                task.customStartDate.month = month + 1;
                task.customStartDate.day = day;
                if (task.customStartDate.toString().compareTo(task.customEndDate.toString()) >= 0)
                    task.customEndDate = task.customStartDate;
                task.synchronize(TaskList.this);
                getTasks();
            }
        };
        DatePickerDialog datePickerDialog =
            new DatePickerDialog(this,
                onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - 1000);
        datePickerDialog.setTitle("Изменить дату начала задачи");
        datePickerDialog.show();
    }

    protected void showEditTaskStartTimeDialog(final Task task) {

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                if (!ignoreTimeSet) {
                    task.startTime = Task.START_TIME.CUSTOM;
                    task.customStartTime.hours = hours;
                    task.customStartTime.minutes = minutes;
                    task.synchronize(TaskList.this);
                    getTasks();
                    Toast.makeText(getApplicationContext(), "Задача обновлена", Toast.LENGTH_SHORT).show();
                }
            }
        };
        TimePickerDialog timePickerDialog =
            new TimePickerDialog(
                TaskList.this,
                onTimeSetListener,
                task.customStartTime.hours,
                task.customStartTime.minutes,
                true
            );
        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ignoreTimeSet = true;
                dialog.cancel();
            }
        });
        timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "ОК", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ignoreTimeSet = false;
                dialog.dismiss();
            }
        });
        timePickerDialog.show();
    }

    protected void showEditTaskEndDateDialog(final Task task) {
        Calendar calendar = new GregorianCalendar(
                task.customEndDate.year,
                task.customEndDate.month - 1,
                task.customEndDate.day
        );
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                task.deadline = Task.DEADLINE.CUSTOM;
                task.customEndDate.year = year;
                task.customEndDate.month = month + 1;
                task.customEndDate.day = day;
                if (task.customEndDate.toString().compareTo(task.customStartDate.toString()) <= 0)
                    task.customStartDate = task.customEndDate;
                task.synchronize(TaskList.this);
                getTasks();
            }
        };
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(this,
                        onDateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - 1000);
        datePickerDialog.setTitle("Изменить дату окончания задачи");
        datePickerDialog.show();
    }

    protected void toggleTaskReminder(Task task) {
        if (task.startTime.toString().equals("NONE"))
            Toast.makeText(getApplicationContext(), "Установите сначала время начала задачи",
                    Toast.LENGTH_SHORT).show();
        else {
            task.needReminder = !task.needReminder;
            task.synchronize(TaskList.this);
            getTasks();
            if (task.needReminder)
                Toast.makeText(getApplicationContext(), "Напоминание установлено", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Напоминание убрано", Toast.LENGTH_SHORT).show();
        }
    }

    protected void showCountableTaskEditDialog(final Task task) {
        final CharSequence[] items = {
                "Название",
                "Количественную цель",
                "Дату начала",
                "Время начала",
                "Дату окончания",
                "Напоминание"
        };
        final AlertDialog countableTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        showEditTaskNameDialog(task);
                        break;
                    case 1:
                        showEditTaskCountDialog(task);
                        break;
                    case 2:
                        showEditTaskStartDateDialog(task);
                        break;
                    case 3:
                        showEditTaskStartTimeDialog(task);
                        break;
                    case 4:
                        showEditTaskEndDateDialog(task);
                        break;
                    case 5:
                        toggleTaskReminder(task);
                        break;
                }
                dialog.dismiss();
            }
        });
        countableTaskDialog = builder.create();
        countableTaskDialog.show();
    }

    protected void showEditTaskCountDialog(final Task task) {
        Display display = getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog editCountDialog = new Dialog(TaskList.this);
        LayoutInflater inflater = (LayoutInflater)TaskList.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.edit_count_dialog, (ViewGroup)findViewById(R.id.edit_count_root));
        editCountDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editCountDialog.setContentView(layout);
        final EditText editTask = (EditText)layout.findViewById(R.id.task_count);
        editTask.setText(Integer.toString(task.count));
        LinearLayout editTextRoot = (LinearLayout)layout.findViewById(R.id.edit_count_root);
        editTextRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.count = Integer.parseInt(editTask.getText().toString());
                if (task.currentCount >= task.count) {
                    task.currentCount = task.count;
                    task.status = Task.STATUS.DONE;
                }
                task.synchronize(TaskList.this);
                editCountDialog.dismiss();
                getTasks();
                Toast.makeText(getApplicationContext(), "Задача обновлена", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editCountDialog.dismiss();
            }
        });
        editCountDialog.show();
    }

    protected void showShoppingListTaskEditDialog(final Task task) {
        final CharSequence[] items = {
                "Название",
                "Список покупок",
                "Дату начала",
                "Время начала",
                "Дату окончания",
                "Напоминание"
        };
        final AlertDialog countableTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        showEditTaskNameDialog(task);
                        break;
                    case 1:
                        Intent intent = new Intent(TaskList.this, EditShoppingList.class);
                        intent.putExtra("id", task.id);
                        intent.putExtra("taskName", task.name + " " + task.customEndDate.toString());
                        intent.putExtra("shoppingList", task.shoppingList);
                        intent.putExtra("timespan", timespan.toString());
                        intent.putExtra("returnActivity", "TaskList");
                        startActivity(intent);
                        break;
                    case 2:
                        showEditTaskStartDateDialog(task);
                        break;
                    case 3:
                        showEditTaskStartTimeDialog(task);
                        break;
                    case 4:
                        showEditTaskEndDateDialog(task);
                        break;
                    case 5:
                        toggleTaskReminder(task);
                        break;
                }
                dialog.dismiss();
            }
        });
        countableTaskDialog = builder.create();
        countableTaskDialog.show();
    }

}