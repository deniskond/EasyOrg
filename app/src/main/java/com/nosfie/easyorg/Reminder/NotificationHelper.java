package com.nosfie.easyorg.Reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.nosfie.easyorg.DataStructures.Task;
import com.nosfie.easyorg.R;

import static android.content.Context.ALARM_SERVICE;

public class NotificationHelper {

    public static void createNotification(Context context, Task task) {
        Intent intent = new Intent(context, Receiver.class);
        intent.setAction(Integer.toString(task.id));
        intent.putExtra("Title", context.getResources().getString(R.string.notification_title));
        intent.putExtra("Content", task.customStartTime.toString().replace("-", ":") + " " + task.name);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 60 * 1000, pIntent);
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
        intent.putExtra("Title", context.getResources().getString(R.string.notification_title));
        intent.putExtra("Content", task.customStartTime.toString().replace("-", ":") + " " + task.name);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        long timeDiff = getTimeDiffMillis(context, task);
        if (timeDiff > 0)
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeDiff, pIntent);
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
        return taskTime - reminderBias - currentTime;
    }

}