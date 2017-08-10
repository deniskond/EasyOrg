package nosfie.easyorg.NewTask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    Boolean lastScreen = false;
    ImageView buttonAdd, buttonTemplateFill;
    LinearLayout buttonBack, buttonNext, buttonClose;
    TextView buttonNextText;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_shopping_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Setting up view elements
        tableInner = (TableLayout)findViewById(R.id.table_inner);
        buttonAdd = (ImageView)findViewById(R.id.buttonAdd);
        buttonTemplateFill = (ImageView)findViewById(R.id.buttonTemplateFill);
        buttonNext = (LinearLayout) findViewById(R.id.buttonNext);
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);
        buttonClose = (LinearLayout)findViewById(R.id.buttonClose);
        buttonNextText = (TextView)findViewById(R.id.buttonNextText);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        // Setting up the info which is received from other "Add task" steps
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
            lastScreen = extras.getBoolean("lastScreen");
            if (lastScreen)
                buttonNextText.setText("ГОТОВО");
        }
        else
            for (int num = 1; num < insertRowIndex; num++)
                addShoppingItemRow(num, "");

        // Add shopping list button event listener (onTouch)
        buttonAdd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonAdd.setImageResource(R.drawable.add_item_button_medium_dark);
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonAdd.setImageResource(R.drawable.add_item_button_medium);
                        addShoppingItemRow(insertRowIndex, "");
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        insertRowIndex++;
                        break;
                }
                return true;
            }
        });

        // Fill from template button event listener (onTouch)
        buttonTemplateFill.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonTemplateFill.setImageResource(R.drawable.template_fill_button_medium_dark);
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonTemplateFill.setImageResource(R.drawable.template_fill_button_medium);
                        // TODO
                        break;
                }
                return true;
            }
        });

        // Setting navigation buttons onClick listeners
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskShoppingList.this, NewTaskFirstScreen.class);
                task.shoppingList.clear();
                intent = task.formIntent(intent, task);
                startActivity(intent);
            }
        });
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Processing "Finish" button click; adding task to database and closing "Add task" section
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonNext.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNextClicked, null));
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonNext.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNext, null));
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
                            return true;
                        }
                        task.count = task.shoppingList.size();
                        Intent intent;
                        if (lastScreen) {
                            intent = new Intent(NewTaskShoppingList.this, MainActivity.class);
                            task.insertIntoDatabase(getApplicationContext());
                            intent.putExtra("toast", "Задача успешно добавлена!");
                        } else {
                            intent = new Intent(NewTaskShoppingList.this, NewTaskSecondScreen.class);
                            intent = task.formIntent(intent, task);
                        }
                        startActivity(intent);
                        break;
                }
                return true;
            }
        };
        buttonNext.setOnTouchListener(onTouchListener);
        buttonNextText.setOnTouchListener(onTouchListener);
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
        numberParams.setMargins(ViewHelper.convertDpToPixels(this, 15), 0, 0, 0);
        number.setPadding(0, 0, ViewHelper.convertDpToPixels(this, 10), 0);
        number.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        number.setLayoutParams(numberParams);
        number.setText(Integer.toString(num));

        EditText item = new EditText(this);
        LinearLayout.LayoutParams itemParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(0, 0,
                ViewHelper.convertDpToPixels(this, 15), 0);
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