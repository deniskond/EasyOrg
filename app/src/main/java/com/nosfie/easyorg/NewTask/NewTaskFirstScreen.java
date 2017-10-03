package com.nosfie.easyorg.NewTask;

import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nosfie.easyorg.DataStructures.Task;
import com.nosfie.easyorg.R;

public class NewTaskFirstScreen extends AppCompatActivity {

    EditText taskName, countEdit;
    TextView countText, buttonNextText;
    Task task = new Task();
    LinearLayout simpleTaskSelector, countableTaskSelector, shoppingListSelector;
    LinearLayout buttonNext, buttonCancel, buttonBack;
    LinearLayout selectInterval, selectToday, selectTimeless;
    ImageView intervalRadio, todayRadio, timelessRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_first_screen);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        // Setting up view elements
        taskName = (EditText)findViewById(R.id.taskName);
        countEdit = (EditText)findViewById(R.id.count);
        countText = (TextView)findViewById(R.id.countText);
        simpleTaskSelector = (LinearLayout)findViewById(R.id.simpleTaskSelector);
        countableTaskSelector = (LinearLayout)findViewById(R.id.countableTaskSelector);
        shoppingListSelector = (LinearLayout)findViewById(R.id.shoppingListSelector);
        buttonNext = (LinearLayout)findViewById(R.id.buttonNext);
        buttonNextText = (TextView)findViewById(R.id.buttonNextText);
        buttonCancel = (LinearLayout)findViewById(R.id.buttonCancel);
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);
        selectInterval = (LinearLayout)findViewById(R.id.selectInterval);
        selectToday = (LinearLayout)findViewById(R.id.selectToday);
        selectTimeless = (LinearLayout)findViewById(R.id.selectTimeless);
        intervalRadio = (ImageView)findViewById(R.id.intervalRadio);
        todayRadio = (ImageView)findViewById(R.id.todayRadio);
        timelessRadio = (ImageView)findViewById(R.id.timelessRadio);

        // Getting info from intent:
        // 1) The info which is received from other "Add task" steps;
        // 2) Predefined task info when add task is issued through other app sections
        // Both steps are done in Task(Bundle) constructor
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = new Task(extras);
            taskName.setText(task.name);
            countEdit.setText(Integer.toString(task.count));
            switch (task.type) {
                case SIMPLE:
                    simpleTaskSelector.setBackgroundResource(R.drawable.border_big_selected);
                    countableTaskSelector.setBackgroundResource(R.drawable.border_small);
                    shoppingListSelector.setBackgroundResource(R.drawable.border_small);
                    break;
                case SHOPPING_LIST:
                    simpleTaskSelector.setBackgroundResource(R.drawable.border_small);
                    countableTaskSelector.setBackgroundResource(R.drawable.border_small);
                    shoppingListSelector.setBackgroundResource(R.drawable.border_big_selected);
                    buttonNextText.setText(getResources().getString(R.string.next));
                    break;
                case COUNTABLE:
                    simpleTaskSelector.setBackgroundResource(R.drawable.border_small);
                    countableTaskSelector.setBackgroundResource(R.drawable.border_big_selected);
                    shoppingListSelector.setBackgroundResource(R.drawable.border_small);
                    countText.setVisibility(View.VISIBLE);
                    countEdit.setVisibility(View.VISIBLE);
                    break;
            }
            if (task.deadline == Task.DEADLINE.CUSTOM) {
                intervalRadio.setImageResource(R.drawable.radio_checked_medium);
                todayRadio.setImageResource(R.drawable.radio_unchecked_medium);
                timelessRadio.setImageResource(R.drawable.radio_unchecked_medium);
            }
            else if (task.deadline == Task.DEADLINE.NONE) {
                intervalRadio.setImageResource(R.drawable.radio_unchecked_medium);
                todayRadio.setImageResource(R.drawable.radio_unchecked_medium);
                timelessRadio.setImageResource(R.drawable.radio_checked_medium);
            }
            if (task.predefinedShoppingList) {
                if (task.name == null)
                    taskName.setText(getResources().getString(R.string.shopping_list));
                simpleTaskSelector.setVisibility(View.GONE);
                countableTaskSelector.setVisibility(View.GONE);
                LinearLayout padding1 = (LinearLayout)findViewById(R.id.padding1);
                LinearLayout padding2 = (LinearLayout)findViewById(R.id.padding2);
                padding1.setVisibility(View.GONE);
                padding2.setVisibility(View.GONE);
            }
            if (task.usePredefinedTimespan || task.usePredefinedDate) {
                selectToday.setVisibility(View.GONE);
                selectInterval.setVisibility(View.GONE);
                selectTimeless.setVisibility(View.GONE);
                LinearLayout selectPredefined = (LinearLayout)findViewById(R.id.selectPredefined);
                selectPredefined.setVisibility(View.VISIBLE);
                TextView predefinedText = (TextView)findViewById(R.id.predefinedText);
                if (task.usePredefinedTimespan) {
                    switch (task.deadline) {
                        case DAY:
                            predefinedText.setText(getResources().getString(R.string.today));
                            break;
                        case WEEK:
                            predefinedText.setText(getResources().getString(R.string.till_the_end_of_week));
                            break;
                        case MONTH:
                            predefinedText.setText(getResources().getString(R.string.till_the_end_of_month));
                            break;
                        case YEAR:
                            predefinedText.setText(getResources().getString(R.string.till_the_end_of_year));
                            break;
                        case NONE:
                            predefinedText.setText(getResources().getString(R.string.perpetual));
                            break;
                    }
                }
                else {
                    predefinedText.setText(task.customStartDate.toHumanString());
                }
            }
        }

        // Setting up onClickListeners for task type buttons
        simpleTaskSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task.type == Task.TYPE.SIMPLE || task.predefinedShoppingList)
                    return;
                simpleTaskSelector.setBackgroundResource(R.drawable.border_big_selected);
                countableTaskSelector.setBackgroundResource(R.drawable.border_small);
                shoppingListSelector.setBackgroundResource(R.drawable.border_small);
                task.type = Task.TYPE.SIMPLE;
                if (taskName.getText().toString().equals(getResources().getString(R.string.shopping_list)))
                    taskName.setText("");
                countText.setVisibility(View.GONE);
                countEdit.setVisibility(View.GONE);
                if (task.deadline == Task.DEADLINE.DAY || task.deadline == Task.DEADLINE.NONE)
                    buttonNextText.setText(getResources().getString(R.string.done));
            }
        });
        countableTaskSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task.type == Task.TYPE.COUNTABLE || task.predefinedShoppingList)
                    return;
                simpleTaskSelector.setBackgroundResource(R.drawable.border_small);
                countableTaskSelector.setBackgroundResource(R.drawable.border_big_selected);
                shoppingListSelector.setBackgroundResource(R.drawable.border_small);
                task.type = Task.TYPE.COUNTABLE;
                if (taskName.getText().toString().equals(getResources().getString(R.string.shopping_list)))
                    taskName.setText("");
                countText.setVisibility(View.VISIBLE);
                countEdit.setVisibility(View.VISIBLE);
                countEdit.setText("");
                if (task.deadline == Task.DEADLINE.DAY || task.deadline == Task.DEADLINE.NONE)
                    buttonNextText.setText(getResources().getString(R.string.done));
            }
        });
        shoppingListSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task.type == Task.TYPE.SHOPPING_LIST)
                    return;
                simpleTaskSelector.setBackgroundResource(R.drawable.border_small);
                countableTaskSelector.setBackgroundResource(R.drawable.border_small);
                shoppingListSelector.setBackgroundResource(R.drawable.border_big_selected);
                task.type = Task.TYPE.SHOPPING_LIST;
                if (taskName.getText().toString().equals(""))
                    taskName.setText(getResources().getString(R.string.shopping_list));
                countText.setVisibility(View.GONE);
                countEdit.setVisibility(View.GONE);
                buttonNextText.setText(getResources().getString(R.string.next));
            }
        });

        // Processing improvised radio group item clicks
        // 1) Interval
        selectInterval.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        selectInterval.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        selectInterval.setBackgroundColor(0x00000000);
                        buttonNextText.setText(getResources().getString(R.string.next));
                        task.deadline = Task.DEADLINE.CUSTOM;
                        intervalRadio.setImageResource(R.drawable.radio_checked_medium);
                        todayRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        timelessRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        break;
                }
                return true;
            }
        });
        // 2) Today
        selectToday.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        selectToday.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        selectToday.setBackgroundColor(0x00000000);
                        if (task.type != Task.TYPE.SHOPPING_LIST)
                            buttonNextText.setText(getResources().getString(R.string.done));
                        task.deadline = Task.DEADLINE.DAY;
                        intervalRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        todayRadio.setImageResource(R.drawable.radio_checked_medium);
                        timelessRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        break;
                }
                return true;
            }
        });
        // 3) Timeless
        selectTimeless.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        selectTimeless.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        selectTimeless.setBackgroundColor(0x00000000);
                        if (task.type != Task.TYPE.SHOPPING_LIST)
                            buttonNextText.setText(getResources().getString(R.string.done));
                        task.deadline = Task.DEADLINE.NONE;
                        intervalRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        todayRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        timelessRadio.setImageResource(R.drawable.radio_checked_medium);
                        break;
                }
                return true;
            }
        });

        // Setting up navigation buttons
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();
            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setting data for next "Add task" step; it can be:
        // 1) Shopping list page
        // 2) Time interval page
        // 3) Finish of "Add task"; adding task to database
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonNext.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNextClicked, null));
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonNext.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNext, null));
                        task.name = taskName.getText().toString();
                        if (!countEdit.getText().toString().equals(""))
                            task.count = Integer.parseInt(countEdit.getText().toString());
                        else
                            task.count = 0;
                        if (task.name.equals("")) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_input_task_name),Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        if (task.type == Task.TYPE.COUNTABLE && task.count == 0) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_input_countable_goal),Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        Intent intent;
                        if (task.deadline == Task.DEADLINE.DAY || task.deadline == Task.DEADLINE.NONE
                                || task.usePredefinedTimespan || task.usePredefinedDate) {
                            task.startTime = Task.START_TIME.NONE;
                            if (!task.usePredefinedDate)
                                task.startDate = Task.START_DATE.TODAY;
                            if (task.type != Task.TYPE.SHOPPING_LIST) {
                                task.insertIntoDatabase(getApplicationContext());
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.success_added_task),Toast.LENGTH_SHORT).show();
                                finish();
                                return true;
                            } else {
                                intent = new Intent(NewTaskFirstScreen.this, NewTaskShoppingList.class);
                                intent.putExtra("lastScreen", true);
                            }
                        }
                        else if (task.type == Task.TYPE.SHOPPING_LIST) {
                            intent = new Intent(NewTaskFirstScreen.this, NewTaskShoppingList.class);
                            intent.putExtra("lastScreen", false);
                        }
                        else
                            intent = new Intent(NewTaskFirstScreen.this, NewTaskSecondScreen.class);

                        intent = task.formIntent(intent, task);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        };
        buttonNext.setOnTouchListener(onTouchListener);
        buttonNextText.setOnTouchListener(onTouchListener);
    }

}