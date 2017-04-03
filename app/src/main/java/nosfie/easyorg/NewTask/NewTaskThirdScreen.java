package nosfie.easyorg.NewTask;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;

import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;

public class NewTaskThirdScreen extends AppCompatActivity {

    Calendar dateAndTime = Calendar.getInstance();
    RadioGroup deadlineRadioGroup;
    TextView customDate;
    Task task = new Task();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_third_screen);

        customDate = (TextView)findViewById(R.id.customDate);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = new Task(extras);
        }

        Button button_back = (Button)findViewById(R.id.buttonBack);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskThirdScreen.this, NewTaskSecondScreen.class);
                intent = task.formIntent(intent, task);
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

        deadlineRadioGroup = (RadioGroup)findViewById(R.id.deadlineRadioGroup);
        deadlineRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                switch (idx) {
                    case 5:
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
        datePickerDialog.show();
    }

    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

            customDate.setText(String.format("%02d", dayOfMonth) + "." +
                    String.format("%02d", monthOfYear) + "." +
                    String.format("%04d", year));
        }
    };

}