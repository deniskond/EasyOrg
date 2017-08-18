package nosfie.easyorg.ShoppingLists;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;
import nosfie.easyorg.Database.TasksConnector;
import nosfie.easyorg.R;
import nosfie.easyorg.TaskList.TaskView;

import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class ShoppingLists extends AppCompatActivity {

    LinearLayout buttonBack, buttonCancel;
    LinearLayout shoppingListsList, templatesList;
    SQLiteDatabase DB;
    TasksConnector tasksConnector;
    ArrayList<Task> templates = new ArrayList<>(),
            shoppingLists = new ArrayList<>();
    TextView result;
    int DP = 0;
    ImageView addTemplateButton, addShoppingListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_lists);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Database setup
        tasksConnector = new TasksConnector(getApplicationContext(), Constants.DB_NAME, null, 1);

        // DP setup
        DP = convertDpToPixels(this, 1);

        // Showing toast text (if exists)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String toast = extras.getString("toast");
            Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
        }

        // Setting up view elements
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);
        buttonCancel = (LinearLayout)findViewById(R.id.buttonCancel);
        templatesList = (LinearLayout)findViewById(R.id.templatesList);
        shoppingListsList = (LinearLayout)findViewById(R.id.shoppingListsList);
        result = (TextView)findViewById(R.id.result);
        addTemplateButton = (ImageView)findViewById(R.id.addTemplateButton);
        addShoppingListButton = (ImageView)findViewById(R.id.addShoppingListButton);

        // Setting navigation buttons onClickListeners
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Add template onClickListener
        addTemplateButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        addTemplateButton.setImageResource(R.drawable.plus_dark);
                        break;
                    case MotionEvent.ACTION_UP:
                        addTemplateButton.setImageResource(R.drawable.plus);
                        Intent intent = new Intent(ShoppingLists.this, AddTemplate.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        // Drawing shopping lists and templates
        drawTemplatesAndShoppingLists();
    }

    private void drawTemplatesAndShoppingLists() {
        templates.clear();
        shoppingLists.clear();

        DB = tasksConnector.getReadableDatabase();
        String columns[] = {"_id", "name", "type", "startDate", "startTime", "count",
                "reminder", "endDate", "shoppingList", "status", "currentCount", "shoppingListState"};
        Cursor cursor;
        cursor = DB.query("tasks", columns, null, null, null, null, "status ASC, endDate");
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getString(7),
                            cursor.getString(8),
                            cursor.getString(9),
                            cursor.getInt(10),
                            cursor.getString(11)
                    );
                    if (task.type == Task.TYPE.TEMPLATE)
                        templates.add(task);
                    else if (task.type == Task.TYPE.SHOPPING_LIST)
                        shoppingLists.add(task);
                } while (cursor.moveToNext());
            }
        }
        DB.close();
        drawTemplates();
        drawShoppingLists();
    }

    public void drawTemplates() {
        boolean templatesEmpty = true;
        templatesList.removeAllViews();
        int num = 1;
        for (Task task: templates) {
            LinearLayout taskRow = TaskView.getTaskRow(
                    this, num, task, true, false, Timespan.TODAY, new Callable() {
                        @Override
                        public Object call() throws Exception {
                            drawTemplatesAndShoppingLists();
                            return null;
                        }
                    });
            templatesList.addView(taskRow);
            num++;
            templatesEmpty = false;
        }
        if (templatesEmpty) {
            LinearLayout noTemplates = new LinearLayout(this);
            LinearLayout.LayoutParams noTemplatesParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            noTemplatesParams.setMargins(0, 0, 0, 1);
            noTemplates.setLayoutParams(noTemplatesParams);
            noTemplates.setOrientation(LinearLayout.HORIZONTAL);
            noTemplates.setBackgroundColor(0xFFFFFFFF);
            noTemplates.setPadding(15 * DP, 10 * DP, 15 * DP, 10 * DP);

            TextView noTemplatesText = new TextView(this);
            LinearLayout.LayoutParams noShopListTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            noTemplatesText.setLayoutParams(noShopListTextParams);
            noTemplatesText.setText("Нет шаблонов");
            noTemplatesText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            noTemplatesText.setTextColor(0xFF555555);

            noTemplates.addView(noTemplatesText);
            templatesList.addView(noTemplates);
        }
    }

    public void drawShoppingLists() {
        boolean shoppingListsEmpty = true;
        shoppingListsList.removeAllViews();
        int num = 1;
        for (Task task: shoppingLists)
            if (task.status == Task.STATUS.ACTUAL || task.status == Task.STATUS.IN_PROCESS) {
            LinearLayout taskRow = TaskView.getTaskRow(
                    this, num, task, false, true, Timespan.MONTH, new Callable() {
                        @Override
                        public Object call() throws Exception {
                            drawTemplatesAndShoppingLists();
                            return null;
                        }
                    });
            shoppingListsList.addView(taskRow);
            result.setText(result.getText() + task.name + " " + task.customEndDate.toString() + "\n");
            num++;
            shoppingListsEmpty = false;
        }
        if (shoppingListsEmpty) {
            LinearLayout noShoppingLists = new LinearLayout(this);
            LinearLayout.LayoutParams noShoppingListsParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            noShoppingListsParams.setMargins(0, 0, 0, 1);
            noShoppingLists.setLayoutParams(noShoppingListsParams);
            noShoppingLists.setOrientation(LinearLayout.HORIZONTAL);
            noShoppingLists.setBackgroundColor(0xFFFFFFFF);
            noShoppingLists.setPadding(15 * DP, 10 * DP, 15 * DP, 10 * DP);

            TextView noShopListText = new TextView(this);
            LinearLayout.LayoutParams noShopListTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            noShopListText.setLayoutParams(noShopListTextParams);
            noShopListText.setText("Нет активных списков покупок");
            noShopListText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            noShopListText.setTextColor(0xFF555555);

            noShoppingLists.addView(noShopListText);
            shoppingListsList.addView(noShoppingLists);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawTemplatesAndShoppingLists();
    }

}