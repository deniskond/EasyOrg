package nosfie.easyorg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class NewTaskFirstScreen extends AppCompatActivity {

    EditText taskEdit, countEdit;
    TextView countText;
    enum TASK_TYPE {
        SIMPLE, SHOPPING_LIST, COUNTABLE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_first_screen);
        taskEdit = (EditText)findViewById(R.id.taskName);
        countEdit = (EditText)findViewById(R.id.count);
        countText = (TextView)findViewById(R.id.countText);

        RadioGroup radio_group = (RadioGroup)findViewById(R.id.radioGroup);
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                String taskText = taskEdit.getText().toString();
                switch (getTaskType()) {
                    case SIMPLE:
                        if (taskText.equals("Список покупок"))
                            taskEdit.setText("");
                        countText.setVisibility(View.INVISIBLE);
                        countEdit.setVisibility(View.INVISIBLE);
                        break;
                    case SHOPPING_LIST:
                        if (taskText.equals(""))
                            taskEdit.setText("Список покупок");
                        countText.setVisibility(View.INVISIBLE);
                        countEdit.setVisibility(View.INVISIBLE);
                        break;
                    case COUNTABLE:
                        if (taskText.equals("Список покупок"))
                            taskEdit.setText("");
                        countText.setVisibility(View.VISIBLE);
                        countEdit.setVisibility(View.VISIBLE);
                        countEdit.setText("");
                        break;
                    default:
                        break;
                }
            }
        });

        Button button_cancel = (Button)findViewById(R.id.buttonCancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskFirstScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button button_next = (Button)findViewById(R.id.buttonNext);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskText = taskEdit.getText().toString();
                String countEditValue = countEdit.getText().toString();
                if (taskText.equals("")) {
                    Toast.makeText(getApplicationContext(), "Введите название задачи",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getTaskType() == TASK_TYPE.COUNTABLE && countEditValue.equals("")) {
                    Toast.makeText(getApplicationContext(), "Введите количественную цель",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(NewTaskFirstScreen.this, NewTaskSecondScreen.class);
                startActivity(intent);
            }
        });

    }

    protected TASK_TYPE getTaskType() {
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        int radioButtonID = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(radioButtonID);
        int idx = radioGroup.indexOfChild(radioButton);
        switch (idx) {
            case 0:
                return TASK_TYPE.SIMPLE;
            case 1:
                return TASK_TYPE.SHOPPING_LIST;
            case 2:
                return TASK_TYPE.COUNTABLE;
            default:
                return TASK_TYPE.SIMPLE;
        }
    }

}
