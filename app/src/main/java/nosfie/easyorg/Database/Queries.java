package nosfie.easyorg.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import nosfie.easyorg.Constants;
import nosfie.easyorg.DataStructures.Daytime;
import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;

/**
 * Created by Nosf on 22.08.2017.
 */

public class Queries {

    final static String columns[] = {"_id", "name", "type", "startDate", "startTime", "count",
            "reminder", "endDate", "shoppingList", "status", "currentCount", "shoppingListState",
            "reminderTime"};
    static TasksConnector tasksConnector;
    static SQLiteDatabase DB;

    public static ArrayList<Task> getNotesFromDB(Context context) {
        tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        DB = tasksConnector.getReadableDatabase();
        Cursor cursor = DB.query("tasks", columns, "type = ?", new String[] { "NOTE" },
                null, null, "endDate ASC, startDate DESC");
        return getTasksByQuery(cursor);
    }

    public static ArrayList<Task> getTasksForTaskListFromDB(Context context, Timespan timespan) {
        Cursor cursor;
        tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        DB = tasksConnector.getReadableDatabase();
        if (timespan == Timespan.TODAY)
            cursor = DB.query("tasks", columns, "type != ? AND type != ?", new String[] { "TEMPLATE", "NOTE" },
                    null, null, "startTime");
        else
            cursor = DB.query("tasks", columns, "type != ? AND type != ?", new String[] { "TEMPLATE", "NOTE" },
                    null, null, "endDate ASC, startDate DESC");
        ArrayList<Task> tasks = getTasksByQuery(cursor);
        return orderTasksWithDayMargin(context, tasks);
    }

    public static ArrayList<Task> getDayTasksFromDB(Context context, String todayStr) {
        tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        DB = tasksConnector.getReadableDatabase();
        Cursor cursor = DB.query("tasks", columns, "endDate = '" + todayStr + "' AND type != ? AND type != ?",
                new String[] { "TEMPLATE", "NOTE" }, null, null, "startTime");
        ArrayList<Task> tasks = getTasksByQuery(cursor);
        return orderTasksWithDayMargin(context, tasks);
    }

    public static ArrayList<Task> getCalendarTasksFromDB(Context context, String firstDayOfMonth,
                                                         String lastDayOfMonth) {
        tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        DB = tasksConnector.getReadableDatabase();
        Cursor cursor = DB.query("tasks", columns,
                "endDate >= '" + firstDayOfMonth + "' AND endDate <= '" + lastDayOfMonth + "' AND " +
                        "type != ? AND type != ?", new String[] { "TEMPLATE", "NOTE" }, null, null, "startTime");
        return getTasksByQuery(cursor);
    }

    public static ArrayList<Task> getAllTasksFromDB(Context context) {
        tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        DB = tasksConnector.getReadableDatabase();
        Cursor cursor = DB.query("tasks", columns, null, null, null, null, "status ASC, endDate");
        ArrayList<Task> tasks = getTasksByQuery(cursor);
        return orderTasksWithDayMargin(context, tasks);
    }

    public static ArrayList<Task> getAllTemplatesFromDB(Context context) {
        tasksConnector = new TasksConnector(context, Constants.DB_NAME, null, 1);
        DB = tasksConnector.getReadableDatabase();
        Cursor cursor = DB.query("tasks", columns, "type=?", new String[] { "TEMPLATE" }, null, null, null);
        return getTasksByQuery(cursor);
    }

    private static ArrayList<Task> getTasksByQuery(Cursor cursor) {
        ArrayList<Task> tasks = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.moveToFirst())
                do {
                    tasks.add(getTaskByCursor(cursor));
                }
                while
                    (cursor.moveToNext());
        }
        DB.close();
        return tasks;
    }

    private static Task getTaskByCursor(Cursor cursor) {
        return new Task(
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
                cursor.getString(11),
                cursor.getString(12)
        );
    }

    private static ArrayList<Task> orderTasksWithDayMargin(Context context, ArrayList<Task> tasks) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String[] timeSplit = preferences.getString("dayMargin", "").split(":");
        Daytime dayMargin = new Daytime(Integer.parseInt(timeSplit[0]), Integer.parseInt(timeSplit[1]));
        ArrayList<Task> orderedTasks = new ArrayList<>();

        // Adding tasks after dayMargin
        for (Task task: tasks)
            if (task.startTime == Task.START_TIME.CUSTOM) {
                if (task.customStartTime.hours > dayMargin.hours ||
                    (task.customStartTime.hours == dayMargin.hours &&
                    task.customStartTime.minutes >= dayMargin.minutes)) {
                        orderedTasks.add(task);
                }
            }
        // Adding tasks before dayMargin
        for (Task task: tasks)
            if (task.startTime == Task.START_TIME.CUSTOM) {
                if (task.customStartTime.hours < dayMargin.hours ||
                        (task.customStartTime.hours == dayMargin.hours &&
                                task.customStartTime.minutes < dayMargin.minutes)) {
                        orderedTasks.add(task);
                }
            }
        // Adding all other tasks with unset startTime
        for (Task task: tasks)
            if (task.startTime != Task.START_TIME.CUSTOM) {
                orderedTasks.add(task);
            }

        return orderedTasks;
    }

}