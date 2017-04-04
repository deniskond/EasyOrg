package nosfie.easyorg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import nosfie.easyorg.NewTask.NewTaskFirstScreen;
import nosfie.easyorg.TaskList.TaskList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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