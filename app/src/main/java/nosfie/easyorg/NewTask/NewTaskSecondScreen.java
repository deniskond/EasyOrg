package nosfie.easyorg.NewTask;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;

public class NewTaskSecondScreen extends AppCompatActivity {

    Button button_next, button_back;
    Calendar dateAndTime = Calendar.getInstance();
    RadioGroup startDateRadioGroup, startTimeRadioGroup;
    TextView customDate, customTime;
    Task task = new Task();
    CheckBox reminderCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_second_screen);

        button_next = (Button)findViewById(R.id.buttonNext);
        button_back = (Button)findViewById(R.id.buttonBack);
        customDate = (TextView)findViewById(R.id.customDate);
        customTime = (TextView)findViewById(R.id.customTime);
        reminderCheckbox = (CheckBox)findViewById(R.id.reminderCheckbox);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = new Task(extras);

            switch (task.startDate) {
                case TODAY:
                    RadioButton optionToday = (RadioButton)findViewById(R.id.optionToday);
                    optionToday.setChecked(true);
                    break;
                case TOMORROW:
                    RadioButton optionTomorrow = (RadioButton)findViewById(R.id.optionTomorrow);
                    optionTomorrow.setChecked(true);
                    break;
                case CUSTOM:
                    RadioButton optionCustomDate = (RadioButton)findViewById(R.id.optionCustomDate);
                    optionCustomDate.setChecked(true);
                    customDate.setText(String.format("%02d", task.customStartDate.day) + "." +
                        String.format("%02d", task.customStartDate.month) + "." +
                        String.format("%04d", task.customStartDate.year));
                    break;
            }

            switch (task.startTime) {
                case NONE:
                    RadioButton optionDuringDay = (RadioButton)findViewById(R.id.optionDuringDay);
                    optionDuringDay.setChecked(true);
                    break;
                case CUSTOM:
                    RadioButton optionCustomTime = (RadioButton)findViewById(R.id.optionCustomTime);
                    optionCustomTime.setChecked(true);
                    customTime.setText(String.format("%02d", task.customStartTime.hours) + ":" +
                            String.format("%02d", task.customStartTime.minutes));
                    break;
            }

            reminderCheckbox.setChecked(task.needReminder);

            if (task.type == Task.TYPE.SHOPPING_LIST) {
                button_next.setText("Готово");
                TextView startDateText = (TextView)findViewById(R.id.startDateText);
                startDateText.setText("Дата");
                TextView startTimeText = (TextView)findViewById(R.id.startTimeText);
                startTimeText.setText("Время");
            }
        }

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (task.type == Task.TYPE.SHOPPING_LIST)
                    intent = new Intent(NewTaskSecondScreen.this, NewTaskShoppingList.class);
                else
                    intent = new Intent(NewTaskSecondScreen.this, NewTaskFirstScreen.class);
                task.startDate = Task.START_DATE.TODAY;
                task.startTime = Task.START_TIME.NONE;
                task.needReminder = false;
                intent = task.formIntent(intent, task);
                startActivity(intent);
            }
        });


        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (task.type == Task.TYPE.SHOPPING_LIST) {
                    intent = new Intent(NewTaskSecondScreen.this, MainActivity.class);
                    task.insertIntoDatabase(getApplicationContext());
                    intent.putExtra("toast", "Задача успешно добавлена!");
                }
                else {
                    intent = new Intent(NewTaskSecondScreen.this, NewTaskThirdScreen.class);
                    task.needReminder = reminderCheckbox.isChecked();
                    intent = task.formIntent(intent, task);
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
                    case 0:
                        task.startDate = Task.START_DATE.TODAY;
                        break;
                    case 1:
                        task.startDate = Task.START_DATE.TOMORROW;
                        break;
                    case 2:
                        task.startDate = Task.START_DATE.CUSTOM;
                        setDate();
                        break;
                }
            }
        });

        startTimeRadioGroup = (RadioGroup)findViewById(R.id.startTimeRadioGroup);
        startTimeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                switch (idx) {
                    case 0:
                        task.startTime = Task.START_TIME.NONE;
                        break;
                    case 1:
                        task.startTime = Task.START_TIME.CUSTOM;
                        setTime();
                        break;
                }
            }
        });

    }

    public void setDate() {
        DatePickerDialog datePickerDialog =
            new DatePickerDialog(
                    NewTaskSecondScreen.this,
                    AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                    onDateSetListener,
                    dateAndTime.get(Calendar.YEAR),
                    dateAndTime.get(Calendar.MONTH),
                    dateAndTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            customDate.setText(String.format("%02d", dayOfMonth) + "." +
                    String.format("%02d", monthOfYear) + "." +
                    String.format("%04d", year));
            task.customStartDate.day = dayOfMonth;
            task.customStartDate.month = monthOfYear;
            task.customStartDate.year = year;
        }
    };

    public void setTime() {
        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        NewTaskSecondScreen.this,
                        onTimeSetListener,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE),
                        true
                );
        timePickerDialog.show();
    }

    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            customTime.setText(String.format("%02d", hours) + ":" +
                    String.format("%02d", minutes));
            task.customStartTime.hours = hours;
            task.customStartTime.minutes = minutes;
        }
    };

}