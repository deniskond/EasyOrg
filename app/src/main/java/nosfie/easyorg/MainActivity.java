package nosfie.easyorg;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        LinearLayout newTask = (LinearLayout)findViewById(R.id.new_task_button);
        newTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewTaskFirstScreen.class);
                startActivity(intent);
            }
        });

        LinearLayout currentTaskList = (LinearLayout)findViewById(R.id.current_task_list);
        currentTaskList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TaskList.class);
                startActivity(intent);
            }
        });

    }

}