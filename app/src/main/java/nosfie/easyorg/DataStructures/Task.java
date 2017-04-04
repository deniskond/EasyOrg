package nosfie.easyorg.DataStructures;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import java.util.ArrayList;

import nosfie.easyorg.Constants;
import nosfie.easyorg.Database.TasksConnector;

public class Task {

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
        TODAY, WEEK, MONTH, YEAR, NONE, CUSTOM
    }

    public class Daytime {
        public int hours;
        public int minutes;
        Daytime() {
            this.hours = 0;
            this.minutes = 0;
        }
    }

    public class Date {
        public int day;
        public int month;
        public int year;
        Date() {
            this.day = 0;
            this.month = 0;
            this.year = 0;
        }
    }

    public String name;
    public int count;
    public boolean needReminder;
    public Date customStartDate = new Date(), customEndDate = new Date();
    public TYPE type;
    public START_DATE startDate;
    public START_TIME startTime;
    public DEADLINE deadline;
    public Daytime customStartTime = new Daytime();
    public ArrayList<String> shoppingList = new ArrayList<>();

    public Task() {
        this.startDate = START_DATE.TODAY;
        this.startTime = START_TIME.NONE;
        this.deadline = DEADLINE.TODAY;
        this.customStartDate = new Date();
        this.customStartTime = new Daytime();
        this.needReminder = false;
        this.customEndDate = new Date();
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
        ContentValues CV = new ContentValues();
        CV.put("name", this.name);
        CV.put("type", this.type.toString());
        CV.put("startDate",
                String.format("%04d", this.customStartDate.year) + "." +
                String.format("%02d", this.customStartDate.month) + "." +
                String.format("%02d", this.customStartDate.day));
        CV.put("startTime",
                String.format("%02d", this.customStartTime.hours) + "-" +
                String.format("%02d", this.customStartTime.minutes));
        CV.put("count", this.count);
        if (this.needReminder)
            CV.put("reminder", 1);
        else
            CV.put("reminder", 0);
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
        DB.insert("tasks", null, CV);
        DB.close();
    }

}