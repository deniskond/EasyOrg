package nosfie.easyorg.TaskList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.R;

public class TaskList extends AppCompatActivity {

    TextView result;
    Button deleteDbButton;
    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    ArrayList<Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);

        result = (TextView)findViewById(R.id.testTextView);
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);

        getTasks();

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

        DB = tasksConnector.getReadableDatabase();

        String columns[] = {"_id", "name", "type", "startDate",
                "startTime", "count", "reminder", "endDate", "shoppingList"};

        Cursor cursor = DB.query("tasks", columns, null, null, null, null, "_id");

        if (cursor != null){
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getString(7),
                        cursor.getString(8)
                    );
                    tasks.add(task);
                    result.setText(
                        result.getText().toString() + "\n" +
                            cursor.getString(0) + ") " +
                            cursor.getString(1) + ", " +
                            cursor.getString(2) + ", " +
                            cursor.getString(3) + ", " +
                            cursor.getString(4) + ", " +
                            cursor.getInt(5) + ", " +
                            cursor.getInt(6) + ", " +
                            cursor.getString(7) + "\n" +
                            "shoppingList:\n" +
                            cursor.getString(8)
                    );
                } while (cursor.moveToNext());
            }
        }

        DB.close();
    }

}