package nosfie.easyorg.TaskList;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nosfie.easyorg.Constants;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.MainActivity;
import nosfie.easyorg.R;
import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class ShoppingList extends AppCompatActivity {

    LinearLayout shoppingListLayout;
    ArrayList<String> shoppingList = new ArrayList<>();
    ArrayList<Integer> shoppingListState = new ArrayList<>();
    int taskId;
    String taskName;
    Button buttonBack, buttonOK;
    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    TextView shoppingListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
        shoppingListLayout = (LinearLayout)findViewById(R.id.shoppingList);
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);
        shoppingListName = (TextView)findViewById(R.id.shoppingListName);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            taskId = extras.getInt("id");
            taskName = extras.getString("taskName");
            shoppingListName.setText(taskName);
            shoppingList = extras.getStringArrayList("shoppingList");
            shoppingListState = extras.getIntegerArrayList("shoppingListState");

            int num = 1;
            for (String item: shoppingList) {
                addShoppingItemRow(num, item, shoppingListState.get(num - 1));
                num++;
            }
        }

        buttonOK = (Button)findViewById(R.id.button_OK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shoppingListStateStr = "";
                int currentCount = 0;
                for (int item: shoppingListState) {
                    shoppingListStateStr += Integer.toString(item);
                    currentCount += item;
                }
                DB = tasksConnector.getWritableDatabase();
                String query = "UPDATE tasks " +
                        "SET shoppingListState = '" + shoppingListStateStr + "', " +
                        "currentCount = '" + currentCount + "'";
                if (currentCount == shoppingList.size())
                    query += ", status = 'DONE'";
                else
                    query += ", status = 'ACTUAL'";
                query += " WHERE _id = '" + taskId + "'";
                DB.execSQL(query);
                DB.close();
                finish();
            }
        });

        buttonBack = (Button)findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    protected void addShoppingItemRow(final int num, String item, int state) {
        final LinearLayout itemRow = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.height = convertDpToPixels(this, 50);
        params.setMargins(0, 1, 0, 0);
        itemRow.setOrientation(LinearLayout.HORIZONTAL);
        itemRow.setGravity(Gravity.CENTER_VERTICAL);
        itemRow.setTag("row" + num);
        if (state == 1)
            itemRow.setBackgroundColor(getResources().getColor(R.color.colorTaskDone));
        else
            itemRow.setBackgroundColor(0xFFFFFFFF);
        itemRow.setLayoutParams(params);

        ImageView tick = new ImageView(this);
        if (state == 0)
            tick.setImageResource(R.drawable.check_empty_icon_128);
        else
            tick.setImageResource(R.drawable.check_icon_128);
        LinearLayout.LayoutParams tickParams = new LinearLayout.LayoutParams(0, 0);
        tickParams.setMargins(convertDpToPixels(this, 20), 0, 0, 0);
        tickParams.width = convertDpToPixels(this, 28);
        tickParams.height = tickParams.width;
        tick.setLayoutParams(tickParams);

        TextView itemText = new TextView(this);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(convertDpToPixels(this, 15), 0, convertDpToPixels(this, 20), 0);
        itemText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        itemText.setText(item);
        itemText.setLayoutParams(itemParams);

        itemRow.addView(tick);
        itemRow.addView(itemText);

        itemRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleItemRow(num);
            }
        });

        shoppingListLayout.addView(itemRow);
    }

    protected void toggleItemRow(int num) {
        LinearLayout row = (LinearLayout)shoppingListLayout.findViewWithTag("row" + num);
        int state = shoppingListState.get(num - 1);
        ImageView imageView = (ImageView)row.getChildAt(0);

        if (state == 0) {
            shoppingListState.set(num - 1, 1);
            row.setBackgroundColor(getResources().getColor(R.color.colorTaskDone));
            imageView.setImageResource(R.drawable.check_icon_128);
        }
        else {
            shoppingListState.set(num - 1, 0);
            row.setBackgroundColor(getResources().getColor(R.color.colorTaskActual));
            imageView.setImageResource(R.drawable.check_empty_icon_128);
        }
    }
}