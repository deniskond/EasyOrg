package nosfie.easyorg;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class NewTaskSecondScreen extends AppCompatActivity {

    String taskName, taskCount;
    NewTaskFirstScreen.TASK_TYPE taskType;
    Button button_next, button_back;
    Calendar dateAndTime = Calendar.getInstance();
    RadioGroup startDateRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_second_screen);

        button_next = (Button)findViewById(R.id.buttonNext);
        button_back = (Button)findViewById(R.id.buttonBack);

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
                    button_next.setText("Готово");
                    TextView startDateText = (TextView)findViewById(R.id.startDateText);
                    startDateText.setText("Дата");
                    TextView startTimeText = (TextView)findViewById(R.id.startTimeText);
                    startTimeText.setText("Время");
                    break;
                case "COUNTABLE":
                    taskType = NewTaskFirstScreen.TASK_TYPE.COUNTABLE;
                    break;
                default:
                    taskType = NewTaskFirstScreen.TASK_TYPE.SIMPLE;
                    break;
            }
        }

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (taskType == NewTaskFirstScreen.TASK_TYPE.SHOPPING_LIST)
                    intent = new Intent(NewTaskSecondScreen.this, NewTaskShoppingList.class);
                else
                    intent = new Intent(NewTaskSecondScreen.this, NewTaskFirstScreen.class);
                intent.putExtra("taskName", taskName);
                intent.putExtra("taskType", taskType.toString());
                intent.putExtra("taskCount", taskCount);
                startActivity(intent);
            }
        });


        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (taskType == NewTaskFirstScreen.TASK_TYPE.SHOPPING_LIST)
                    intent = new Intent(NewTaskSecondScreen.this, MainActivity.class);
                else {
                    intent = new Intent(NewTaskSecondScreen.this, NewTaskThirdScreen.class);
                    intent.putExtra("taskName", taskName);
                    intent.putExtra("taskType", taskType.toString());
                    intent.putExtra("taskCount", taskCount);
                }
                startActivity(intent);
            }
        });

        startDateRadioGroup = (RadioGroup)findViewById(R.id.startDateRadioGroup);
        startDateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                switch (idx) {
                    case 2:
                        setDate();
                        break;
                }
            }
        });

    }

    public void setDate() {

        DatePickerDialog datePickerDialog =
            new DatePickerDialog(
                    NewTaskSecondScreen.this,
                    AlertDialog.THEME_HOLO_LIGHT,
                    onDateSetListener,
                    dateAndTime.get(Calendar.YEAR),
                    dateAndTime.get(Calendar.MONTH),
                    dateAndTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            Toast.makeText(getApplicationContext(), "EEEE", Toast.LENGTH_SHORT).show();
        }
    };

}