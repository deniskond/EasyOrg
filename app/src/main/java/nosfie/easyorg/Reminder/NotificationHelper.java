package nosfie.easyorg.Reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nosfie.easyorg.DataStructures.Task;

import static android.content.Context.ALARM_SERVICE;
import static nosfie.easyorg.Database.Queries.getAllTasksFromDB;

/**
 * Created by Nosf on 30.08.2017.
 */

public class NotificationHelper {

    public static void createNotification(Context context, Task task) {
        Intent intent = new Intent(context, Receiver.class);
        intent.setAction(Integer.toString(task.id));
        //Log.d("qq", "Content: " + task.customStartTime.toString().replace("-", ":") + " " + task.name);
        intent.putExtra("Title", "Напоминание о задаче");
        intent.putExtra("Content", task.customStartTime.toString().replace("-", ":") + " " + task.name);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        long timeDiff = getTimeDiffMillis(context, task);
        if (timeDiff > 0)
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeDiff, pIntent);
    }

    public static void deleteNotification(Context context, Task task) {
        Intent intent = new Intent(context, Receiver.class);
        intent.setAction(Integer.toString(task.id));
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pIntent);
    }

    public static void updateNotification(Context context, Task task) {
        Intent intent = new Intent(context, Receiver.class);
        intent.setAction(Integer.toString(task.id));
        intent.putExtra("Title", "EasyOrg - Напоминание");
        intent.putExtra("Content", task.customStartTime.toString().replace("-", ":") + " " + task.name);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        long timeDiff = getTimeDiffMillis(context, task);
        if (timeDiff > 0)
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeDiff, pIntent);
    }

    public static void updateAllNotifications(Context context) {
        ArrayList<Task> tasks = getAllTasksFromDB(context);
        for (Task task: tasks)
            updateNotification(context, task);
    }

    private static long getTimeDiffMillis(Context context, Task task) {
        Calendar taskCalendar = new GregorianCalendar(
                task.customStartDate.year,
                task.customStartDate.month - 1,
                task.customStartDate.day,
                task.customStartTime.hours,
                task.customStartTime.minutes);
        long taskTime = taskCalendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();
        long reminderBias = 0;
        switch (task.reminderTime) {
            case EXACT:
                reminderBias = 0;
                break;
            case FIVE_MINS:
                reminderBias = 5 * 60 * 1000;
                break;
            case TEN_MINS:
                reminderBias = 10 * 60 * 1000;
                break;
            case THIRTY_MINS:
                reminderBias = 30 * 60 * 1000;
                break;
            case ONE_HOUR:
                reminderBias = 60 * 60 * 1000;
                break;
        }

        Log.d("qq", "Alarm in " + Long.toString((taskTime - reminderBias - currentTime) / 1000));
        return taskTime - reminderBias - currentTime;
    }

}