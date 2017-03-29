package nosfie.easyorg;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class NewTask extends AppCompatActivity {

    EditText taskEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_first_screen);
        taskEdit = (EditText)findViewById(R.id.taskName);

        RadioGroup radio_group = (RadioGroup)findViewById(R.id.radioGroup);
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checked) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                String taskText = taskEdit.getText().toString();

                switch (idx) {
                    case 0:
                        if (taskText.equals("Список покупок"))
                            taskEdit.setText("");
                        findViewById(R.id.countText).setVisibility(View.INVISIBLE);
                        findViewById(R.id.count).setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        if (taskText.equals(""))
                            taskEdit.setText("Список покупок");
                        findViewById(R.id.countText).setVisibility(View.INVISIBLE);
                        findViewById(R.id.count).setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        if (taskText.equals("Список покупок"))
                            taskEdit.setText("");
                        findViewById(R.id.countText).setVisibility(View.VISIBLE);
                        findViewById(R.id.count).setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        });

    }

}
