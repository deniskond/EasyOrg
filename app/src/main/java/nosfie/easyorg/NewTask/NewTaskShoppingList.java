package nosfie.easyorg.NewTask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;

public class NewTaskShoppingList extends AppCompatActivity {

    TableLayout tableLayout;
    Task task = new Task();
    int insertRowIndex = 9;
    Boolean forToday = false;
    Button button_back, button_next, button_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_shopping_list);

        tableLayout = (TableLayout)findViewById(R.id.table_1);
        button_add = (Button)findViewById(R.id.buttonAdd);
        button_next = (Button)findViewById(R.id.buttonNext);
        button_back = (Button)findViewById(R.id.buttonBack);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = new Task(extras);
            int num = 1;
            for (String item: task.shoppingList) {
                if (item != null && !item.isEmpty()) {
                    TableRow tableRow = (TableRow) tableLayout.findViewWithTag("row" + Integer.toString(num));
                    LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
                    EditText editText = (EditText) linearLayout.getChildAt(1);
                    editText.setText(item);
                    if (num >= insertRowIndex) {
                        insertRowIndex++;
                        tableRow.setVisibility(View.VISIBLE);
                    }
                    num++;
                }
            }
            forToday = extras.getBoolean("forToday");
            if (forToday)
                button_next.setText("Готово");
        }

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableLayout.findViewWithTag("row" + Integer.toString(insertRowIndex)).
                        setVisibility(View.VISIBLE);
                insertRowIndex++;
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskShoppingList.this, NewTaskFirstScreen.class);
                task.shoppingList.clear();
                intent = task.formIntent(intent, task);
                startActivity(intent);
            }
        });

        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.shoppingList.clear();
                for (int i = 1; i <= 20; i++) {
                    TableRow tableRow = (TableRow) tableLayout.findViewWithTag("row" + Integer.toString(i));
                    LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
                    EditText editText = (EditText) linearLayout.getChildAt(1);
                    String item = editText.getText().toString();
                    if (item != null && !item.isEmpty())
                        task.shoppingList.add(item);
                }
                if (task.shoppingList.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Введите список покупок", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent;
                if (forToday) {
                    intent = new Intent(NewTaskShoppingList.this, MainActivity.class);
                    task.insertIntoDatabase(getApplicationContext());
                    intent.putExtra("toast", "Задача успешно добавлена!");
                } else {
                    intent = new Intent(NewTaskShoppingList.this, NewTaskSecondScreen.class);
                    intent = task.formIntent(intent, task);
                }
                startActivity(intent);
            }
        });

    }

}