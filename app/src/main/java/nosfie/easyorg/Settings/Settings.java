package nosfie.easyorg.Settings;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.MainActivity;
import nosfie.easyorg.NewTask.NewTaskSecondScreen;
import nosfie.easyorg.NewTask.NewTaskShoppingList;
import nosfie.easyorg.R;

public class Settings extends AppCompatActivity {

    Button addColumnButton;
    SQLiteDatabase DB;
    TasksConnector tasksConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);

        Button addColumnButton = (Button)findViewById(R.id.addColumnButton);
        addColumnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB = tasksConnector.getWritableDatabase();
                DB.execSQL("ALTER TABLE tasks ADD COLUMN currentcount INT");
                DB.close();
            }
        });
    }

}
