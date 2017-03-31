package nosfie.easyorg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class NewTaskThirdScreen extends AppCompatActivity {

    String taskName, taskCount;
    NewTaskFirstScreen.TASK_TYPE taskType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_third_screen);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            taskName = extras.getString("taskName");
            taskCount = extras.getString("taskCount");
            String taskTypeString = extras.getString("taskType");
            switch (taskTypeString) {
                case "SIMPLE":
                    taskType = NewTaskFirstScreen.TASK_TYPE.SIMPLE;
                    break;
                case "SHOPPING_LIST":
                    taskType = NewTaskFirstScreen.TASK_TYPE.SHOPPING_LIST;
                    break;
                case "COUNTABLE":
                    taskType = NewTaskFirstScreen.TASK_TYPE.COUNTABLE;
                    break;
                default:
                    taskType = NewTaskFirstScreen.TASK_TYPE.SIMPLE;
                    break;
            }
        }

        Button button_back = (Button)findViewById(R.id.buttonBack);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskThirdScreen.this, NewTaskSecondScreen.class);
                intent.putExtra("taskName", taskName);
                intent.putExtra("taskType", taskType.toString());
                intent.putExtra("taskCount", taskCount);
                startActivity(intent);
            }
        });

        Button button_complete = (Button)findViewById(R.id.buttonComplete);
        button_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskThirdScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

}