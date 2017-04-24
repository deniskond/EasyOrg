package nosfie.easyorg;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.NewTask.NewTaskFirstScreen;
import nosfie.easyorg.TaskList.TaskList;

public class MainActivity extends AppCompatActivity {

    TasksConnector tasksConnector;
    SQLiteDatabase DB;

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

        final LinearLayout newTask = (LinearLayout)findViewById(R.id.new_task_button);
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

        final LinearLayout currentTaskList = (LinearLayout)findViewById(R.id.current_task_list);
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

    }

}