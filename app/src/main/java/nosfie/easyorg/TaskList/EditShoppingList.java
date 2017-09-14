package nosfie.easyorg.TaskList;

import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nosfie.easyorg.Constants;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.R;
import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;
import static nosfie.easyorg.NewTask.ShoppingListView.getShoppingItemRow;

public class EditShoppingList extends AppCompatActivity {

    ArrayList<String> shoppingList = new ArrayList<>();
    int insertRowId = 1;
    int taskId = 0;
    String returnActivityName, timespan;
    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    int DP = 0;
    TableLayout shoppingListItems;
    ImageView buttonAdd;
    LinearLayout buttonBack, buttonSave;
    TextView buttonSaveText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shopping_list);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        // Setting up DB
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);

        // Setting DP value for current screen
        DP = convertDpToPixels(this, 1);

        // Setting up view elements
        shoppingListItems = (TableLayout)findViewById(R.id.shoppingListItems);
        buttonAdd = (ImageView)findViewById(R.id.buttonAdd);
        buttonBack = (LinearLayout) findViewById(R.id.buttonBack);
        buttonSave = (LinearLayout)findViewById(R.id.buttonSave);
        buttonSaveText = (TextView)findViewById(R.id.buttonSaveText);

        // Getting shopping list items from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            shoppingList = extras.getStringArrayList("shoppingList");
            taskId = extras.getInt("id");
            returnActivityName = extras.getString("returnActivity");
            timespan = extras.getString("timespan");
            drawShoppingListForEditing();
        }

        // "Add item" button
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addShoppingItemRow(insertRowId++, "");
            }
        });

        // "Back" button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // "Save" button
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonSave.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNextClicked, null));
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonSave.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNext, null));
                        ArrayList<String> newShoppingList = new ArrayList<>();
                        for (int id = 0; id < insertRowId; id++) {
                            LinearLayout row = (LinearLayout)findViewById(id);
                            if (row != null) {
                                LinearLayout linear = (LinearLayout)(row.getChildAt(0));
                                LinearLayout linear2 = (LinearLayout)(linear.getChildAt(1));
                                EditText editText = (EditText)(linear2.getChildAt(0));
                                String item = editText.getText().toString();
                                if (!item.equals(""))
                                    newShoppingList.add(item);
                            }
                        }
                        String strShoppingList = "";
                        String strShoppingListState = "";
                        for (int i = 0; i < newShoppingList.size(); i++) {
                            strShoppingListState += "0";
                            String item = newShoppingList.get(i).replaceAll("|", "");
                            if (i != newShoppingList.size() - 1)
                                strShoppingList += item + "|";
                            else
                                strShoppingList += item;
                        }
                        DB = tasksConnector.getWritableDatabase();
                        String query = "UPDATE tasks " +
                                "SET shoppingList = '" + strShoppingList + "', " +
                                "shoppingListState = '" + strShoppingListState + "', " +
                                "currentCount = '0', status = 'ACTUAL', " +
                                "count = '" + newShoppingList.size() + "' " +
                                "WHERE _id = '" + taskId + "'";
                        DB.execSQL(query);
                        DB.close();
                        finish();
                        break;
                }
                return true;
            }
        };
        buttonSave.setOnTouchListener(onTouchListener);
        buttonSaveText.setOnTouchListener(onTouchListener);
    }

    protected void drawShoppingListForEditing() {
        shoppingListItems.removeAllViews();
        insertRowId = 1;
        for (String item: shoppingList)
            addShoppingItemRow(insertRowId++, item);
    }

    private void addShoppingItemRow(int num, String name) {
        // Setting delete icon OnClickListener which will be used in each shopping list row
        View.OnClickListener deleteIconListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (insertRowId == 2) {
                    Toast.makeText(EditShoppingList.this,
                            getResources().getString(R.string.error_need_shopping_element),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                LinearLayout row = (LinearLayout)(view.getParent()).getParent();
                int id = row.getId();
                ((ViewManager)row.getParent()).removeView(row);
                for (int i = id + 1; i < insertRowId; i++) {
                    LinearLayout nextRow = (LinearLayout)findViewById(i);
                    nextRow.setId(i - 1);
                    TextView number = (TextView)(((LinearLayout)nextRow.getChildAt(0)).getChildAt(0));
                    number.setText(Integer.toString(i - 1));
                }
                insertRowId--;
            }
        };

        TableRow itemRow = getShoppingItemRow(this, num, name);
        LinearLayout row = (LinearLayout)itemRow.getChildAt(0);
        ImageView deleteIcon = (ImageView)row.getChildAt(2);
        deleteIcon.setOnClickListener(deleteIconListener);
        shoppingListItems.addView(itemRow);
    }

}