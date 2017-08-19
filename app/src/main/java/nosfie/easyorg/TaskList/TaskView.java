package nosfie.easyorg.TaskList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Callable;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.Daytime;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;
import nosfie.easyorg.R;
import nosfie.easyorg.ShoppingLists.EditTemplate;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static nosfie.easyorg.Helpers.DateStringsHelper.getShortMonthName;
import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class TaskView {

    private static Calendar alertDialogCalendar = Calendar.getInstance();
    private static int DP = 0;
    private static int taskRowHeight = Constants.TASK_ROW_HEIGHT;
    private static Callable updateCallback;

    public static LinearLayout getTaskRow(
            final Context context, int num, final Task task,
            boolean showIcon, boolean showEditButton, Timespan timespan, Callable uc) {

        DP = convertDpToPixels(context, 1);
        updateCallback = uc;

        // Main row
        LinearLayout row = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        row.setLayoutParams(params);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundColor(0xFFAAAAAA);
        row.setPadding(0, 0, 0, 1);

        // Number Row
        LinearLayout numberRow = new LinearLayout(context);
        LinearLayout.LayoutParams numberRowParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        numberRowParams.width = 40 * DP;
        numberRowParams.setMargins(0, 0, 1, 0);
        numberRow.setGravity(Gravity.CENTER);
        numberRow.setLayoutParams(numberRowParams);
        numberRow.setBackgroundColor(0xFFFFFFFF);
        numberRow.setOrientation(LinearLayout.HORIZONTAL);

        if (task.needReminder) {
            ImageView reminder = new ImageView(context);
            LinearLayout.LayoutParams reminderParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            reminderParams.width = 24 * DP;
            reminder.setLayoutParams(reminderParams);
            reminder.setAdjustViewBounds(true);
            reminder.setImageResource(R.drawable.bell_icon_small);
            numberRow.addView(reminder);
        }
        else {
            TextView number = new TextView(context);
            LinearLayout.LayoutParams numberParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
            number.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            number.setText(Integer.toString(num));
            number.setTypeface(null, Typeface.BOLD);
            number.setGravity(Gravity.CENTER);
            number.setLayoutParams(numberParams);
            numberRow.addView(number);
        }
        row.addView(numberRow);

        // Task name row
        LinearLayout taskNameRow = new LinearLayout(context);
        LinearLayout.LayoutParams taskNameRowParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        taskNameRowParams.setMargins(0, 0, 1, 0);
        taskNameRowParams.weight = 1;
        taskNameRow.setLayoutParams(taskNameRowParams);
        taskNameRow.setOrientation(LinearLayout.HORIZONTAL);
        taskNameRow.setMinimumHeight(DP * taskRowHeight);
        taskNameRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processTaskNameClick(context, task);
            }
        });

        RelativeLayout taskNameRelative  = new RelativeLayout(context);
        LinearLayout.LayoutParams taskRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        taskNameRelative.setLayoutParams(taskRowParams);
        taskNameRow.addView(taskNameRelative);

        LinearLayout taskBgContainer = new LinearLayout(context);
        RelativeLayout.LayoutParams taskBgContainerParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        taskBgContainer.setLayoutParams(taskBgContainerParams);
        taskBgContainer.setOrientation(LinearLayout.HORIZONTAL);
        taskBgContainer.setBackgroundColor(context.getResources().getColor(R.color.colorTaskActual));
        taskBgContainer.setWeightSum(100);

        LinearLayout taskBg = new LinearLayout(context);
        LinearLayout.LayoutParams taskBgParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        if (task.type == Task.TYPE.SIMPLE || task.status != Task.STATUS.ACTUAL)
            taskBgParams.weight = 100;
        else
            taskBgParams.weight = (int)(((double)task.currentCount / (double)task.count) * 100);
        taskBg.setLayoutParams(taskBgParams);
        int color = 0;
        switch (task.status) {
            case ACTUAL:
                if (task.type == Task.TYPE.SIMPLE)
                    color = R.color.colorTaskActual;
                else
                    color = R.color.colorTaskDone;
                break;
            case DONE:
                color = R.color.colorTaskDone;
                break;
            case NOT_DONE:
                color = R.color.colorTaskFailed;
                break;
            case IN_PROCESS:
                color = R.color.colorTaskInProcess;
                break;
            case POSTPONED:
                color = R.color.colorTaskPostponed;
                break;
        }
        taskBg.setBackgroundColor(context.getResources().getColor(color));
        taskBg.setId(task.id);
        taskBgContainer.addView(taskBg);

        taskNameRelative.addView(taskBgContainer);

        TextView timeText = new TextView(context);
        RelativeLayout.LayoutParams timeTextParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        timeTextParams.setMargins(8 * DP, 0, 0, 0);
        timeText.setGravity(Gravity.CENTER_VERTICAL);
        timeText.setLayoutParams(timeTextParams);
        timeText.setTextColor(0xFFE50000);
        timeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        if (timespan == Timespan.TODAY) {
            timeText.setText(task.customStartTime.toString().replaceAll("-", ":"));
            if (task.startTime.toString().equals("NONE"))
                timeText.setVisibility(View.GONE);
        }
        else if (timespan == Timespan.WEEK || timespan == Timespan.MONTH) {
            if (task.customStartDate.toString().equals(task.customEndDate.toString()))
                timeText.setText(Integer.toString(task.customEndDate.day) + " "
                        + getShortMonthName(task.customEndDate.month - 1, false));
            else
                timeText.setVisibility(View.GONE);
        }
        else if (timespan == Timespan.YEAR) {
            if (task.customStartDate.month == task.customEndDate.month)
                timeText.setText(getShortMonthName(task.customEndDate.month - 1, false));
            else
                timeText.setVisibility(View.GONE);
        }
        else
            timeText.setVisibility(View.GONE);
        timeText.setId(2000 + task.id);
        taskNameRelative.addView(timeText);


        ImageView icon = new ImageView(context);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(
                24 * DP,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        if (task.type == Task.TYPE.SHOPPING_LIST) {
            iconParams.setMargins(8 * DP, 8 * DP, 2 * DP, 8 * DP);
            icon.setImageResource(R.drawable.cart_icon_small);
        }
        else if (task.type == Task.TYPE.TEMPLATE) {
            iconParams.width = 16 * DP;
            iconParams.setMargins(11 * DP, 8 * DP, 0 * DP, 8 * DP);
            icon.setImageResource(R.drawable.template_icon_small);
        }
        iconParams.addRule(RelativeLayout.RIGHT_OF, 2000 + task.id);
        icon.setLayoutParams(iconParams);
        icon.setAdjustViewBounds(true);
        icon.setId(3000 + task.id);
        taskNameRelative.addView(icon);
        if ((task.type != Task.TYPE.TEMPLATE && task.type != Task.TYPE.SHOPPING_LIST) || !showIcon)
            icon.setVisibility(View.GONE);

        TextView taskName = new TextView(context);
        RelativeLayout.LayoutParams taskNameParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        taskNameParams.addRule(RelativeLayout.RIGHT_OF, 3000 + task.id);
        taskName.setLayoutParams(taskNameParams);
        taskName.setPadding(8 * DP, 4 * DP, 4 * DP, 4 * DP);
        taskName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        taskName.setGravity(Gravity.CENTER_VERTICAL);
        if (task.type == Task.TYPE.SIMPLE) {
            taskName.setText(task.name);
        }
        else if (task.type == Task.TYPE.TEMPLATE) {
            taskName.setText(Html.fromHtml("<font>" + task.name + "</font>" +
                    " <font color=\"#e50000\">(" + task.count + ")</font>"));
        }
        else {
            taskName.setText(Html.fromHtml("<font>" + task.name + "</font>" +
                    " <font color=\"#e50000\">(" + task.currentCount + "/" + task.count + ")</font>"));
        }
        taskNameRelative.addView(taskName);

        row.addView(taskNameRow);

        // Buttons row
        LinearLayout buttonsRow = new LinearLayout(context);
        LinearLayout.LayoutParams buttonsRowParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        buttonsRowParams.setMargins(0, 0, 1, 0);
        buttonsRow.setLayoutParams(buttonsRowParams);
        buttonsRow.setBackgroundColor(0xFFFFFFFF);
        buttonsRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonsRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView editImage = new ImageView(context);
        LinearLayout.LayoutParams editImageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        editImageParams.height = 28 * DP;
        editImageParams.weight = 1;
        editImage.setLayoutParams(editImageParams);
        editImage.setPadding(18, 0, 12, 0);
        editImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        editImage.setAdjustViewBounds(true);
        editImage.setImageResource(R.drawable.edit_icon_small);
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processEditTaskClick(context, task);
            }
        });
        if (!showEditButton)
            editImage.setVisibility(View.GONE);

        buttonsRow.addView(editImage);

        ImageView deleteImage = new ImageView(context);
        LinearLayout.LayoutParams deleteImageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        deleteImageParams.height = 28 * DP;
        deleteImageParams.weight = 1;
        deleteImage.setLayoutParams(deleteImageParams);
        if (!showEditButton)
            deleteImage.setPadding(18, 0, 18, 0);
        else
            deleteImage.setPadding(12, 0, 18, 0);
        deleteImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        deleteImage.setAdjustViewBounds(true);
        deleteImage.setImageResource(R.drawable.delete_icon_small);
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processDeleteImageClick(context, task);
            }
        });

        buttonsRow.addView(deleteImage);
        row.addView(buttonsRow);
        return row;
    }

    private static void processTaskNameClick(final Context context, final Task task) {
        switch (task.type) {
            case SIMPLE:
                showSimpleTaskDialog(context, task);
                break;
            case COUNTABLE:
                showCountableTaskDialog(context, task);
                break;
            case SHOPPING_LIST:
                Intent intent = new Intent(context, ShoppingList.class);
                intent.putExtra("id", task.id);
                intent.putExtra("taskName", task.name);
                intent.putExtra("shoppingList", task.shoppingList);
                intent.putExtra("shoppingListState", task.shoppingListState);
                context.startActivity(intent);
                break;
            case TEMPLATE:
                Intent templateIntent = new Intent(context, EditTemplate.class);
                templateIntent.putExtra("id", task.id);
                templateIntent.putExtra("taskName", task.name);
                templateIntent.putExtra("shoppingList", task.shoppingList);
                context.startActivity(templateIntent);
                break;
        }
    }

    private static void showCountableTaskDialog(final Context context, final Task task) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        final Dialog countableDialog = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.countable_dialog,
                (ViewGroup)rootView.findViewById(R.id.countable_dialog_root));
        countableDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        countableDialog.setContentView(layout);

        final RadioButton radioActual = (RadioButton)layout.findViewById(R.id.radioActual);
        final RadioButton radioDone = (RadioButton)layout.findViewById(R.id.radioDone);
        final RadioButton radioNotDone = (RadioButton)layout.findViewById(R.id.radioNotDone);
        final RadioButton radioPostponed = (RadioButton)layout.findViewById(R.id.radioPostponed);
        LinearLayout countableDialogRoot = (LinearLayout)layout.findViewById(R.id.countable_dialog_root);
        countableDialogRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        final SeekBar seekBar = (SeekBar)layout.findViewById(R.id.countableSeekbar);
        seekBar.setProgress(task.currentCount);
        seekBar.setMax(task.count);
        final TextView countText = (TextView)layout.findViewById(R.id.count_text);
        countText.setText(task.currentCount + "/" + task.count);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                countText.setText(i + "/" + task.count);
                if (i == task.count) {
                    radioDone.setChecked(true);
                    radioActual.setEnabled(false);
                    radioNotDone.setEnabled(false);
                    radioPostponed.setEnabled(false);
                }
                else {
                    radioActual.setChecked(true);
                    radioActual.setEnabled(true);
                    radioNotDone.setEnabled(true);
                    radioPostponed.setEnabled(true);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBar.setProgress(task.count);
            }
        });

        switch (task.status) {
            case ACTUAL:
                radioActual.setChecked(true);
                break;
            case DONE:
                radioDone.setChecked(true);
                break;
            case NOT_DONE:
                radioNotDone.setChecked(true);
                break;
            case POSTPONED:
                radioPostponed.setChecked(true);
                break;
        }
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countableDialog.dismiss();
            }
        });
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioActual.isChecked())
                    task.status = Task.STATUS.ACTUAL;
                if (radioNotDone.isChecked())
                    task.status = Task.STATUS.NOT_DONE;
                if (radioPostponed.isChecked())
                    task.status = Task.STATUS.POSTPONED;

                task.currentCount = seekBar.getProgress();
                if (task.currentCount == task.count)
                    task.status = Task.STATUS.DONE;
                task.synchronize(context);
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countableDialog.dismiss();
            }
        });
        countableDialog.show();
    }

    private static void showSimpleTaskDialog(final Context context, final Task task) {
        int status = 0;
        switch (task.status) {
            case ACTUAL:
                status = 0;
                break;
            case DONE:
                status = 1;
                break;
            case NOT_DONE:
                status = 2;
                break;
            case IN_PROCESS:
                status = 3;
                break;
            case POSTPONED:
                status = 4;
                break;
        }

        final CharSequence[] items = {"Актуальна", "Выполнена", "Не будет выполнена", "Частично выполнена", "Отложена"};
        final AlertDialog simpleTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Отметить статус задачи");

        builder.setSingleChoiceItems(items, status, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                int color = 0;
                switch (item)
                {
                    case 0:
                        color = R.color.colorTaskActual;
                        task.status = Task.STATUS.ACTUAL;
                        break;
                    case 1:
                        color = R.color.colorTaskDone;
                        task.status = Task.STATUS.DONE;
                        break;
                    case 2:
                        color = R.color.colorTaskFailed;
                        task.status = Task.STATUS.NOT_DONE;
                        break;
                    case 3:
                        color = R.color.colorTaskInProcess;
                        task.status = Task.STATUS.IN_PROCESS;
                        break;
                    case 4:
                        color = R.color.colorTaskPostponed;
                        task.status = Task.STATUS.POSTPONED;
                        break;
                }
                View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
                LinearLayout taskRow = (LinearLayout)rootView.findViewById(task.id);
                taskRow.setBackgroundColor(context.getResources().getColor(color));
                task.synchronize(context);
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        simpleTaskDialog = builder.create();
        simpleTaskDialog.show();
    }

    private static void processDeleteImageClick(final Context context, final Task task) {
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle("Подтвердите удаление");  // заголовок
        if (task.type == Task.TYPE.TEMPLATE)
            ad.setMessage("Вы действительно хотите удалить шаблон \"" + task.name + "\" ?"); // сообщение
        else
            ad.setMessage("Вы действительно хотите удалить задачу \"" + task.name + "\" ?"); // сообщение
        ad.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                task.delete(context);
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Задача удалена", Toast.LENGTH_SHORT).show();
            }
        });
        ad.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                ///
            }
        });
        ad.setCancelable(true);
        ad.show();
    }

    private static void processEditTaskClick(Context context, Task task) {
        switch (task.type) {
            case SIMPLE:
                showSimpleTaskEditDialog(context, task);
                break;
            case SHOPPING_LIST:
                showShoppingListTaskEditDialog(context, task);
                break;
            case COUNTABLE:
                showCountableTaskEditDialog(context, task);
                break;
        }
    }

    private static void showSimpleTaskEditDialog(final Context context, final Task task) {
        final CharSequence[] items = {
                "Название",
                "Дату начала",
                "Время начала",
                "Дату окончания",
                "Напоминание"};
        final AlertDialog simpleTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Изменить:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        showEditTaskNameDialog(context, task);
                        break;
                    case 1:
                        showEditTaskStartDateDialog(context, task);
                        break;
                    case 2:
                        showEditTaskStartTimeDialog(context, task);
                        break;
                    case 3:
                        showEditTaskEndDateDialog(context, task);
                        break;
                    case 4:
                        toggleTaskReminder(context, task);
                        break;
                }
                dialog.dismiss();
            }
        });
        simpleTaskDialog = builder.create();
        simpleTaskDialog.show();
    }

    private static void showEditTaskNameDialog(final Context context, final Task task) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog editTextDialog = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.edit_task_name_dialog,
                (ViewGroup)rootView.findViewById(R.id.edit_name_root));
        editTextDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editTextDialog.setContentView(layout);
        final EditText editTask = (EditText)layout.findViewById(R.id.task_name);
        editTask.setText(task.name);
        LinearLayout editTextRoot = (LinearLayout)layout.findViewById(R.id.edit_name_root);
        editTextRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.name = editTask.getText().toString();
                task.synchronize(context);
                editTextDialog.dismiss();
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Задача обновлена", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextDialog.dismiss();
            }
        });
        editTextDialog.show();
    }


    private static void showEditTaskStartDateDialog(final Context context, final Task task) {
        Calendar calendar = new GregorianCalendar(
                task.customStartDate.year,
                task.customStartDate.month - 1,
                task.customStartDate.day
        );
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog editStartDateDialog = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.edit_start_date_dialog,
                (ViewGroup)rootView.findViewById(R.id.edit_start_date_root));
        editStartDateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editStartDateDialog.setContentView(layout);

        final CalendarView calendarView = (CalendarView)layout.findViewById(R.id.calendarView);
        long today = Calendar.getInstance().getTimeInMillis() - 3 * 60 * 60 * 1000;
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTimeInMillis(today);
        todayCalendar.add(Calendar.HOUR_OF_DAY, -todayCalendar.get(Calendar.HOUR_OF_DAY));
        todayCalendar.add(Calendar.MINUTE, -todayCalendar.get(Calendar.MINUTE));
        todayCalendar.add(Calendar.SECOND, -todayCalendar.get(Calendar.SECOND));

        calendarView.setMinDate(todayCalendar.getTimeInMillis());
        calendarView.setDate(calendar.getTimeInMillis(), false, true);

        // MinDate bug workaround
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            calendar.add(Calendar.MONTH, 24);
            calendarView.setDate(calendar.getTimeInMillis(), false, true);
            calendar.add(Calendar.MONTH, -24);
            calendar.add(Calendar.SECOND, 1);
            calendarView.setDate(calendar.getTimeInMillis(), false, true);
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView,
                                            int year, int month, int date) {
                alertDialogCalendar = new GregorianCalendar(year, month, date);
            }
        });

        LinearLayout editStartDateRoot = (LinearLayout)layout.findViewById(R.id.edit_start_date_root);
        editStartDateRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.startDate = Task.START_DATE.CUSTOM;
                task.customStartDate.year = alertDialogCalendar.get(Calendar.YEAR);
                task.customStartDate.month = alertDialogCalendar.get(Calendar.MONTH) + 1;
                task.customStartDate.day = alertDialogCalendar.get(Calendar.DAY_OF_MONTH);
                if (task.customEndDate.toString().compareTo(task.customStartDate.toString()) <= 0)
                    task.customEndDate = task.customStartDate;
                task.synchronize(context);
                editStartDateDialog.dismiss();
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Задача обновлена", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editStartDateDialog.dismiss();
            }
        });
        editStartDateDialog.show();
    }

    private static void showEditTaskStartTimeDialog(final Context context, final Task task) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog editTimeDialog = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.edit_time_dialog,
                (ViewGroup)rootView.findViewById(R.id.edit_time_root));
        editTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editTimeDialog.setContentView(layout);

        final RadioButton radioNoTime = (RadioButton)layout.findViewById(R.id.radioNoTime);
        final RadioButton radioCustomTime = (RadioButton)layout.findViewById(R.id.radioCustomTime);
        if (task.startTime == Task.START_TIME.NONE)
            radioNoTime.setChecked(true);
        if (task.startTime == Task.START_TIME.CUSTOM)
            radioCustomTime.setChecked(true);

        final TimePicker timePicker = (TimePicker)layout.findViewById(R.id.timePicker);
        if (task.customStartTime == null)
            task.customStartTime = new Daytime();
        timePicker.setCurrentHour(task.customStartTime.hours);
        timePicker.setCurrentMinute(task.customStartTime.minutes);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                radioCustomTime.setChecked(true);
            }
        });

        LinearLayout editTimeRoot = (LinearLayout)layout.findViewById(R.id.edit_time_root);
        editTimeRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioNoTime.isChecked()) {
                    task.startTime = Task.START_TIME.NONE;
                    task.needReminder = false;
                }
                else {
                    task.startTime = Task.START_TIME.CUSTOM;
                    task.customStartTime.hours = timePicker.getCurrentHour();
                    task.customStartTime.minutes = timePicker.getCurrentMinute();
                }
                task.synchronize(context);
                editTimeDialog.dismiss();
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Задача обновлена", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTimeDialog.dismiss();
            }
        });
        editTimeDialog.show();
    }

    private static void showEditTaskEndDateDialog(final Context context, final Task task) {
        Calendar calendar = new GregorianCalendar(
                task.customEndDate.year,
                task.customEndDate.month - 1,
                task.customEndDate.day
        );
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog editEndDateDialog = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.edit_end_date_dialog,
                (ViewGroup)rootView.findViewById(R.id.edit_end_date_root));
        editEndDateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editEndDateDialog.setContentView(layout);

        final RadioButton radioNoTime = (RadioButton)layout.findViewById(R.id.radioNoTime);
        final RadioButton radioCustomTime = (RadioButton)layout.findViewById(R.id.radioCustomTime);
        if (task.deadline == Task.DEADLINE.NONE)
            radioNoTime.setChecked(true);
        if (task.deadline == Task.DEADLINE.CUSTOM)
            radioCustomTime.setChecked(true);

        final CalendarView calendarView = (CalendarView)layout.findViewById(R.id.calendarView);
        if (task.deadline == Task.DEADLINE.NONE)
            calendar = Calendar.getInstance();

        long today = Calendar.getInstance().getTimeInMillis() - 3 * 60 * 60 * 1000;
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTimeInMillis(today);
        todayCalendar.add(Calendar.HOUR_OF_DAY, -todayCalendar.get(Calendar.HOUR_OF_DAY));
        todayCalendar.add(Calendar.MINUTE, -todayCalendar.get(Calendar.MINUTE));
        todayCalendar.add(Calendar.SECOND, -todayCalendar.get(Calendar.SECOND));

        calendarView.setMinDate(todayCalendar.getTimeInMillis());
        calendarView.setDate(calendar.getTimeInMillis(), false, true);

        // MinDate bug workaround
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            calendar.add(Calendar.MONTH, 24);
            calendarView.setDate(calendar.getTimeInMillis(), false, true);
            calendar.add(Calendar.MONTH, -24);
            calendar.add(Calendar.SECOND, 1);
            calendarView.setDate(calendar.getTimeInMillis(), false, true);
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView,
                                            int year, int month, int date) {
                alertDialogCalendar = new GregorianCalendar(year, month, date);
                radioCustomTime.setChecked(true);
            }
        });

        LinearLayout editEndDateRoot = (LinearLayout)layout.findViewById(R.id.edit_end_date_root);
        editEndDateRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioNoTime.isChecked()) {
                    task.deadline = Task.DEADLINE.NONE;
                    task.needReminder = false;
                    task.startTime = Task.START_TIME.NONE;
                }
                else {
                    task.deadline = Task.DEADLINE.CUSTOM;
                    task.customEndDate.year = alertDialogCalendar.get(Calendar.YEAR);
                    task.customEndDate.month = alertDialogCalendar.get(Calendar.MONTH) + 1;
                    task.customEndDate.day = alertDialogCalendar.get(Calendar.DAY_OF_MONTH);
                    if (task.customEndDate.toString().compareTo(task.customStartDate.toString()) <= 0)
                        task.customStartDate = task.customEndDate;
                }
                task.synchronize(context);
                editEndDateDialog.dismiss();
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Задача обновлена", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editEndDateDialog.dismiss();
            }
        });
        editEndDateDialog.show();
    }

    private static void toggleTaskReminder(final Context context, Task task) {
        if (task.startTime.toString().equals("NONE"))
            Toast.makeText(context, "Установите сначала время начала задачи",
                    Toast.LENGTH_SHORT).show();
        else {
            task.needReminder = !task.needReminder;
            task.synchronize(context);
            try {
                updateCallback.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (task.needReminder)
                Toast.makeText(context, "Напоминание установлено", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Напоминание убрано", Toast.LENGTH_SHORT).show();
        }
    }

    private static void showCountableTaskEditDialog(final Context context, final Task task) {
        final CharSequence[] items = {
                "Название",
                "Количественную цель",
                "Дату начала",
                "Время начала",
                "Дату окончания",
                "Напоминание"
        };
        final AlertDialog countableTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Изменить:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        showEditTaskNameDialog(context, task);
                        break;
                    case 1:
                        showEditTaskCountDialog(context, task);
                        break;
                    case 2:
                        showEditTaskStartDateDialog(context, task);
                        break;
                    case 3:
                        showEditTaskStartTimeDialog(context, task);
                        break;
                    case 4:
                        showEditTaskEndDateDialog(context, task);
                        break;
                    case 5:
                        toggleTaskReminder(context, task);
                        break;
                }
                dialog.dismiss();
            }
        });
        countableTaskDialog = builder.create();
        countableTaskDialog.show();
    }

    private static void showEditTaskCountDialog(final Context context, final Task task) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog editCountDialog = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.edit_count_dialog,
                (ViewGroup)rootView.findViewById(R.id.edit_count_root));
        editCountDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editCountDialog.setContentView(layout);
        final EditText editTask = (EditText)layout.findViewById(R.id.task_count);
        editTask.setText(Integer.toString(task.count));
        LinearLayout editTextRoot = (LinearLayout)layout.findViewById(R.id.edit_count_root);
        editTextRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.count = Integer.parseInt(editTask.getText().toString());
                if (task.currentCount >= task.count) {
                    task.currentCount = task.count;
                    task.status = Task.STATUS.DONE;
                }
                else {
                    if (task.status == Task.STATUS.DONE)
                        task.status = Task.STATUS.ACTUAL;
                }
                task.synchronize(context);
                editCountDialog.dismiss();
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Задача обновлена", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editCountDialog.dismiss();
            }
        });
        editCountDialog.show();
    }

    private static void showShoppingListTaskEditDialog(final Context context, final Task task) {
        final CharSequence[] items = {
                "Название",
                "Список покупок",
                "Дату начала",
                "Время начала",
                "Дату окончания",
                "Напоминание"
        };
        final AlertDialog countableTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Изменить:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        showEditTaskNameDialog(context, task);
                        break;
                    case 1:
                        Intent intent = new Intent(context, EditShoppingList.class);
                        intent.putExtra("id", task.id);
                        intent.putExtra("taskName", task.name + " " + task.customEndDate.toString());
                        intent.putExtra("shoppingList", task.shoppingList);
                        context.startActivity(intent);
                        break;
                    case 2:
                        showEditTaskStartDateDialog(context, task);
                        break;
                    case 3:
                        showEditTaskStartTimeDialog(context, task);
                        break;
                    case 4:
                        showEditTaskEndDateDialog(context, task);
                        break;
                    case 5:
                        toggleTaskReminder(context, task);
                        break;
                }
                dialog.dismiss();
            }
        });
        countableTaskDialog = builder.create();
        countableTaskDialog.show();
    }

}
