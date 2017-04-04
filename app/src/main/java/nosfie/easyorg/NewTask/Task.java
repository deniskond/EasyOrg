package nosfie.easyorg.NewTask;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class Task {

    enum TYPE {
        SIMPLE, SHOPPING_LIST, COUNTABLE
    }

    enum START_DATE {
        TODAY, TOMORROW, CUSTOM
    }

    enum START_TIME {
        NONE, CUSTOM
    }

    enum DEADLINE {
        TODAY, WEEK, MONTH, YEAR, NONE, CUSTOM
    }

    class Daytime {
        int hours;
        int minutes;
        Daytime() {
            this.hours = 0;
            this.minutes = 0;
        }
    }

    class Date {
        int day;
        int month;
        int year;
        Date() {
            this.day = 0;
            this.month = 0;
            this.year = 0;
        }
    }

    String name;
    int count;
    boolean needReminder;
    Date customStartDate = new Date(), customEndDate = new Date();
    TYPE type;
    START_DATE startDate;
    START_TIME startTime;
    DEADLINE deadline;
    Daytime customStartTime = new Daytime();

    ArrayList<String> shoppingList = new ArrayList<>();

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

}