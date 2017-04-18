package nosfie.easyorg.DataStructures;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import nosfie.easyorg.Constants;
import nosfie.easyorg.Database.TasksConnector;

public class Task {

    public enum STATUS {
        ACTUAL, DONE, NOT_DONE, IN_PROCESS, POSTPONED
    }

    public enum TYPE {
        SIMPLE, SHOPPING_LIST, COUNTABLE
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

    public class Daytime {
        public int hours;
        public int minutes;
        Daytime() {
            this.hours = 0;
            this.minutes = 0;
        }
    }

    public class customDate {
        public int day;
        public int month;
        public int year;
        customDate() {
            this.day = 0;
            this.month = 0;
            this.year = 0;
        }
    }

    public int id;
    public String name;
    public int count;
    public boolean needReminder;
    public customDate customStartDate = new customDate(), customEndDate = new customDate();
    public TYPE type;
    public START_DATE startDate;
    public START_TIME startTime;
    public DEADLINE deadline;
    public STATUS status;
    public Daytime customStartTime = new Daytime();
    public ArrayList<String> shoppingList = new ArrayList<>();

    public Task() {
        this.startDate = START_DATE.TODAY;
        this.startTime = START_TIME.NONE;
        this.deadline = DEADLINE.DAY;
        this.customStartDate = new customDate();
        this.customStartTime = new Daytime();
        this.needReminder = false;
        this.customEndDate = new customDate();
    }

    public Task(int id, String name, String type, String startDate, String startTime,
                int count, int reminder, String endDate, String shoppingList, String taskStatus) {
        this.id = id;
        this.name = name;
        this.type = TYPE.valueOf(type);

        this.startDate = START_DATE.CUSTOM;
        String[] startDateSplit = startDate.split("\\.");
        this.customStartDate = new customDate();
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
        if (reminder == 0)
            this.needReminder = false;
        else
            this.needReminder = true;

        if (endDate.equals("0000.00.00")) {
            this.deadline = DEADLINE.NONE;
            this.customEndDate = new customDate();
        } else {
            this.deadline = DEADLINE.CUSTOM;
            this.customEndDate = new customDate();
            String[] endDateSplit = endDate.split("\\.");
            this.customEndDate.year = Integer.parseInt(endDateSplit[0]);
            this.customEndDate.month = Integer.parseInt(endDateSplit[1]);
            this.customEndDate.day = Integer.parseInt(endDateSplit[2]);
        }

        String[] shoppingListSplit = shoppingList.split("\\|");
        for (String item: shoppingListSplit)
            this.shoppingList.add(item);

        this.status = STATUS.valueOf(taskStatus);
    }

    public Task(Bundle info) {
        // First screen
        this.name = info.getString("taskName");
        this.type = TYPE.valueOf(info.getString("taskType"));
        this.count = info.getInt("taskCount");
        // Shopping list screen
        this.shoppingList = info.getStringArrayList("shoppingList");
        // Second screen
        this.startDate = START_DATE.valueOf(info.getString("startDate"));
        this.customStartDate.day = info.getInt("startDay");
        this.customStartDate.month = info.getInt("startMonth");
        this.customStartDate.year = info.getInt("startYear");
        this.startTime = START_TIME.valueOf(info.getString("startTime"));
        this.customStartTime.hours = info.getInt("startHours");
        this.customStartTime.minutes = info.getInt("startMinutes");
        this.needReminder = info.getBoolean("needReminder");
        // Third screen
        this.deadline = DEADLINE.valueOf(info.getString("deadline"));
        this.customEndDate.day = info.getInt("endDay");
        this.customEndDate.month = info.getInt("endMonth");
        this.customEndDate.year = info.getInt("endYear");
        // Default value
        this.status = STATUS.ACTUAL;
    }

    public Intent formIntent(Intent intent, Task task) {
        // First screen
        intent.putExtra("taskName", task.name);
        intent.putExtra("taskType", task.type.toString());
        intent.putExtra("taskCount", task.count);
        // Shopping list screen
        intent.putExtra("shoppingList", task.shoppingList);
        // Second screen
        intent.putExtra("startDate", task.startDate.toString());
        intent.putExtra("startDay", task.customStartDate.day);
        intent.putExtra("startMonth", task.customStartDate.month);
        intent.putExtra("startYear", task.customStartDate.year);
        intent.putExtra("startTime", task.startTime.toString());
        intent.putExtra("startHours", task.customStartTime.hours);
        intent.putExtra("startMinutes", task.customStartTime.minutes);
        intent.putExtra("needReminder", task.needReminder);
        // Third screen
        intent.putExtra("deadline", task.deadline.toString());
        intent.putExtra("endDay", task.customEndDate.day);
        intent.putExtra("endMonth", task.customEndDate.month);
        intent.putExtra("endYear", task.customEndDate.year);
        return intent;
    }

    public void insertIntoDatabase(Context context) {
        TasksConnector tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        SQLiteDatabase DB = tasksConnector.getWritableDatabase();
        DB.execSQL(tasksConnector.CREATE_TABLE);
        ContentValues CV = getContentValues();
        DB.insert("tasks", null, CV);
        DB.close();
    }

    public void synchronize(Context context) {
        TasksConnector tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        SQLiteDatabase DB = tasksConnector.getWritableDatabase();
        ContentValues CV = getContentValues();
        DB.update("tasks", CV, "_id = ?", new String[] { Integer.toString(this.id) });
        DB.close();
    }

    public void delete(Context context) {
        TasksConnector tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        SQLiteDatabase DB = tasksConnector.getWritableDatabase();
        DB.delete("tasks", "_id = " + this.id, null);
        DB.close();
    }

    public ContentValues getContentValues() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        ContentValues CV = new ContentValues();
        CV.put("name", this.name);
        CV.put("type", this.type.toString());

        switch (this.startDate) {
            case TODAY:
                this.customStartDate.year = calendar.get(Calendar.YEAR);
                this.customStartDate.month = calendar.get(Calendar.MONTH) + 1;
                this.customStartDate.day = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case TOMORROW:
                calendar.setTime(date);
                calendar.add(Calendar.DATE, 1);
                this.customStartDate.year = calendar.get(Calendar.YEAR);
                this.customStartDate.month = calendar.get(Calendar.MONTH) + 1;
                this.customStartDate.day = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case CUSTOM:
                break;
        }

        CV.put("startDate", String.format("%04d", this.customStartDate.year) + "." +
                String.format("%02d", this.customStartDate.month) + "." +
                String.format("%02d", this.customStartDate.day));

        switch (this.startTime) {
            case NONE:
                CV.put("startTime", "none");
                break;
            case CUSTOM:
                CV.put("startTime",
                        String.format("%02d", this.customStartTime.hours) + "-" +
                                String.format("%02d", this.customStartTime.minutes));
                break;
        }

        CV.put("count", this.count);
        if (this.needReminder)
            CV.put("reminder", 1);
        else
            CV.put("reminder", 0);

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

        CV.put("endDate",
                String.format("%04d", this.customEndDate.year) + "." +
                        String.format("%02d", this.customEndDate.month) + "." +
                        String.format("%02d", this.customEndDate.day));

        String strShoppingList = "";
        for (int i = 0; i < this.shoppingList.size(); i++) {
            String item = this.shoppingList.get(i).replaceAll("|", "");
            if (i != this.shoppingList.size() - 1)
                strShoppingList += item + "|";
            else
                strShoppingList += item;
        }
        CV.put("shoppingList", strShoppingList);
        CV.put("status", this.status.toString());
        return CV;
    }

}