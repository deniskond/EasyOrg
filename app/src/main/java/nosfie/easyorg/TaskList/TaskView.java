package nosfie.easyorg.TaskList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CompoundButton;
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
import java.util.Objects;
import java.util.concurrent.Callable;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.CustomDate;
import nosfie.easyorg.DataStructures.DayValues;
import nosfie.easyorg.DataStructures.Daytime;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;
import nosfie.easyorg.Notes.ViewNote;
import nosfie.easyorg.R;
import nosfie.easyorg.ShoppingLists.EditTemplate;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static nosfie.easyorg.Helpers.DateStringsHelper.getShortMonthName;
import static nosfie.easyorg.Helpers.ViewHelper.convertDpToPixels;

public class TaskView {

    private static boolean ignoreCheckedChange = false;
    private static Calendar alertDialogCalendar = Calendar.getInstance();
    private static int taskRowHeight = Constants.TASK_ROW_HEIGHT;
    private static Callable updateCallback;
    private static Callable stateCallback;
    private static int
            colorTaskActual = 0,
            colorTaskDone = 0,
            colorTaskFailed = 0,
            colorTaskInProcess = 0,
            colorTaskPostponed = 0;
    private static Daytime dayMargin;
    private static CustomDate swapStartDate = new CustomDate();
    private static Timespan globalTimespan;

    public static LinearLayout getTaskRow(
            final Context context, int num, final Task task,
            boolean showIcon, boolean showEditButton, Timespan timespan,
            Callable uc, Callable sc) {

        // Filling values
        globalTimespan = timespan;
        int DP = convertDpToPixels(context, 1);
        updateCallback = uc;
        stateCallback = sc;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final int colorTaskActual = preferences.getInt("colorTaskActual", -1);
        final int colorTaskDone = preferences.getInt("colorTaskDone", -1);
        final int colorTaskFailed = preferences.getInt("colorTaskFailed", -1);
        final int colorTaskInProcess = preferences.getInt("colorTaskInProcess", -1);
        final int colorTaskPostponed = preferences.getInt("colorTaskPostponed", -1);
        String[] timeSplit = preferences.getString("dayMargin", "").split(":");
        int hours = Integer.parseInt(timeSplit[0]);
        int minutes = Integer.parseInt(timeSplit[1]);
        dayMargin = new Daytime(hours, minutes);

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

        if (task.type == Task.TYPE.NOTE && task.count != 0) {
            ImageView noteIcon = new ImageView(context);
            LinearLayout.LayoutParams reminderParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            reminderParams.width = 20 * DP;
            noteIcon.setLayoutParams(reminderParams);
            noteIcon.setAdjustViewBounds(true);
            switch (task.count) {
                case 1:
                    noteIcon.setImageResource(R.drawable.note_icon_1);
                    break;
                case 2:
                    noteIcon.setImageResource(R.drawable.note_icon_2);
                    break;
                case 3:
                    noteIcon.setImageResource(R.drawable.note_icon_3);
                    break;
                case 4:
                    noteIcon.setImageResource(R.drawable.note_icon_4);
                    break;
                case 5:
                    noteIcon.setImageResource(R.drawable.note_icon_5);
                    break;
                case 6:
                    noteIcon.setImageResource(R.drawable.note_icon_6);
                    break;
            }
            numberRow.addView(noteIcon);
        }
        else if (task.needReminder) {
            ImageView reminder = new ImageView(context);
            LinearLayout.LayoutParams reminderParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            reminderParams.width = 24 * DP;
            reminder.setLayoutParams(reminderParams);
            reminder.setAdjustViewBounds(true);
            switch (task.reminderTime) {
                case EXACT:
                    reminder.setImageResource(R.drawable.bell_icon_small);
                    break;
                case FIVE_MINS:
                    reminder.setImageResource(R.drawable.reminder_5m);
                    break;
                case TEN_MINS:
                    reminder.setImageResource(R.drawable.reminder_10m);
                    break;
                case THIRTY_MINS:
                    reminder.setImageResource(R.drawable.reminder_30m);
                    break;
                case ONE_HOUR:
                    reminder.setImageResource(R.drawable.reminder_1h);
                    break;
                default:
                    reminder.setImageResource(R.drawable.bell_icon_small);
                    break;
            }
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
        taskBgContainer.setBackgroundColor(colorTaskActual);
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
                    color = colorTaskActual;
                else
                    color = colorTaskDone;
                break;
            case DONE:
                color = colorTaskDone;
                break;
            case NOT_DONE:
                color = colorTaskFailed;
                break;
            case IN_PROCESS:
                color = colorTaskInProcess;
                break;
            case POSTPONED:
                color = colorTaskPostponed;
                break;
        }
        taskBg.setBackgroundColor(color);
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

        // Icon visibility rules
        if (task.type == Task.TYPE.SIMPLE) icon.setVisibility(View.GONE);
        if (task.type == Task.TYPE.COUNTABLE) icon.setVisibility(View.GONE);
        if (!showIcon) icon.setVisibility(View.GONE);
        if (task.type == Task.TYPE.NOTE) icon.setVisibility(View.GONE);

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
        else if (task.type == Task.TYPE.NOTE) {
            if (task.name == null || Objects.equals(task.name, "") || task.name.length() == 0) {
                if (task.text.length() > 20)
                    task.name = task.text.substring(0, 20) + "...";
                else
                    task.name = task.text;
            }
            task.name = task.name.replace('\n', ' ');
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
                intent.putExtra("timespan", globalTimespan.toString());
                intent.putExtra("taskStatus", task.status.toString());
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
            case NOTE:
                Intent noteIntent = new Intent(context, ViewNote.class);
                noteIntent.putExtra("id", task.id);
                noteIntent.putExtra("name", task.name);
                noteIntent.putExtra("text", task.text);
                noteIntent.putExtra("date", task.customEndDate.toString());
                noteIntent.putExtra("count", task.count);
                context.startActivity(noteIntent);
                break;
        }
    }

    private static void showCountableTaskDialog(final Context context, final Task task) {
        final Task.STATUS initialStatus = task.status;

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
                task.intervalFinishedTime = getTaskIntervalFinishedTime(context, task, initialStatus);
                task.synchronize(context);
                try {
                    stateCallback.call();
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
        final Task.STATUS initialStatus = task.status;
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

        final CharSequence[] items = {
                context.getResources().getString(R.string.task_status_actual),
                context.getResources().getString(R.string.task_status_done),
                context.getResources().getString(R.string.task_status_failed),
                context.getResources().getString(R.string.task_status_partly_done),
                context.getResources().getString(R.string.task_status_postponed)};
        final AlertDialog simpleTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.task_status_title));

        builder.setSingleChoiceItems(items, status, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                int color = 0;
                switch (item)
                {
                    case 0:
                        color = colorTaskActual;
                        task.status = Task.STATUS.ACTUAL;
                        break;
                    case 1:
                        color = colorTaskDone;
                        task.status = Task.STATUS.DONE;
                        break;
                    case 2:
                        color = colorTaskFailed;
                        task.status = Task.STATUS.NOT_DONE;
                        break;
                    case 3:
                        color = colorTaskInProcess;
                        task.status = Task.STATUS.IN_PROCESS;
                        break;
                    case 4:
                        color = colorTaskPostponed;
                        task.status = Task.STATUS.POSTPONED;
                        break;
                }
                View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
                LinearLayout taskRow = (LinearLayout)rootView.findViewById(task.id);
                taskRow.setBackgroundColor(color);
                task.intervalFinishedTime = getTaskIntervalFinishedTime(context, task, initialStatus);
                task.synchronize(context);
                try {
                    stateCallback.call();
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
        ad.setTitle(context.getResources().getString(R.string.confirm_delete));
        switch (task.type) {
            case TEMPLATE:
                ad.setMessage(context.getResources().getString(R.string.confirm_delete_template) + " \"" + task.name + "\" ?");
                break;
            case NOTE:
                ad.setMessage(context.getResources().getString(R.string.confirm_delete_note) + " \"" + task.name + "\" ?");
                break;
            default:
                ad.setMessage(context.getResources().getString(R.string.confirm_delete_task) + " \"" + task.name + "\" ?");
                break;
        }
        ad.setPositiveButton(context.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                task.delete(context);
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                switch (task.type) {
                    case TEMPLATE:
                        Toast.makeText(context, context.getResources().getString(R.string.template_deleted), Toast.LENGTH_SHORT).show();
                        break;
                    case NOTE:
                        Toast.makeText(context, context.getResources().getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(context, context.getResources().getString(R.string.task_deleted), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        ad.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
            case NOTE:
                showNoteEditDialog(context, task);
                break;
        }
    }

    private static void showSimpleTaskEditDialog(final Context context, final Task task) {
        final CharSequence[] items = {
                context.getResources().getString(R.string.edit_name),
                context.getResources().getString(R.string.edit_countable_goal),
                context.getResources().getString(R.string.edit_time_interval),
                context.getResources().getString(R.string.edit_start_time),
                context.getResources().getString(R.string.edit_reminder)};
        final AlertDialog simpleTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.change) + ":");
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
                        showEditTaskReminder(context, task);
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
                if (task.type != Task.TYPE.NOTE)
                    Toast.makeText(context, context.getResources().getString(R.string.task_updated), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, context.getResources().getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
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
        Calendar currentStartDateCalendar = new GregorianCalendar(
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
        long today = Calendar.getInstance().getTimeInMillis();
        today -= dayMargin.hours * 60 * 60 * 1000;
        today -= dayMargin.minutes * 60 * 1000;
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTimeInMillis(today);
        todayCalendar.add(Calendar.HOUR_OF_DAY, -todayCalendar.get(Calendar.HOUR_OF_DAY));
        todayCalendar.add(Calendar.MINUTE, -todayCalendar.get(Calendar.MINUTE));
        todayCalendar.add(Calendar.SECOND, -todayCalendar.get(Calendar.SECOND));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView,
                                            int year, int month, int date) {
                alertDialogCalendar = new GregorianCalendar(year, month, date);
            }
        });

        calendarView.setMinDate(todayCalendar.getTimeInMillis());
        Calendar targetCalendar;
        if (todayCalendar.getTimeInMillis() <= currentStartDateCalendar.getTimeInMillis())
            targetCalendar = currentStartDateCalendar;
        else
            targetCalendar = todayCalendar;

        alertDialogCalendar = new GregorianCalendar(targetCalendar.get(Calendar.YEAR),
                targetCalendar.get(Calendar.MONTH), targetCalendar.get(Calendar.DATE));

        // MinDate bug workaround
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            currentStartDateCalendar.add(Calendar.MONTH, 24);
            calendarView.setDate(targetCalendar.getTimeInMillis(), false, true);
            currentStartDateCalendar.add(Calendar.MONTH, -24);
            currentStartDateCalendar.add(Calendar.SECOND, 1);
            calendarView.setDate(targetCalendar.getTimeInMillis(), false, true);
        }

        LinearLayout editStartDateRoot = (LinearLayout)layout.findViewById(R.id.edit_start_date_root);
        editStartDateRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapStartDate.year = alertDialogCalendar.get(Calendar.YEAR);
                swapStartDate.month = alertDialogCalendar.get(Calendar.MONTH) + 1;
                swapStartDate.day = alertDialogCalendar.get(Calendar.DAY_OF_MONTH);
                editStartDateDialog.dismiss();
                showEditTaskEndDateDialog(context, task);
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
                Toast.makeText(context, context.getResources().getString(R.string.task_updated), Toast.LENGTH_SHORT).show();
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
        Calendar calendar;
        if (task.customEndDate.toString().compareTo(swapStartDate.toString()) <= 0) {
            calendar = new GregorianCalendar(
                    swapStartDate.year,
                    swapStartDate.month - 1,
                    swapStartDate.day
            );
            alertDialogCalendar = new GregorianCalendar(
                    swapStartDate.year, swapStartDate.month - 1, swapStartDate.day);
        }
        else {
            calendar = new GregorianCalendar(
                    task.customEndDate.year,
                    task.customEndDate.month - 1,
                    task.customEndDate.day
            );
            alertDialogCalendar = new GregorianCalendar(
                    task.customEndDate.year, task.customEndDate.month - 1, task.customEndDate.day);
        }
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

        Calendar startDateCalendar = new GregorianCalendar(
                swapStartDate.year,
                swapStartDate.month - 1,
                swapStartDate.day
        );
        calendarView.setMinDate(startDateCalendar.getTimeInMillis());
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
                task.startDate = Task.START_DATE.CUSTOM;
                task.customStartDate = swapStartDate;

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
                }
                task.synchronize(context);
                swapStartDate = new CustomDate();
                editEndDateDialog.dismiss();
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, context.getResources().getString(R.string.task_updated), Toast.LENGTH_SHORT).show();
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

    private static void showEditTaskReminder(final Context context, final Task task) {
        if (task.startTime.toString().equals("NONE")) {
            Toast.makeText(context, context.getResources().getString(R.string.error_set_task_time_first),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog editReminderDialog = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.reminder_dialog,
                (ViewGroup)rootView.findViewById(R.id.dialog_root));
        editReminderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editReminderDialog.setContentView(layout);
        LinearLayout editTextRoot = (LinearLayout)layout.findViewById(R.id.dialog_root);
        editTextRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        final RadioButton radioNone = (RadioButton)layout.findViewById(R.id.radioNone);
        radioNone.setVisibility(View.VISIBLE);
        final RadioButton radioExact = (RadioButton)layout.findViewById(R.id.radioExact);
        final RadioButton radio5Min = (RadioButton)layout.findViewById(R.id.radio5Min);
        final RadioButton radio10Min = (RadioButton)layout.findViewById(R.id.radio10Min);
        final RadioButton radio30Min = (RadioButton)layout.findViewById(R.id.radio30Min);
        final RadioButton radio1Hour = (RadioButton)layout.findViewById(R.id.radio1Hour);
        if (!task.needReminder) {
            radioNone.setChecked(true);
        }
        else {
            switch (task.reminderTime) {
                case EXACT:
                    radioExact.setChecked(true);
                    break;
                case FIVE_MINS:
                    radio5Min.setChecked(true);
                    break;
                case TEN_MINS:
                    radio10Min.setChecked(true);
                    break;
                case THIRTY_MINS:
                    radio30Min.setChecked(true);
                    break;
                case ONE_HOUR:
                    radio1Hour.setChecked(true);
                    break;
            }
        }

        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioNone.isChecked()) {
                    task.needReminder = false;
                    task.reminderTime = Task.REMINDER_TIME.EXACT;
                }
                else if (radioExact.isChecked()) {
                    task.needReminder = true;
                    task.reminderTime = Task.REMINDER_TIME.EXACT;
                }
                else if (radio5Min.isChecked()) {
                    task.needReminder = true;
                    task.reminderTime = Task.REMINDER_TIME.FIVE_MINS;
                }
                else if (radio10Min.isChecked()) {
                    task.needReminder = true;
                    task.reminderTime = Task.REMINDER_TIME.TEN_MINS;
                }
                else if (radio30Min.isChecked()) {
                    task.needReminder = true;
                    task.reminderTime = Task.REMINDER_TIME.THIRTY_MINS;
                }
                else if (radio1Hour.isChecked()) {
                    task.needReminder = true;
                    task.reminderTime = Task.REMINDER_TIME.ONE_HOUR;
                }
                task.synchronize(context);
                try {
                    updateCallback.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (task.needReminder)
                    Toast.makeText(context, context.getResources().getString(R.string.reminder_updated), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, context.getResources().getString(R.string.reminder_deleted), Toast.LENGTH_SHORT).show();
                editReminderDialog.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editReminderDialog.dismiss();
            }
        });
        editReminderDialog.show();
    }

    private static void showCountableTaskEditDialog(final Context context, final Task task) {
        final CharSequence[] items = {
                context.getResources().getString(R.string.edit_name),
                context.getResources().getString(R.string.edit_countable_goal),
                context.getResources().getString(R.string.edit_time_interval),
                context.getResources().getString(R.string.edit_start_time),
                context.getResources().getString(R.string.edit_reminder)
        };
        final AlertDialog countableTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.change) + ":");
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
                        showEditTaskReminder(context, task);
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
        if (task.count != 0)
            editTask.setText(Integer.toString(task.count));
        final RadioButton radioNoCount = (RadioButton)layout.findViewById(R.id.radioNoCount);
        final RadioButton radioIsCount = (RadioButton)layout.findViewById(R.id.radioIsCount);
        if (task.type == Task.TYPE.COUNTABLE)
            radioIsCount.setChecked(true);
        if (task.type == Task.TYPE.SIMPLE) {
            radioNoCount.setChecked(true);
            editTask.setText("");
        }
        editTask.setSelection(editTask.getText().length());
        editTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ignoreCheckedChange)
                    ignoreCheckedChange = false;
                else
                    radioIsCount.setChecked(true);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        radioNoCount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ignoreCheckedChange = true;
                    editTask.setText("");
                }
            }
        });
        LinearLayout editTextRoot = (LinearLayout)layout.findViewById(R.id.edit_count_root);
        editTextRoot.setMinimumWidth((int)(displaySize.x * 0.85f));
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTask.getText().toString().length() > 0)
                    task.count = Integer.parseInt(editTask.getText().toString());
                else
                    task.count = 0;
                if (task.count == 0)
                    task.type = Task.TYPE.SIMPLE;
                else {
                    if (radioIsCount.isChecked())
                        task.type = Task.TYPE.COUNTABLE;
                    else
                        task.type = Task.TYPE.SIMPLE;
                }
                if (task.currentCount >= task.count && task.count != 0) {
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
                Toast.makeText(context, context.getResources().getString(R.string.task_updated), Toast.LENGTH_SHORT).show();
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
                context.getResources().getString(R.string.edit_name),
                context.getResources().getString(R.string.shopping_list),
                context.getResources().getString(R.string.edit_time_interval),
                context.getResources().getString(R.string.edit_start_time),
                context.getResources().getString(R.string.edit_reminder)
        };
        final AlertDialog countableTaskDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.change) + ":");
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
                        showEditTaskReminder(context, task);
                        break;
                }
                dialog.dismiss();
            }
        });
        countableTaskDialog = builder.create();
        countableTaskDialog.show();
    }

    private static void showNoteEditDialog(final Context context, final Task task) {
        final CharSequence[] items = {
                context.getResources().getString(R.string.edit_name),
                context.getResources().getString(R.string.edit_note)
        };
        final AlertDialog noteDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.change) + ":");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        showEditTaskNameDialog(context, task);
                        break;
                    case 1:
                        showEditIconDialog(context, task);
                        break;
                }
                dialog.dismiss();
            }
        });
        noteDialog = builder.create();
        noteDialog.show();
    }

    private static void showEditIconDialog(final Context context, final Task task) {
        // Setting basic dialog elements
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog selectIconDialog = new Dialog(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.note_icon_dialog,
                (ViewGroup)rootView.findViewById(R.id.dialog_root));
        selectIconDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectIconDialog.setContentView(layout);
        LinearLayout root = (LinearLayout)layout.findViewById(R.id.dialog_root);
        root.setMinimumWidth((int)(displaySize.x * 0.85f));

        // Setting up dialog elements
        final LinearLayout videoRow = (LinearLayout)layout.findViewById(R.id.videoRow);
        final LinearLayout audioRow = (LinearLayout)layout.findViewById(R.id.audioRow);
        final LinearLayout moneyRow = (LinearLayout)layout.findViewById(R.id.moneyRow);
        final LinearLayout carRow = (LinearLayout)layout.findViewById(R.id.carRow);
        final LinearLayout gamesRow = (LinearLayout)layout.findViewById(R.id.gamesRow);
        final LinearLayout booksRow = (LinearLayout)layout.findViewById(R.id.booksRow);
        final LinearLayout videoBorder = (LinearLayout)layout.findViewById(R.id.videoBorder);
        final LinearLayout audioBorder = (LinearLayout)layout.findViewById(R.id.audioBorder);
        final LinearLayout moneyBorder = (LinearLayout)layout.findViewById(R.id.moneyBorder);
        final LinearLayout carBorder = (LinearLayout)layout.findViewById(R.id.carBorder);
        final LinearLayout gamesBorder = (LinearLayout)layout.findViewById(R.id.gamesBorder);
        final LinearLayout booksBorder = (LinearLayout)layout.findViewById(R.id.booksBorder);

        // Showing "no icon" option
        final LinearLayout noIconRow = (LinearLayout)layout.findViewById(R.id.noIconRow);
        final LinearLayout noIconBorder = (LinearLayout)layout.findViewById(R.id.noIconBorder);
        noIconRow.setVisibility(View.VISIBLE);

        // Setting OK and Cancel button listeners
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.synchronize(context);
                Toast.makeText(context, context.getResources().getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
                try {
                    updateCallback.call();
                }
                catch (Exception ignored) {}
                selectIconDialog.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectIconDialog.dismiss();
            }
        });

        // Setting icons select onClickListeners
        videoRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (task.count == 1)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        videoRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        videoRow.setBackgroundColor(0x00000000);
                        task.count = 1;
                        videoBorder.setBackgroundResource(R.drawable.border_big_selected);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        noIconBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        audioRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (task.count == 2)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        audioRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        audioRow.setBackgroundColor(0x00000000);
                        task.count = 2;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_big_selected);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        noIconBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        moneyRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (task.count == 3)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moneyRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        moneyRow.setBackgroundColor(0x00000000);
                        task.count = 3;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_big_selected);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        noIconBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        carRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (task.count == 4)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        carRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        carRow.setBackgroundColor(0x00000000);
                        task.count = 4;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_big_selected);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        noIconBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        gamesRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (task.count == 5)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        gamesRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        gamesRow.setBackgroundColor(0x00000000);
                        task.count = 5;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_big_selected);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        noIconBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        booksRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (task.count == 6)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        booksRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        booksRow.setBackgroundColor(0x00000000);
                        task.count = 6;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_big_selected);
                        noIconBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        noIconRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (task.count == 0)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        noIconRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        noIconRow.setBackgroundColor(0x00000000);
                        task.count = 0;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        noIconBorder.setBackgroundResource(R.drawable.border_big_selected);
                        break;
                }
                return true;
            }
        });

        // Setting selected element
        switch (task.count) {
            case 0:
                noIconBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 1:
                videoBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 2:
                audioBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 3:
                moneyBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 4:
                carBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 5:
                gamesBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 6:
                booksBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
        }

        // Showing
        selectIconDialog.show();
    }

    private static CustomDate getTaskIntervalFinishedTime(Context context,
                   Task task, Task.STATUS initialStatus) {
        CustomDate intervalFinishedTime = new CustomDate();
        boolean initialPositive = false;
        if (initialStatus == Task.STATUS.DONE ||
            initialStatus == Task.STATUS.IN_PROCESS ||
            initialStatus == Task.STATUS.NOT_DONE)
            initialPositive = true;
        boolean afterChangePositive = false;
        if (task.status == Task.STATUS.DONE ||
            task.status == Task.STATUS.IN_PROCESS ||
            task.status == Task.STATUS.NOT_DONE)
            afterChangePositive = true;

        if (initialPositive && afterChangePositive) {
            intervalFinishedTime.year = task.intervalFinishedTime.year;
            intervalFinishedTime.month = task.intervalFinishedTime.month;
            intervalFinishedTime.day = task.intervalFinishedTime.day;
        }
        else if (!afterChangePositive) {
            intervalFinishedTime.year = 0;
            intervalFinishedTime.month = 0;
            intervalFinishedTime.day = 0;
        }
        else if (!initialPositive && afterChangePositive) {
            DayValues dayValues = new DayValues(context);
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
        }

        return intervalFinishedTime;
    }

}