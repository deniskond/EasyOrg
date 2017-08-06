package nosfie.easyorg.NewTask;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;

public class NewTaskFirstScreen extends AppCompatActivity {

    EditText taskEdit, countEdit;
    TextView countText;
    Task task = new Task();
    RadioGroup radio_group;
    CheckBox forTodayCheckbox;
    Button button_cancel, button_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_first_screen);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        /*
        taskEdit = (EditText)findViewById(R.id.taskName);
        countEdit = (EditText)findViewById(R.id.count);
        countText = (TextView)findViewById(R.id.countText);
        forTodayCheckbox = (CheckBox)findViewById(R.id.forTodayCheckBox);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = new Task(extras);
            taskEdit.setText(task.name);
            countEdit.setText(Integer.toString(task.count));
            switch (task.type) {
                case SIMPLE:
                    RadioButton optionSimple = (RadioButton)findViewById(R.id.optionSimple);
                    optionSimple.setChecked(true);
                    break;
                case SHOPPING_LIST:
                    RadioButton optionShoppingList = (RadioButton)findViewById(R.id.optionShoppingList);
                    optionShoppingList.setChecked(true);
                    break;
                case COUNTABLE:
                    RadioButton optionCountable = (RadioButton)findViewById(R.id.optionCountable);
                    optionCountable.setChecked(true);
                    countText.setVisibility(View.VISIBLE);
                    countEdit.setVisibility(View.VISIBLE);
                    break;
            }
        }

        radio_group = (RadioGroup)findViewById(R.id.radioGroup);
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                String taskText = taskEdit.getText().toString();
                switch (getTaskType()) {
                    case SIMPLE:
                        task.type = Task.TYPE.SIMPLE;
                        if (taskText.equals("Список покупок"))
                            taskEdit.setText("");
                        countText.setVisibility(View.GONE);
                        countEdit.setVisibility(View.GONE);
                        if (forTodayCheckbox.isChecked())
                            button_next.setText("Готово");
                        break;
                    case SHOPPING_LIST:
                        task.type = Task.TYPE.SHOPPING_LIST;
                        if (taskText.equals(""))
                            taskEdit.setText("Список покупок");
                        countText.setVisibility(View.GONE);
                        countEdit.setVisibility(View.GONE);
                        button_next.setText("Дальше");
                        break;
                    case COUNTABLE:
                        task.type = Task.TYPE.COUNTABLE;
                        if (taskText.equals("Список покупок"))
                            taskEdit.setText("");
                        countText.setVisibility(View.VISIBLE);
                        countEdit.setVisibility(View.VISIBLE);
                        countEdit.setText("");
                        if (forTodayCheckbox.isChecked())
                            button_next.setText("Готово");
                        break;
                    default:
                        break;
                }
            }
        });


        forTodayCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task.type != Task.TYPE.SHOPPING_LIST) {
                    if (forTodayCheckbox.isChecked()) {
                        button_next.setText("Готово");
                    } else {
                        button_next.setText("Дальше");
                    }
                }
            }
        });

        button_cancel = (Button)findViewById(R.id.buttonCancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskFirstScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

        button_next = (Button)findViewById(R.id.buttonNext);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.name = taskEdit.getText().toString();
                String countEditString = countEdit.getText().toString();
                if (!countEditString.equals(""))
                    task.count = Integer.parseInt(countEdit.getText().toString());
                else
                    task.count = 0;
                if (task.name.equals("")) {
                    Toast.makeText(getApplicationContext(), "Введите название задачи",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (task.type == Task.TYPE.COUNTABLE && task.count == 0) {
                    Toast.makeText(getApplicationContext(), "Введите количественную цель",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent;

                if (forTodayCheckbox.isChecked()) {
                    task.startTime = Task.START_TIME.NONE;
                    task.startDate = Task.START_DATE.TODAY;
                    task.deadline = Task.DEADLINE.DAY;
                    if (task.type != Task.TYPE.SHOPPING_LIST) {
                        intent = new Intent(NewTaskFirstScreen.this, MainActivity.class);
                        task.insertIntoDatabase(getApplicationContext());
                        intent.putExtra("toast", "Задача успешно добавлена!");
                    } else {
                        intent = new Intent(NewTaskFirstScreen.this, NewTaskShoppingList.class);
                        intent.putExtra("forToday", true);
                    }
                }
                else if (task.type == Task.TYPE.SHOPPING_LIST) {
                    intent = new Intent(NewTaskFirstScreen.this, NewTaskShoppingList.class);
                    intent.putExtra("forToday", false);
                }
                else
                    intent = new Intent(NewTaskFirstScreen.this, NewTaskSecondScreen.class);

                intent = task.formIntent(intent, task);
                startActivity(intent);
            }
        });*/

    }

    /*
    protected Task.TYPE getTaskType() {
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(radioButtonID);
        int idx = radioGroup.indexOfChild(radioButton);
        switch (idx) {
            case 0:
                return Task.TYPE.SIMPLE;
            case 1:
                return Task.TYPE.SHOPPING_LIST;
            case 2:
                return Task.TYPE.COUNTABLE;
            default:
                return Task.TYPE.SIMPLE;
        }
    }*/

}
