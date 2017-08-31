package nosfie.easyorg.Reminder;

/**
 * Created by Nosf on 30.08.2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import nosfie.easyorg.R;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(ctx)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(intent.getExtras().getString("Title"))
                .setContentText(intent.getExtras().getString("Content"))
                .setSmallIcon(R.drawable.icon256)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(false)
                .setColor(0xff0000ff)
                .setLights(0xff0000ff, 100, 100)
                .setOnlyAlertOnce(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        nm.notify(1, notification);
    }
}
