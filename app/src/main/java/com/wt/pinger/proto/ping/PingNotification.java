package com.wt.pinger.proto.ping;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.wt.pinger.R;
import com.wt.pinger.activity.PingActivity;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.service.PingWorkerService;

public class PingNotification {

    private static final String NOTIFICATION_CHANNEL = "pinger_channel";

    private Service mService;
    private AddressItem mAddressItem;
    private Intent stopIntent, contentIntent;
    private NotificationManager nm;
    private String contentText = "";

    public PingNotification(@NonNull Service s, @NonNull AddressItem a) {
        mService = s;
        mAddressItem = a;
        nm = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm != null) {
                NotificationChannel channel = nm.getNotificationChannel(NOTIFICATION_CHANNEL);
                if (channel == null) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL,
                            "Pinger",
                            NotificationManager.IMPORTANCE_LOW
                    );
                    notificationChannel.setDescription("Pinger");
                    notificationChannel.enableLights(false);
                    notificationChannel.enableVibration(false);
                    notificationChannel.setShowBadge(false);

                    nm.createNotificationChannel(notificationChannel);
                }
            }
        }

        stopIntent = new Intent(s, PingWorkerService.class);
        stopIntent.setPackage(s.getPackageName());
        stopIntent.setAction(PingWorkerService.ACTION_STOP_PING);

        contentIntent = new Intent(mService, PingActivity.class);

        mAddressItem.saveToIntent(contentIntent);
        mAddressItem.saveToIntent(stopIntent);

        mService.startForeground(mAddressItem._id.intValue(), getNotification());
    }

    public void updateNotification(String text) {
        contentText = text;
        nm.notify(mAddressItem._id.intValue(), getNotification());
    }

    private Notification getNotification() {
        String name = TextUtils.isEmpty(mAddressItem.display_name) ? mAddressItem.addres : mAddressItem.display_name;
        return new NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL)
            .setChannelId(NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle(name)
            .setContentText(contentText)
            .addAction(
                    R.drawable.ic_close_x,
                    mService.getString(R.string.label_stop),
                    PendingIntent.getService(mService, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            )
            .extend(new NotificationCompat.WearableExtender().addAction(
                    new NotificationCompat.Action.Builder(
                            R.drawable.ic_close_x,
                            mService.getString(R.string.label_stop),
                            PendingIntent.getService(mService, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    ).build()
            ))
            .setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowCancelButton(true)
                    .setShowActionsInCompactView(0)
                    .setCancelButtonIntent(PendingIntent.getService(mService, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            )
            .setColor(0xFF3F51B5)
            .setColorized(true)
            .setDeleteIntent(PendingIntent.getService(mService, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            .setContentIntent(PendingIntent.getActivity(mService, 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            .build();
    }

}
