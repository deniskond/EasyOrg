package nosfie.easyorg.NewTask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.Helpers.ViewHelper;
import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;

import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class NewTaskShoppingList extends AppCompatActivity {

    TableLayout tableInner;
    Task task = new Task();
    int insertRowIndex = 9;
    Boolean forToday = false;
    Button button_back, button_next, button_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_shopping_list);

        tableInner = (TableLayout)findViewById(R.id.table_inner);
        button_add = (Button)findViewById(R.id.buttonAdd);
        button_next = (Button)findViewById(R.id.buttonNext);
        button_back = (Button)findViewById(R.id.buttonBack);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = new Task(extras);
            int num = 1;
            for (String item: task.shoppingList) {
                if (item != null && !item.isEmpty()) {
                    addShoppingItemRow(num, item);
                    num++;
                }
            }
            if (num < insertRowIndex)
                while (num != insertRowIndex) {
                    addShoppingItemRow(num, "");
                    num++;
                }
            else
                insertRowIndex = num;
            forToday = extras.getBoolean("forToday");
            if (forToday)
                button_next.setText("Готово");
        }
        else
            for (int num = 1; num < insertRowIndex; num++)
                addShoppingItemRow(num, "");

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addShoppingItemRow(insertRowIndex, "");
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
                for (int i = 1; i < insertRowIndex; i++) {
                    TableRow tableRow = (TableRow) tableInner.findViewWithTag("row" + Integer.toString(i));
                    LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
                    EditText editText = (EditText) linearLayout.getChildAt(1);
                    String item = editText.getText().toString();
                    if (item != null && !item.isEmpty()) {
                        task.shoppingList.add(item);
                        task.shoppingListState.add(0);
                    }
                }
                if (task.shoppingList.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Введите список покупок", Toast.LENGTH_SHORT).show();
                    return;
                }
                task.count = task.shoppingList.size();
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

    protected void addShoppingItemRow(int num, String value) {
        TableRow row = new TableRow(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.setMargins(0, convertDpToPixels(this, 10), 0, 0);
        row.setBackgroundColor(0x00000000);
        row.setLayoutParams(params);
        row.setTag("row" + num);

        LinearLayout linearRow = new LinearLayout(this);
        TableRow.LayoutParams linearParams = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearParams.weight = 1;
        linearRow.setLayoutParams(linearParams);

        TextView number = new TextView(this);
        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        numberParams.width = convertDpToPixels(this, 30);
        numberParams.setMargins(ViewHelper.convertDpToPixels(this, 20), 0, 0, 0);
        number.setPadding(0, 0, ViewHelper.convertDpToPixels(this, 10), 0);
        number.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        number.setLayoutParams(numberParams);
        number.setText(Integer.toString(num));

        EditText item = new EditText(this);
        LinearLayout.LayoutParams itemParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(ViewHelper.convertDpToPixels(this, 5), 0,
                ViewHelper.convertDpToPixels(this, 20), 0);
        item.setPadding(
            ViewHelper.convertDpToPixels(this, 8),
            ViewHelper.convertDpToPixels(this, 5),
            ViewHelper.convertDpToPixels(this, 5),
            ViewHelper.convertDpToPixels(this, 5)
        );
        item.setSingleLine(true);
        item.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        item.setBackgroundResource(R.drawable.border_small);
        item.setGravity(Gravity.BOTTOM);
        item.setTextColor(0xFF000000);
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        item.setLayoutParams(itemParams);
        item.setText(value);

        linearRow.addView(number);
        linearRow.addView(item);

        row.addView(linearRow);
        tableInner.addView(row);
    }

}