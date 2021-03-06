package com.nosfie.easyorg.DataStructures;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.nosfie.easyorg.Constants;
import com.nosfie.easyorg.Database.TasksConnector;

import static com.nosfie.easyorg.Reminder.NotificationHelper.createNotification;
import static com.nosfie.easyorg.Reminder.NotificationHelper.deleteNotification;
import static com.nosfie.easyorg.Reminder.NotificationHelper.updateNotification;

public class Task {

    public enum STATUS {
        ACTUAL, DONE, NOT_DONE, IN_PROCESS, POSTPONED
    }

    public enum TYPE {
        SIMPLE, SHOPPING_LIST, COUNTABLE, TEMPLATE, NOTE
    }

    public enum START_DATE {
        TODAY, TOMORROW, CUSTOM
    }

    public enum START_TIME {
        NONE, CUSTOM
    }

    public enum DEADLINE {
        DAY, WEEK, MONTH, YEAR, NONE, CUSTOM
    }

    public enum REMINDER_TIME {
        EXACT, FIVE_MINS, TEN_MINS, THIRTY_MINS, ONE_HOUR
    }

    public int id;
    public String name;
    public String text;
    public int count, currentCount;
    public boolean needReminder;
    public CustomDate
            customStartDate = new CustomDate(),
            customEndDate = new CustomDate(),
            intervalFinishedTime = new CustomDate();
    public Timespan finishedTimespan = Timespan.TODAY;
    public TYPE type;
    public START_DATE startDate;
    public START_TIME startTime;
    public DEADLINE deadline;
    public STATUS status;
    public Daytime customStartTime = new Daytime();
    public ArrayList<String> shoppingList = new ArrayList<>();
    public ArrayList<Integer> shoppingListState = new ArrayList<>();
    public boolean predefinedShoppingList = false,
                   usePredefinedTimespan = false,
                   usePredefinedDate = false;
    public REMINDER_TIME reminderTime;

    private void fillDefaultParameters() {
        if (this.startDate == null) this.startDate = START_DATE.TODAY;
        if (this.startTime == null) this.startTime = START_TIME.NONE;
        if (this.deadline == null) this.deadline = DEADLINE.DAY;
        if (this.customStartDate == null) this.customStartDate = new CustomDate();
        if (this.customStartTime == null) this.customStartTime = new Daytime();
        this.needReminder = false;
        if (this.customEndDate == null) this.customEndDate = new CustomDate();
        if (this.status == null) this.status = STATUS.ACTUAL;
        if (this.type == null) this.type = TYPE.SIMPLE;
        if (this.shoppingList == null) this.shoppingList = new ArrayList<>();
        if (this.shoppingListState == null) this.shoppingListState = new ArrayList<>();
        if (this.reminderTime == null) this.reminderTime = REMINDER_TIME.EXACT;
    }

    public Task() {
        fillDefaultParameters();
    }

    public Task(int id, String name, String type, String startDate, String startTime,
                int count, int reminder, String endDate, String shoppingList, String taskStatus,
                int currentCount, String shoppingListState, String reminderTime, String intervalFinishedTime) {
        this.id = id;
        this.name = name;
        this.type = TYPE.valueOf(type);

        this.startDate = START_DATE.CUSTOM;
        String[] startDateSplit = startDate.split("\\.");
        this.customStartDate = new CustomDate();
        this.customStartDate.year = Integer.parseInt(startDateSplit[0]);
        this.customStartDate.month = Integer.parseInt(startDateSplit[1]);
        this.customStartDate.day = Integer.parseInt(startDateSplit[2]);
        if (startTime.equals("none")) {
            this.startTime = START_TIME.NONE;
            this.customStartTime = new Daytime();
        } else {
            this.startTime = START_TIME.CUSTOM;
            this.customStartTime = new Daytime();
            String[] startTimeSplit = startTime.split("\\-");
            this.customStartTime.hours = Integer.parseInt(startTimeSplit[0]);
            this.customStartTime.minutes = Integer.parseInt(startTimeSplit[1]);
        }

        this.count = count;
        this.currentCount = currentCount;
        this.needReminder = reminder != 0;
        if (reminderTime != null)
            this.reminderTime = REMINDER_TIME.valueOf(reminderTime);
        else
            this.reminderTime = REMINDER_TIME.EXACT;
        if (endDate.equals("0000.00.00")) {
            this.deadline = DEADLINE.NONE;
            this.customEndDate = new CustomDate();
        } else {
            this.deadline = DEADLINE.CUSTOM;
            this.customEndDate = new CustomDate();
            String[] endDateSplit = endDate.split("\\.");
            this.customEndDate.year = Integer.parseInt(endDateSplit[0]);
            this.customEndDate.month = Integer.parseInt(endDateSplit[1]);
            this.customEndDate.day = Integer.parseInt(endDateSplit[2]);
        }

        String[] shoppingListSplit = shoppingList.split("\\|");
        for (String item: shoppingListSplit)
            this.shoppingList.add(item);

        this.status = STATUS.valueOf(taskStatus);

        if (shoppingListState != null)
            for (int pos = 0; pos < shoppingListState.length(); pos++)
                this.shoppingListState.add(Integer.parseInt(shoppingListState.substring(pos, pos + 1)));

        this.text = shoppingList;

        if (intervalFinishedTime == null || intervalFinishedTime.equals(""))
            intervalFinishedTime = "0000.00.00";
        String[] intervalFirstSplit = intervalFinishedTime.split("\\|");
        intervalFinishedTime = intervalFirstSplit[0];
        if (intervalFirstSplit.length == 2)
            this.finishedTimespan = Timespan.valueOf(intervalFirstSplit[1]);
        String[] intervalDateSplit = intervalFinishedTime.split("\\.");
        this.intervalFinishedTime = new CustomDate();
        this.intervalFinishedTime.year = Integer.parseInt(intervalDateSplit[0]);
        this.intervalFinishedTime.month = Integer.parseInt(intervalDateSplit[1]);
        this.intervalFinishedTime.day = Integer.parseInt(intervalDateSplit[2]);
    }

    public Task(Bundle info) {
        // First screen
        this.name = info.getString("taskName");
        String taskTypeStr = info.getString("taskType");
        if (taskTypeStr != null)
            this.type = TYPE.valueOf(taskTypeStr);
        this.count = info.getInt("taskCount");
        this.currentCount = 0;
        // Shopping list screen
        this.shoppingList = info.getStringArrayList("shoppingList");
        this.shoppingListState = info.getIntegerArrayList("shoppingListState");
        // Second screen
        String startDateStr = info.getString("startDate");
        if (startDateStr != null)
            this.startDate = START_DATE.valueOf(startDateStr );
        this.customStartDate.day = info.getInt("startDay");
        this.customStartDate.month = info.getInt("startMonth");
        this.customStartDate.year = info.getInt("startYear");
        String startTimeStr = info.getString("startTime");
        if (startTimeStr != null)
            this.startTime = START_TIME.valueOf(startTimeStr);
        this.customStartTime.hours = info.getInt("startHours");
        this.customStartTime.minutes = info.getInt("startMinutes");
        this.needReminder = info.getBoolean("needReminder");
        String deadlineStr = info.getString("deadline");
        if (deadlineStr != null)
            this.deadline = DEADLINE.valueOf(deadlineStr);
        this.customEndDate.day = info.getInt("endDay");
        this.customEndDate.month = info.getInt("endMonth");
        this.customEndDate.year = info.getInt("endYear");
        // Default value
        this.status = STATUS.ACTUAL;
        // Getting predefined task info
        this.predefinedShoppingList = info.getBoolean("predefinedShoppingList");
        if (this.predefinedShoppingList) {
            this.type = Task.TYPE.SHOPPING_LIST;
        }
        String predefinedTimespanStr = info.getString("predefinedTimespan");
        if (predefinedTimespanStr != null) {
            this.usePredefinedTimespan = true;
            this.startDate = Task.START_DATE.TODAY;
            this.deadline = DEADLINE.valueOf(predefinedTimespanStr);
        }
        String predefinedDateStr = info.getString("predefinedDate");
        if (predefinedDateStr != null) {
            usePredefinedDate = true;
            this.startDate = Task.START_DATE.CUSTOM;
            String[] predefinedDateSplit = predefinedDateStr.split("\\.");
            this.customStartDate.year = Integer.parseInt(predefinedDateSplit[0]);
            this.customStartDate.month = Integer.parseInt(predefinedDateSplit[1]);
            this.customStartDate.day = Integer.parseInt(predefinedDateSplit[2]);
            this.deadline = Task.DEADLINE.DAY;
        }
        // Filling empty fields with default values
        fillDefaultParameters();
    }

    public Intent formIntent(Intent intent, Task task) {
        // First screen
        intent.putExtra("taskName", task.name);
        intent.putExtra("taskType", task.type.toString());
        intent.putExtra("taskCount", task.count);
        // Shopping list screen
        intent.putExtra("shoppingList", task.shoppingList);
        intent.putExtra("shoppingListState", task.shoppingListState);
        // Second screen
        intent.putExtra("startDate", task.startDate.toString());
        intent.putExtra("startDay", task.customStartDate.day);
        intent.putExtra("startMonth", task.customStartDate.month);
        intent.putExtra("startYear", task.customStartDate.year);
        intent.putExtra("startTime", task.startTime.toString());
        intent.putExtra("startHours", task.customStartTime.hours);
        intent.putExtra("startMinutes", task.customStartTime.minutes);
        intent.putExtra("needReminder", task.needReminder);
        intent.putExtra("deadline", task.deadline.toString());
        intent.putExtra("endDay", task.customEndDate.day);
        intent.putExtra("endMonth", task.customEndDate.month);
        intent.putExtra("endYear", task.customEndDate.year);
        // Predefined task info
        intent.putExtra("predefinedShoppingList", this.predefinedShoppingList);
        if (this.usePredefinedTimespan)
            intent.putExtra("predefinedTimespan", this.deadline.toString());
        if (this.usePredefinedDate)
            intent.putExtra("predefinedDate", this.customStartDate.toString());
        return intent;
    }

    public void insertIntoDatabase(Context context) {
        TasksConnector tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        SQLiteDatabase DB = tasksConnector.getWritableDatabase();
        DB.execSQL(tasksConnector.CREATE_TABLE);
        ContentValues CV = getContentValues(context);
        this.id = (int)DB.insert("tasks", null, CV);
        DB.close();
        if (this.needReminder)
            createNotification(context.getApplicationContext(), this);
    }

    public void synchronize(Context context) {
        TasksConnector tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        SQLiteDatabase DB = tasksConnector.getWritableDatabase();
        ContentValues CV = getContentValues(context);
        DB.update("tasks", CV, "_id = ?", new String[] { Integer.toString(this.id) });
        DB.close();
        if (this.needReminder)
            updateNotification(context, this);
        else
            deleteNotification(context, this);
    }

    public void delete(Context context) {
        TasksConnector tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        SQLiteDatabase DB = tasksConnector.getWritableDatabase();
        DB.delete("tasks", "_id = " + this.id, null);
        DB.close();
        if (this.needReminder)
            deleteNotification(context, this);
    }

    private ContentValues getContentValues(Context context) {
        Calendar calendar = Calendar.getInstance();
        DayValues dayValues = new DayValues(context);
        ContentValues CV = new ContentValues();
        CV.put("name", this.name);
        CV.put("type", this.type.toString());

        switch (this.startDate) {
            case TODAY:
                this.customStartDate = dayValues.today;
                break;
            case TOMORROW:
                this.customStartDate = dayValues.tomorrow;
                break;
            case CUSTOM:
                break;
        }

        CV.put("startDate", this.customStartDate.toString());

        switch (this.startTime) {
            case NONE:
                CV.put("startTime", "none");
                break;
            case CUSTOM:
                CV.put("startTime", this.customStartTime.toString());
                break;
        }

        CV.put("count", this.count);
        if (this.needReminder)
            CV.put("reminder", 1);
        else
            CV.put("reminder", 0);
        CV.put("reminderTime", this.reminderTime.toString());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
        String dateInString = this.customStartDate.day
                + "-" + this.customStartDate.month
                + "-" + this.customStartDate.year;
        try {
            calendar.setTime(sdf.parse(dateInString));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (this.deadline) {
            case DAY:
                this.customEndDate.year = calendar.get(Calendar.YEAR);
                this.customEndDate.month = calendar.get(Calendar.MONTH) + 1;
                this.customEndDate.day = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case WEEK:
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int addition = 8 - dayOfWeek;
                if (addition == 7) addition = 0;
                calendar.add(Calendar.DATE, addition);
                this.customEndDate.year = calendar.get(Calendar.YEAR);
                this.customEndDate.month = calendar.get(Calendar.MONTH) + 1;
                this.customEndDate.day = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case MONTH:
                this.customEndDate.year = calendar.get(Calendar.YEAR);
                this.customEndDate.month = calendar.get(Calendar.MONTH) + 1;
                this.customEndDate.day = calendar.getActualMaximum(Calendar.DATE);
                break;
            case YEAR:
                this.customEndDate.year = calendar.get(Calendar.YEAR);
                this.customEndDate.month = 12;
                this.customEndDate.day = 31;
                break;
            case NONE:
                this.customEndDate.year = 0;
                this.customEndDate.month = 0;
                this.customEndDate.day = 0;
                break;
            case CUSTOM:
                break;
        }

        CV.put("endDate", this.customEndDate.toString());

        String strShoppingList = "";
        for (int i = 0; i < this.shoppingList.size(); i++) {
            String item = this.shoppingList.get(i).replaceAll("|", "");
            if (i != this.shoppingList.size() - 1)
                strShoppingList += item + "|";
            else
                strShoppingList += item;
        }
        if (this.type != TYPE.NOTE)
            CV.put("shoppingList", strShoppingList);
        else
            CV.put("shoppingList", this.text);
        CV.put("status", this.status.toString());
        CV.put("currentcount", this.currentCount);

        String shoppingListStateStr = "";
        for (int digit: shoppingListState)
            shoppingListStateStr += Integer.toString(digit);
        CV.put("shoppingListState", shoppingListStateStr);

        CV.put("intervalFinishedTime", this.intervalFinishedTime.toString() + "|" + finishedTimespan.toString());

        return CV;
    }

    @Override
    public String toString() {
        return  "id: " + this.id +
                "\n Start date: " + this.customStartDate +
                "\n End date: " + this.customEndDate +
                "\n Deadline: " + this.deadline +
                "\n Start time: " + this.startTime +
                "\n Need reminder: " + this.needReminder +
                "\n Name: " + this.name +
                "\n Count: " + this.count +
                "\n Current count: " + this.currentCount +
                "\n Custom start time: " + this.customStartTime +
                "\n Start date: " + this.startDate +
                "\n Status: " + this.status +
                "\n Type: " + this.type +
                "\n Reminder time: " + this.reminderTime +
                "\n Text: " + this.text;
    }

    public boolean isFinished() {
        if (this.status == STATUS.DONE ||
                this.status == STATUS.NOT_DONE ||
                this.status == STATUS.IN_PROCESS)
            return true;
        else
            return false;
    }

}