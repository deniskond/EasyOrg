package nosfie.easyorg.NewTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.R;

import static nosfie.easyorg.Database.Queries.getAllTemplatesFromDB;
import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;
import static nosfie.easyorg.NewTask.ShoppingListView.getShoppingItemRow;

public class NewTaskShoppingList extends AppCompatActivity {

    final int DEFAULT_INSERT_ROW_INDEX = 9;
    TableLayout shoppingListContainer;
    Task task = new Task();
    int insertRowIndex = DEFAULT_INSERT_ROW_INDEX;
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
        assert actionBar != null;
        actionBar.hide();

        // Setting up view elements
        shoppingListContainer = (TableLayout)findViewById(R.id.shoppingListContainer);
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
            for (String item : task.shoppingList) {
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
                buttonNextText.setText(getResources().getString(R.string.done));
        }
        else
            for (int num = 1; num < insertRowIndex; num++)
                addShoppingItemRow(num, "");

        // "Add shopping list button" event listener (onTouch)
        buttonAdd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonAdd.setImageResource(R.drawable.add_item_button_medium_dark);
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonAdd.setImageResource(R.drawable.add_item_button_medium);
                        shoppingListContainer.addView(getShoppingItemRow(NewTaskShoppingList.this, insertRowIndex, ""));
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
                        showSelectTemplateDialog();
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
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_input_shopping_list), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        task.count = task.shoppingList.size();
                        Intent intent;
                        if (lastScreen) {
                            task.insertIntoDatabase(getApplicationContext());
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.success_added_task), Toast.LENGTH_SHORT).show();
                            finish();
                            return true;
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

    private void addShoppingItemRow(int num, String name) {
        // Setting delete icon OnClickListener which will be used in each shopping list row
        View.OnClickListener deleteIconListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (insertRowIndex == 2) {
                    Toast.makeText(NewTaskShoppingList.this,
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

    private void showSelectTemplateDialog() {
        // Getting templates and checking if there are any
        final ArrayList<Task> templates = getAllTemplatesFromDB(this);
        if (templates.size() == 0) {
            Toast.makeText(NewTaskShoppingList.this, getResources().getString(R.string.error_no_templates),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Setting basic dialog elements
        WindowManager windowManager = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog selectTemplateDialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)this).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.templates_dialog,
                (ViewGroup)rootView.findViewById(R.id.dialog_root));
        selectTemplateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectTemplateDialog.setContentView(layout);
        LinearLayout root = (LinearLayout)layout.findViewById(R.id.dialog_root);
        root.setMinimumWidth((int)(displaySize.x * 0.85f));

        // Filling RadioGroup with templates
        final RadioGroup templatesList = (RadioGroup)layout.findViewById(R.id.templatesList);
        templatesList.removeAllViews();
        boolean first = true;
        for (Task template: templates) {
            AppCompatRadioButton radioButton = new AppCompatRadioButton(this);
            radioButton.setText(template.name);
            RadioGroup.LayoutParams radioButtonParams = new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            int DP = convertDpToPixels(this, 1);
            if (!first)
                radioButtonParams.setMargins(0, 10 * DP, 0, 0);
            else
                first = false;
            radioButton.setLayoutParams(radioButtonParams);
            radioButton.setPadding(5 * DP, 0, 0, 0);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked},
                            new int[]{android.R.attr.state_checked}
                    },
                    new int[]{
                            R.color.checkboxChecked,
                            R.color.checkboxUnchecked
                    }
            );
            radioButton.setSupportButtonTintList(colorStateList);
            templatesList.addView(radioButton);
        }

        // Setting OK and Cancel button listeners
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shoppingListContainer.removeAllViews();
                insertRowIndex = DEFAULT_INSERT_ROW_INDEX;
                int radioButtonID = templatesList.getCheckedRadioButtonId();
                View radioButton = templatesList.findViewById(radioButtonID);
                int idChecked = templatesList.indexOfChild(radioButton);
                Task selectedTemplate = templates.get(idChecked);
                int num = 1;
                for (String item: selectedTemplate.shoppingList)
                    addShoppingItemRow(num++, item);
                if (num < insertRowIndex)
                    for (int i = num; i < insertRowIndex; i++)
                        addShoppingItemRow(num++, "");
                else
                    insertRowIndex = num;
                selectTemplateDialog.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTemplateDialog.dismiss();
            }
        });

        // Showing
        selectTemplateDialog.show();
    }

}