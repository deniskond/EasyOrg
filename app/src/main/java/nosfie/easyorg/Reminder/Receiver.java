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

import nosfie.easyorg.R;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(ctx)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(intent.getExtras().getString("Title"))
                .setContentText(intent.getExtras().getString("Content"))
                .setSmallIcon(R.drawable.icon256)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(false)
                .setColor(0xff0000ff)
                .setLights(0xff0000ff, 500, 500)
                .setOnlyAlertOnce(false)
                .build();
        nm.notify(1, notification);
    }
}
