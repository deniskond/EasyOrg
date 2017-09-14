package nosfie.easyorg.Reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import nosfie.easyorg.R;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri path = Uri.parse("android.resource://nosfie.easyorg/" + R.raw.alarm_tone);
        Notification notification = new NotificationCompat.Builder(ctx)
                .setContentTitle(intent.getExtras().getString("Title"))
                .setContentText(intent.getExtras().getString("Content"))
                .setSmallIcon(R.drawable.icon256)
                .setAutoCancel(false)
                .setColor(0xff0000ff)
                .setLights(0xff0000ff, 100, 100)
                .setOnlyAlertOnce(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(path)
                .build();
        //notification.flags = Notification.FLAG_INSISTENT;
        nm.notify(1, notification);
    }
}
