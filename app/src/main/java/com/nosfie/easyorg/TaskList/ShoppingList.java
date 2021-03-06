package com.nosfie.easyorg.TaskList;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import com.nosfie.easyorg.Constants;
import com.nosfie.easyorg.DataStructures.CustomDate;
import com.nosfie.easyorg.DataStructures.DayValues;
import com.nosfie.easyorg.DataStructures.Task;
import com.nosfie.easyorg.DataStructures.Timespan;
import com.nosfie.easyorg.Database.TasksConnector;
import com.nosfie.easyorg.R;
import static com.nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class ShoppingList extends AppCompatActivity {

    LinearLayout shoppingListLayout;
    ArrayList<String> shoppingList = new ArrayList<>();
    ArrayList<Integer> shoppingListState = new ArrayList<>();
    int taskId;
    String taskName;
    LinearLayout buttonBack, buttonSave;
    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    TextView shoppingListName, progressBarText, buttonSaveText;
    ProgressBar progressBar;
    Task.STATUS taskStatus;
    Timespan globalTimespan = Timespan.TODAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        // Setting up DB
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);

        // Setting up view elements
        shoppingListLayout = (LinearLayout)findViewById(R.id.shoppingList);
        shoppingListName = (TextView)findViewById(R.id.shoppingListName);
        progressBarText = (TextView)findViewById(R.id.progressBarText);
        progressBar = (ProgressBar)findViewById(R.id.mprogressBar);
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);
        buttonSave = (LinearLayout)findViewById(R.id.buttonSave);
        buttonSaveText = (TextView)findViewById(R.id.buttonSaveText);

        // Getting shopping list items from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            taskId = extras.getInt("id");
            taskName = extras.getString("taskName");
            taskStatus = Task.STATUS.valueOf(extras.getString("taskStatus"));
            globalTimespan = Timespan.valueOf(extras.getString("timespan"));
            assert taskName != null;
            if (taskName.length() > 25)
                taskName = taskName.substring(0, 25) + "...";
            shoppingListName.setText(taskName);
            shoppingList = extras.getStringArrayList("shoppingList");
            shoppingListState = extras.getIntegerArrayList("shoppingListState");
            int num = 1;
            for (String item: shoppingList) {
                assert shoppingListState != null;
                addShoppingItemRow(num, item, shoppingListState.get(num - 1));
                num++;
            }
        }

        // Setting "Back" button onClickListener
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Setting "Save" button onTouchListener
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonSave.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNextClicked, null));
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonSave.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNext, null));
                        CustomDate intervalFinishedTime = new CustomDate();
                        boolean initialPositive = false;
                        if (taskStatus == Task.STATUS.DONE ||
                            taskStatus == Task.STATUS.IN_PROCESS ||
                            taskStatus == Task.STATUS.NOT_DONE)
                                initialPositive = true;
                        String shoppingListStateStr = "";
                        int currentCount = 0;
                        for (int item: shoppingListState) {
                            shoppingListStateStr += Integer.toString(item);
                            currentCount += item;
                        }
                        boolean afterChangePositive = false;
                        if (currentCount == shoppingList.size())
                            afterChangePositive = true;

                        String intervalFinishedQuery = "";
                        if (initialPositive && afterChangePositive) {
                            // do nothing
                        }
                        else if (!afterChangePositive) {
                            intervalFinishedQuery = ", intervalFinishedTime = '0000.00.00'";
                        }
                        else if (!initialPositive && afterChangePositive) {
                            DayValues dayValues = new DayValues(ShoppingList.this);
                            switch (globalTimespan) {
                                case TODAY:
                                    intervalFinishedTime = dayValues.today;
                                    break;
                                case WEEK:
                                    intervalFinishedTime = dayValues.endOfWeek;
                                    break;
                                case MONTH:
                                    intervalFinishedTime = dayValues.endOfMonth;
                                    break;
                                case YEAR:
                                    intervalFinishedTime = dayValues.endOfYear;
                                    break;
                                default:
                                    intervalFinishedTime = dayValues.today;
                                    break;
                            }
                            intervalFinishedQuery = ", intervalFinishedTime = '"
                                    + intervalFinishedTime.toString() + "|" + globalTimespan.toString()
                                    + "'";
                        }

                        DB = tasksConnector.getWritableDatabase();
                        String query = "UPDATE tasks " +
                                "SET shoppingListState = '" + shoppingListStateStr + "', " +
                                "currentCount = '" + currentCount + "'";
                        if (currentCount == shoppingList.size())
                            query += ", status = 'DONE'";
                        else
                            query += ", status = 'ACTUAL'";
                        query += intervalFinishedQuery;
                        query += " WHERE _id = '" + taskId + "'";
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

        // Drawing appropriate state of the progress bar at the very start
        redrawProgressBar();
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
        tickParams.width = convertDpToPixels(this, 24);
        tickParams.height = convertDpToPixels(this, 24);
        tick.setLayoutParams(tickParams);

        TextView itemText = new TextView(this);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(convertDpToPixels(this, 15), 0, convertDpToPixels(this, 20), 0);
        itemText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
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

        redrawProgressBar();
    }

    protected void redrawProgressBar() {
        int itemsCount = shoppingListState.size();
        int itemsBought = 0;
        for (int item: shoppingListState)
            itemsBought += item;
        progressBarText.setText(Integer.toString(itemsBought) + "/" + Integer.toString(itemsCount));
        progressBar.setProgress((int)((double)itemsBought / (double)itemsCount * 100));
    }
}