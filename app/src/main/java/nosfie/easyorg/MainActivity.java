package nosfie.easyorg;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import nosfie.easyorg.Settings.Settings;
import nosfie.easyorg.ShoppingLists.ShoppingLists;
import nosfie.easyorg.TaskCalendar.TaskCalendar;
import nosfie.easyorg.TaskList.ShoppingList;
import nosfie.easyorg.TaskList.TaskList;

public class MainActivity extends AppCompatActivity {

    TasksConnector tasksConnector;
    SQLiteDatabase DB;
    LinearLayout newTask, currentTaskList, settings, shoppingLists, taskCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // First start actions;
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        DB = tasksConnector.getWritableDatabase();
        DB.execSQL(tasksConnector.CREATE_TABLE);
        DB.close();

        // Showing toast text (if exists)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String toast = extras.getString("toast");
            Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
        }

        // Setting up view elements
        newTask = (LinearLayout)findViewById(R.id.newTask);
        currentTaskList = (LinearLayout)findViewById(R.id.currentTaskList);
        shoppingLists = (LinearLayout)findViewById(R.id.shoppingLists);
        settings = (LinearLayout)findViewById(R.id.settings);
        taskCalendar = (LinearLayout)findViewById(R.id.calendar);

        // Setting up navigation button listeners
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
        shoppingLists.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        shoppingLists.setBackgroundColor(0xFFEEEEEE);
                        break;
                    case MotionEvent.ACTION_UP:
                        shoppingLists.setBackgroundColor(0xFFFFFFFF);
                        Intent intent = new Intent(MainActivity.this, ShoppingLists.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
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

}