package nosfie.easyorg.NewTask;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;

public class NewTaskThirdScreen extends AppCompatActivity {

    Calendar dateAndTime = Calendar.getInstance();
    RadioGroup deadlineRadioGroup;
    TextView customDate;
    Task task = new Task();
    RadioButton optionDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_third_screen);

        customDate = (TextView)findViewById(R.id.customDate);
        optionDay = (RadioButton)findViewById(R.id.optionDay);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = new Task(extras);
        }

        Button button_back = (Button)findViewById(R.id.buttonBack);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskThirdScreen.this, NewTaskSecondScreen.class);
                task.deadline = Task.DEADLINE.TODAY;
                intent = task.formIntent(intent, task);
                startActivity(intent);
            }
        });

        Button button_complete = (Button)findViewById(R.id.buttonComplete);
        button_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskThirdScreen.this, MainActivity.class);
                task.insertIntoDatabase(getApplicationContext());
                intent.putExtra("toast", "Задача успешно добавлена!");
                startActivity(intent);
            }
        });

        deadlineRadioGroup = (RadioGroup)findViewById(R.id.deadlineRadioGroup);
        deadlineRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                switch (idx) {
                    case 0:
                        task.deadline = Task.DEADLINE.TODAY;
                        customDate.setText("Не выбрана");
                        break;
                    case 1:
                        task.deadline = Task.DEADLINE.WEEK;
                        customDate.setText("Не выбрана");
                        break;
                    case 2:
                        task.deadline = Task.DEADLINE.MONTH;
                        customDate.setText("Не выбрана");
                        break;
                    case 3:
                        task.deadline = Task.DEADLINE.YEAR;
                        customDate.setText("Не выбрана");
                        break;
                    case 4:
                        task.deadline = Task.DEADLINE.NONE;
                        customDate.setText("Не выбрана");
                        break;
                    case 5:
                        task.deadline = Task.DEADLINE.CUSTOM;
                        setDate();
                        break;
                }
            }
        });
    }

    public void setDate() {
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        NewTaskThirdScreen.this,
                        AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                        onDateSetListener,
                        dateAndTime.get(Calendar.YEAR),
                        dateAndTime.get(Calendar.MONTH),
                        dateAndTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setOnCancelListener(onDateCancelListener);
        datePickerDialog.show();
    }

    DatePickerDialog.OnCancelListener onDateCancelListener = new DatePickerDialog.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            optionDay.setChecked(true);
            customDate.setText("Не выбрана");
            task.customEndDate.day = 0;
            task.customEndDate.month = 0;
            task.customEndDate.year = 0;
        }
    };

    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            customDate.setText(String.format("%02d", dayOfMonth) + "." +
                    String.format("%02d", monthOfYear) + "." +
                    String.format("%04d", year));
            task.customEndDate.day = dayOfMonth;
            task.customEndDate.month = monthOfYear;
            task.customEndDate.year = year;
        }
    };

}