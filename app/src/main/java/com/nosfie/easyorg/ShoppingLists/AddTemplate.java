package com.nosfie.easyorg.ShoppingLists;

import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.nosfie.easyorg.DataStructures.Task;
import com.nosfie.easyorg.R;

import static com.nosfie.easyorg.NewTask.ShoppingListView.getShoppingItemRow;

public class AddTemplate extends AppCompatActivity {

    LinearLayout buttonBack, buttonSave;
    TextView buttonSaveText;
    ImageView buttonAdd;
    TableLayout shoppingListContainer;
    int insertRowIndex = 4;
    ScrollView scrollView;
    Task task = new Task();
    EditText templateName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_lists_template);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        // Setting up view elements
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);
        buttonAdd = (ImageView)findViewById(R.id.buttonAdd);
        shoppingListContainer = (TableLayout)findViewById(R.id.shoppingListContainer);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        buttonSave = (LinearLayout)findViewById(R.id.buttonSave);
        buttonSaveText = (TextView)findViewById(R.id.buttonSaveText);
        templateName = (EditText)findViewById(R.id.templateName);

        // Setting navigation buttons onClick listeners
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // "Add shopping list item" button event listener (onTouch)
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

        // Processing "Finish" button click; adding task to database and closing "Add task" section
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonSave.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNextClicked, null));
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonSave.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNext, null));
                        task.name = templateName.getText().toString();
                        if (task.name.equals("")) {
                            Toast.makeText(AddTemplate.this, getResources().getString(R.string.error_no_template_name),Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        task.type = Task.TYPE.TEMPLATE;
                        task.shoppingList.clear();
                        for (int id = 0; id < insertRowIndex; id++) {
                            LinearLayout row = (LinearLayout)findViewById(id);
                            if (row != null) {
                                LinearLayout linear = (LinearLayout)(row.getChildAt(0));
                                LinearLayout linear2 = (LinearLayout)(linear.getChildAt(1));
                                EditText editText = (EditText)(linear2.getChildAt(0));
                                String item = editText.getText().toString();
                                if (!item.equals("") && !item.isEmpty()) {
                                    task.shoppingList.add(item);
                                    task.shoppingListState.add(0);
                                }
                            }
                        }
                        if (task.shoppingList.size() == 0) {
                            Toast.makeText(AddTemplate.this, getResources().getString(R.string.error_input_shopping_list), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        task.count = task.shoppingList.size();
                        task.insertIntoDatabase(getApplicationContext());
                        Toast.makeText(AddTemplate.this, getResources().getString(R.string.success_added_template), Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
                return true;
            }
        };
        buttonSave.setOnTouchListener(onTouchListener);
        buttonSaveText.setOnTouchListener(onTouchListener);

        // Adding default number of shopping list rows
        for (int num = 1; num < insertRowIndex; num++)
            addShoppingItemRow(num, "");
    }

    private void addShoppingItemRow(int num, String name) {
        // Setting delete icon OnClickListener which will be used in each shopping list row
        View.OnClickListener deleteIconListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (insertRowIndex == 2) {
                    Toast.makeText(AddTemplate.this,
                            getResources().getString(R.string.error_need_shopping_element),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                LinearLayout row = (LinearLayout)(view.getParent()).getParent();
                int id = row.getId();
                ((ViewManager)row.getParent()).removeView(row);
                for (int i = id + 1; i < insertRowIndex; i++) {
                    LinearLayout nextRow = (LinearLayout)findViewById(i);
                    nextRow.setId(i - 1);
                    TextView number = (TextView)(((LinearLayout)nextRow.getChildAt(0)).getChildAt(0));
                    number.setText(Integer.toString(i - 1));
                }
                insertRowIndex--;
            }
        };

        TableRow itemRow = getShoppingItemRow(this, num, name);
        LinearLayout row = (LinearLayout)itemRow.getChildAt(0);
        ImageView deleteIcon = (ImageView)row.getChildAt(2);
        deleteIcon.setOnClickListener(deleteIconListener);
        shoppingListContainer.addView(itemRow);
    }

}