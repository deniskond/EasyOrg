package nosfie.easyorg.NewTask;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;

public class NewTaskSecondScreen extends AppCompatActivity {

    LinearLayout buttonNext, buttonBack, buttonCancel;
    Calendar dateAndTime = Calendar.getInstance();
    TextView customDate, customTime, buttonNextText, customDeadlineDate;
    Task task = new Task();
    Boolean deadlineChanged = false;
    // Starting date elements
    LinearLayout startToday, startTomorrow, startCustom;
    ImageView startTodayRadio, startTomorrowRadio, startCustomRadio;
    // Starting time elements
    LinearLayout timeDuringDay, timeCustom, needReminder;
    ImageView timeDuringDayRadio, timeCustomRadio, needReminderImage;
    TextView needReminderText;
    // Deadline elements
    LinearLayout deadlineDay, deadlineWeek, deadlineMonth, deadlineYear, deadlineCustom;
    ImageView deadlineDayRadio, deadlineWeekRadio, deadlineMonthRadio, deadlineYearRadio, deadlineCustomRadio;
    ArrayList<ImageView>
            startingDateList = new ArrayList<>(),
            startingTimeList = new ArrayList<>(),
            deadlineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_second_screen);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Setting up view elements
        buttonNext = (LinearLayout)findViewById(R.id.buttonNext);
        buttonNextText = (TextView)findViewById(R.id.buttonNextText);
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);
        buttonCancel = (LinearLayout)findViewById(R.id.buttonCancel);
        customDate = (TextView)findViewById(R.id.customDate);
        customTime = (TextView)findViewById(R.id.customTime);
        //reminderCheckbox = (CheckBox)findViewById(R.id.reminderCheckbox);
        customDeadlineDate = (TextView)findViewById(R.id.customDeadlineDate);
        //// Starting date elements
        startToday = (LinearLayout)findViewById(R.id.startToday);
        startTomorrow = (LinearLayout)findViewById(R.id.startTomorrow);
        startCustom = (LinearLayout)findViewById(R.id.startCustom);
        startTodayRadio = (ImageView)findViewById(R.id.startTodayRadio);
        startTomorrowRadio = (ImageView)findViewById(R.id.startTomorrowRadio);
        startCustomRadio = (ImageView)findViewById(R.id.startCustomRadio);
        //// Starting time elements
        timeDuringDay = (LinearLayout)findViewById(R.id.timeDuringDay);
        timeCustom = (LinearLayout)findViewById(R.id.timeCustom);
        timeDuringDayRadio = (ImageView)findViewById(R.id.timeDuringDayRadio);
        timeCustomRadio = (ImageView)findViewById(R.id.timeCustomRadio);
        needReminder = (LinearLayout)findViewById(R.id.needReminder);
        needReminderImage = (ImageView)findViewById(R.id.needReminderImage);
        needReminderText = (TextView)findViewById(R.id.needReminderText);
        //// Deadline elements
        deadlineDay = (LinearLayout)findViewById(R.id.deadlineDay);
        deadlineWeek = (LinearLayout)findViewById(R.id.deadlineWeek);
        deadlineMonth = (LinearLayout)findViewById(R.id.deadlineMonth);
        deadlineYear = (LinearLayout)findViewById(R.id.deadlineYear);
        deadlineCustom = (LinearLayout)findViewById(R.id.deadlineCustom);
        deadlineDayRadio = (ImageView)findViewById(R.id.deadlineDayRadio);
        deadlineWeekRadio = (ImageView)findViewById(R.id.deadlineWeekRadio);
        deadlineMonthRadio = (ImageView)findViewById(R.id.deadlineMonthRadio);
        deadlineYearRadio = (ImageView)findViewById(R.id.deadlineYearRadio);
        deadlineCustomRadio = (ImageView)findViewById(R.id.deadlineCustomRadio);

        // Filling ImageViews (radios) lists
        startingDateList.add(startTodayRadio);
        startingDateList.add(startTomorrowRadio);
        startingDateList.add(startCustomRadio);
        startingTimeList.add(timeDuringDayRadio);
        startingTimeList.add(timeCustomRadio);
        deadlineList.add(deadlineDayRadio);
        deadlineList.add(deadlineWeekRadio);
        deadlineList.add(deadlineMonthRadio);
        deadlineList.add(deadlineYearRadio);
        deadlineList.add(deadlineCustomRadio);

        // Passing task info
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = new Task(extras);
        }

        // Setting up navigation buttons
        buttonBack.setOnClickListener(new View.OnClickListener() {
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
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Processing start date "radio" checked change
        startToday.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startToday.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        startToday.setBackgroundColor(0x00000000);
                        task.startDate = Task.START_DATE.TODAY;
                        customDate.setText("Не выбрана");
                        selectRadioOption(startingDateList, 0);
                        break;
                }
                return true;
            }
        });
        startTomorrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTomorrow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        startTomorrow.setBackgroundColor(0x00000000);
                        task.startDate = Task.START_DATE.TOMORROW;
                        customDate.setText("Не выбрана");
                        selectRadioOption(startingDateList, 1);
                        break;
                }
                return true;
            }
        });
        startCustom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startCustom.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        startCustom.setBackgroundColor(0x00000000);
                        selectRadioOption(startingDateList, 2);
                        task.startDate = Task.START_DATE.CUSTOM;
                        setDate();
                        break;
                }
                return true;
            }
        });

        // Processing start time section clicks
        timeDuringDay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timeDuringDay.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        timeDuringDay.setBackgroundColor(0x00000000);
                        selectRadioOption(startingTimeList, 0);
                        task.startTime = Task.START_TIME.NONE;
                        needReminderImage.setImageResource(R.drawable.checkbox_unchecked_medium);
                        needReminderText.setTextColor(0xFF999999);
                        task.needReminder = false;
                        customTime.setText("Не выбрано");
                        break;
                }
                return true;
            }
        });
        timeCustom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timeCustom.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        timeCustom.setBackgroundColor(0x00000000);
                        selectRadioOption(startingTimeList, 1);
                        task.startTime = Task.START_TIME.CUSTOM;
                        needReminderText.setTextColor(0xFF555555);
                        setTime();
                        break;
                }
                return true;
            }
        });
        needReminder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        needReminder.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        needReminder.setBackgroundColor(0x00000000);
                        if (task.startTime == Task.START_TIME.CUSTOM) {
                            if (task.needReminder == true) {
                                task.needReminder = false;
                                needReminderImage.setImageResource(R.drawable.checkbox_unchecked_medium);
                            } else {
                                task.needReminder = true;
                                needReminderImage.setImageResource(R.drawable.checkbox_checked_medium);
                            }
                        }
                        break;
                }
                return true;
            }
        });

        // Processing deadline radio checked change
        deadlineDay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        deadlineDay.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        deadlineDay.setBackgroundColor(0x00000000);
                        task.deadline = Task.DEADLINE.DAY;
                        customDeadlineDate.setText("Не выбрана");
                        selectRadioOption(deadlineList, 0);
                        deadlineChanged = true;
                        break;
                }
                return true;
            }
        });
        deadlineWeek.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        deadlineWeek.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        deadlineWeek.setBackgroundColor(0x00000000);
                        task.deadline = Task.DEADLINE.WEEK;
                        customDeadlineDate.setText("Не выбрана");
                        selectRadioOption(deadlineList, 1);
                        deadlineChanged = true;
                        break;
                }
                return true;
            }
        });
        deadlineMonth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        deadlineMonth.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        deadlineMonth.setBackgroundColor(0x00000000);
                        task.deadline = Task.DEADLINE.MONTH;
                        customDeadlineDate.setText("Не выбрана");
                        selectRadioOption(deadlineList, 2);
                        deadlineChanged = true;
                        break;
                }
                return true;
            }
        });
        deadlineYear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        deadlineYear.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        deadlineYear.setBackgroundColor(0x00000000);
                        task.deadline = Task.DEADLINE.YEAR;
                        customDeadlineDate.setText("Не выбрана");
                        selectRadioOption(deadlineList, 3);
                        deadlineChanged = true;
                        break;
                }
                return true;
            }
        });
        deadlineCustom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        deadlineCustom.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        deadlineCustom.setBackgroundColor(0x00000000);
                        task.deadline = Task.DEADLINE.CUSTOM;
                        setDeadlineDate();
                        selectRadioOption(deadlineList, 4);
                        deadlineChanged = true;
                        break;
                }
                return true;
            }
        });

        // Setting finishing onTouch listener
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonNext.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNextClicked, null));
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonNext.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNext, null));
                        if (!deadlineChanged)
                            task.deadline = Task.DEADLINE.DAY;
                        Intent intent;
                        intent = new Intent(NewTaskSecondScreen.this, MainActivity.class);
                        task.insertIntoDatabase(getApplicationContext());
                        intent.putExtra("toast", "Задача успешно добавлена!");
                        startActivity(intent);
                        break;
                }
                return true;
            }
        };
        buttonNext.setOnTouchListener(onTouchListener);
        buttonNextText.setOnTouchListener(onTouchListener);
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
        datePickerDialog.setOnCancelListener(onDateCancelListener);
        datePickerDialog.show();
    }

    DatePickerDialog.OnCancelListener onDateCancelListener = new DatePickerDialog.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            selectRadioOption(startingDateList, 0);
            task.startDate = Task.START_DATE.TODAY;
            customDate.setText("Не выбрана");
            task.customStartDate.day = 0;
            task.customStartDate.month = 0;
            task.customStartDate.year = 0;
        }
    };

    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            customDate.setText(String.format("%02d", dayOfMonth) + "." +
                    String.format("%02d", monthOfYear + 1) + "." +
                    String.format("%04d", year));
            task.customStartDate.day = dayOfMonth;
            task.customStartDate.month = monthOfYear + 1;
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
        timePickerDialog.setOnCancelListener(onTimeCancelListener);
        timePickerDialog.show();
    }

    TimePickerDialog.OnCancelListener onTimeCancelListener = new TimePickerDialog.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            selectRadioOption(startingTimeList, 0);
            task.startTime = Task.START_TIME.NONE;
            customTime.setText("Не выбрано");
            task.customStartTime.hours = 0;
            task.customStartTime.minutes = 0;
        }
    };

    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            customTime.setText(String.format("%02d", hours) + ":" +
                    String.format("%02d", minutes));
            task.customStartTime.hours = hours;
            task.customStartTime.minutes = minutes;
        }
    };

    public void setDeadlineDate() {
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        NewTaskSecondScreen.this,
                        AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                        onDeadlineDateSetListener,
                        dateAndTime.get(Calendar.YEAR),
                        dateAndTime.get(Calendar.MONTH),
                        dateAndTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setOnCancelListener(onDeadlineDateCancelListener);
        datePickerDialog.show();
    }

    DatePickerDialog.OnCancelListener onDeadlineDateCancelListener = new DatePickerDialog.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            selectRadioOption(deadlineList, 0);
            task.deadline = Task.DEADLINE.DAY;
            customDeadlineDate.setText("Не выбрана");
            task.customEndDate.day = 0;
            task.customEndDate.month = 0;
            task.customEndDate.year = 0;
        }
    };

    DatePickerDialog.OnDateSetListener onDeadlineDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            customDeadlineDate.setText(String.format("%02d", dayOfMonth) + "." +
                    String.format("%02d", monthOfYear + 1) + "." +
                    String.format("%04d", year));
            task.customEndDate.day = dayOfMonth;
            task.customEndDate.month = monthOfYear + 1;
            task.customEndDate.year = year;
        }
    };

    public void selectRadioOption(ArrayList<ImageView> radioList, int selectedIndex) {
        for (int num = 0; num < radioList.size(); num++) {
            if (num == selectedIndex)
                radioList.get(num).setImageResource(R.drawable.radio_checked_medium);
            else
                radioList.get(num).setImageResource(R.drawable.radio_unchecked_medium);
        }
    }

}