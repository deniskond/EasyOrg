package nosfie.easyorg;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.NewTask.NewTaskFirstScreen;
import nosfie.easyorg.NewTask.NewTaskShoppingList;
import nosfie.easyorg.Settings.Settings;
import nosfie.easyorg.TaskCalendar.TaskCalendar;
import nosfie.easyorg.TaskList.ShoppingList;
import nosfie.easyorg.TaskList.TaskList;

public class MainActivity extends AppCompatActivity {

    TasksConnector tasksConnector;
    SQLiteDatabase DB;
    LinearLayout newTask, currentTaskList, settings, currentShoppingList, taskCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        DB = tasksConnector.getWritableDatabase();
        DB.execSQL(tasksConnector.CREATE_TABLE);
        DB.close();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String toast = extras.getString("toast");
            Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
        }

        newTask = (LinearLayout)findViewById(R.id.new_task_button);
        newTask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        newTask.setBackgroundColor(0xFFEEEEEE);
                        break;
                    case MotionEvent.ACTION_UP:
                        newTask.setBackgroundColor(0xFFFFFFFF);
                        Intent intent = new Intent(MainActivity.this, NewTaskFirstScreen.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        currentTaskList = (LinearLayout)findViewById(R.id.current_task_list);
        currentTaskList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        currentTaskList.setBackgroundColor(0xFFEEEEEE);
                        break;
                    case MotionEvent.ACTION_UP:
                        currentTaskList.setBackgroundColor(0xFFFFFFFF);
                        Intent intent = new Intent(MainActivity.this, TaskList.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        currentShoppingList = (LinearLayout)findViewById(R.id.current_shopping_list);
        currentShoppingList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        currentShoppingList.setBackgroundColor(0xFFEEEEEE);
                        break;
                    case MotionEvent.ACTION_UP:
                        currentShoppingList.setBackgroundColor(0xFFFFFFFF);
                        Task nearestShoppingList = getNearestShoppingList();
                        if (nearestShoppingList.name == null) {
                            Toast.makeText(getApplicationContext(), "Нет ни одного списка покупок!", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Intent intent = new Intent(MainActivity.this, ShoppingList.class);
                        intent.putExtra("id", nearestShoppingList.id);
                        intent.putExtra("taskName", nearestShoppingList.name + " "
                                + nearestShoppingList.customEndDate.toString());
                        intent.putExtra("shoppingList", nearestShoppingList.shoppingList);
                        intent.putExtra("shoppingListState", nearestShoppingList.shoppingListState);
                        intent.putExtra("returnActivity", "MainActivity");
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        settings = (LinearLayout)findViewById(R.id.settings_button);
        settings.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        settings.setBackgroundColor(0xFFEEEEEE);
                        break;
                    case MotionEvent.ACTION_UP:
                        settings.setBackgroundColor(0xFFFFFFFF);
                        Intent intent = new Intent(MainActivity.this, Settings.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        taskCalendar = (LinearLayout)findViewById(R.id.calendar);
        taskCalendar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        taskCalendar.setBackgroundColor(0xFFEEEEEE);
                        break;
                    case MotionEvent.ACTION_UP:
                        taskCalendar.setBackgroundColor(0xFFFFFFFF);
                        Intent intent = new Intent(MainActivity.this, TaskCalendar.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

    }

    Task getNearestShoppingList() {
        ArrayList<Task> tasks = new ArrayList<>();
        DB = tasksConnector.getReadableDatabase();
        String columns[] = {"_id", "name", "type", "startDate", "startTime", "count",
                "reminder", "endDate", "shoppingList", "status", "currentCount", "shoppingListState"};
        Cursor cursor = DB.query("tasks", columns, "type = 'SHOPPING_LIST'", null, null, null, "_id");
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

        if (tasks.size() == 0) {
            return new Task();
        }
        int searchResult = 0;
        long bestTimeDiff = getTimeDiff(tasks.get(searchResult));
        for (int i = 1; i < tasks.size(); i++) {
            long timeDiff = getTimeDiff(tasks.get(i));
            if (Math.abs(timeDiff) < Math.abs(bestTimeDiff) && !(timeDiff < 0 && bestTimeDiff > 0)) {
                searchResult = i;
                bestTimeDiff = timeDiff;
            }
        }
        return tasks.get(searchResult);
    }

    long getTimeDiff(Task task) {
        Calendar now = Calendar.getInstance();
        if (task.startTime == Task.START_TIME.NONE) {
            Calendar taskTime = new GregorianCalendar(
                task.customStartDate.year,
                task.customStartDate.month - 1,
                task.customStartDate.day
            );
            taskTime.add(Calendar.DAY_OF_MONTH, 1);
            taskTime.add(Calendar.HOUR_OF_DAY, 3);
            return taskTime.getTimeInMillis() - now.getTimeInMillis();
        }
        else {
            Calendar taskTime = new GregorianCalendar(
                task.customStartDate.year,
                task.customStartDate.month - 1,
                task.customStartDate.day,
                task.customStartTime.hours,
                task.customStartTime.minutes
            );
            return taskTime.getTimeInMillis() - now.getTimeInMillis();
        }
    }

}