package nosfie.easyorg.NewTask;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;

public class Task {

    enum TYPE {
        SIMPLE, SHOPPING_LIST, COUNTABLE
    }

    String name;
    int count, hours, minutes;
    boolean needReminder;
    Date startDate, endDate;
    TYPE type;

    ArrayList<String> shoppingList = new ArrayList<>();

    public Task() {

    }

    public Task(Bundle info) {
        this.name = info.getString("taskName");
        this.count = info.getInt("taskCount");
        String taskTypeString = info.getString("taskType");
        switch (taskTypeString) {
            case "SIMPLE":
                this.type = Task.TYPE.SIMPLE;
                break;
            case "SHOPPING_LIST":
                this.type = Task.TYPE.SHOPPING_LIST;
                break;
            case "COUNTABLE":
                this.type = Task.TYPE.COUNTABLE;
                break;
        }
    }

    public Intent formIntent(Intent intent, Task task) {
        intent.putExtra("taskName", task.name);
        intent.putExtra("taskType", task.type.toString());
        intent.putExtra("taskCount", task.count);
        return intent;
    }

}


