package nosfie.easyorg.TaskList;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import nosfie.easyorg.Constants;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.R;
import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class EditShoppingList extends AppCompatActivity {

    ArrayList<String> shoppingList = new ArrayList<>();
    int insertRowId = 1;
    int taskId = 0;
    String returnActivityName, timespan;
    SQLiteDatabase DB;
    TasksConnector tasksConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_shopping_list);
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            shoppingList = extras.getStringArrayList("shoppingList");
            taskId = extras.getInt("id");
            returnActivityName = extras.getString("returnActivity");
            timespan = extras.getString("timespan");
            for (String item: shoppingList)
                addShoppingListEditRow(item, insertRowId++);
        }

        Button buttonAdd = (Button)findViewById(R.id.buttonAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addShoppingListEditRow("", insertRowId++);
            }
        });

        Button buttonBack = (Button)findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button buttonOk = (Button)findViewById(R.id.button_OK);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> newShoppingList = new ArrayList<>();
                for (int id = 0; id < insertRowId; id++) {
                    LinearLayout row = (LinearLayout)findViewById(id);
                    if (row != null) {
                        EditText editText = (EditText)row.getChildAt(0);
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
            }
        });

    }

    protected void addShoppingListEditRow(String item, final int id) {
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(rowParams);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setId(id);

        EditText itemName = new EditText(this);
        LinearLayout.LayoutParams itemNameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = convertDpToPixels(this, 15);
        itemNameParams.setMargins(margin, margin, margin, margin);
        itemNameParams.weight = 1;
        itemName.setLayoutParams(itemNameParams);
        int padding = convertDpToPixels(this, 5);
        itemName.setPadding(padding, padding, padding, padding);
        itemName.setBackgroundResource(R.drawable.border_small);
        itemName.setText(item);

        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageResource(R.drawable.delete_icon_small);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        iconParams.setMargins(0, 0, convertDpToPixels(this, 15), 0);
        deleteIcon.setLayoutParams(iconParams);
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout row = (LinearLayout)findViewById(id);
                ((ViewManager)row.getParent()).removeView(row);
            }
        });

        row.addView(itemName);
        row.addView(deleteIcon);

        LinearLayout shoppingListItems = (LinearLayout)findViewById(R.id.shopping_list_items);
        shoppingListItems.addView(row);

    }

}