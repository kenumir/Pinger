package com.wt.pinger.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.wt.pinger.R;
import com.wt.pinger.activity.PingActivity;
import com.wt.pinger.proto.ItemProto;
import com.wt.pinger.providers.PingContentProvider;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.providers.data.PingItem;
import com.wt.pinger.utils.BusProvider;
import com.wt.pinger.utils.DateTime;
import com.wt.pinger.utils.PingProgram;

public class PingService extends Service {

    private static final String ACTION_START_STOP = "com.wt.pinger.PING_START_STOP";
    private static final String ACTION_CHECK = "com.wt.pinger.CHECK_SERVICE";

    private static final int NOTIFICATION_ID = 112;

    public static void startStop(final @NonNull Context ctx, @NonNull AddressItem a) {
        final Intent it = new Intent(ctx, PingService.class);
        it.setAction(ACTION_START_STOP);
        a.saveToIntent(it);
        ctx.startService(it);
    }

    public static void check(final @NonNull Context ctx) {
        final Intent it = new Intent(ctx, PingService.class);
        it.setAction(ACTION_CHECK);
        ctx.startService(it);
    }

    public static final int SERVICE_STATE_IDLE = 1;
    public static final int SERVICE_STATE_WORKING = 2;

    private PingProgram mPingProgram;
    private NotificationCompat.Builder mBuilder;
    private AddressItem mPingItem;

    public PingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent stopIntent = new Intent(this, PingService.class);
        stopIntent.setPackage(getPackageName());
        stopIntent.setAction(ACTION_START_STOP);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("");
        mBuilder.addAction(
                R.drawable.ic_close_x,
                getString(R.string.label_stop),
                PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        );
        mBuilder.extend(new NotificationCompat.WearableExtender().addAction(
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_close_x,
                        getString(R.string.label_stop),
                        PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                ).build()
        ));
        mBuilder.setDeleteIntent(PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            switch(intent.getAction()) {
                case ACTION_START_STOP:
                    if (mPingProgram == null) {
                        mPingItem = ItemProto.fromIntent(intent, AddressItem.class);
                        if (mPingItem != null) {
                            mPingProgram = new PingProgram.Builder()
                                    .listener(new PingProgram.OnPingListener() {
                                        @Override
                                        public void onStart() {
                                            BusProvider.getInstance().post(SERVICE_STATE_WORKING);
                                            getContentResolver().delete(PingContentProvider.URI_CONTENT, null, new String[]{mPingItem._id.toString()});

                                            PingItem data = new PingItem();
                                            data.addressId = mPingItem._id;
                                            data.info = getResources().getString(R.string.ping_info_start, mPingItem.addres);
                                            data.timestamp = System.currentTimeMillis();
                                            getContentResolver().insert(PingContentProvider.URI_CONTENT, data.toContentValues(true));

                                            Intent it = new Intent(PingService.this, PingActivity.class);
                                            mPingItem.saveToIntent(it);
                                            mBuilder.setContentIntent(PendingIntent.getActivity(PingService.this, 1, it, PendingIntent.FLAG_UPDATE_CURRENT));
                                            startForeground(NOTIFICATION_ID, mBuilder.build());
                                        }

                                        @Override
                                        public void onResult(@Nullable PingItem data) {
                                            if (data != null) {
                                                data.addressId = mPingItem != null ? mPingItem._id : null;
                                                getContentResolver().insert(PingContentProvider.URI_CONTENT, data.toContentValues(true));
                                                if (data.isDataValid()) {
                                                    mBuilder.setContentText(
                                                            DateTime.formatTime(PingService.this, data.timestamp) +
                                                                    " - " + data.seq + " - " + data.time + "ms"
                                                    );
                                                } else {
                                                    mBuilder.setContentText(data.info);
                                                }
                                                startForeground(NOTIFICATION_ID, mBuilder.build());
                                            }
                                        }

                                        @Override
                                        public void onError(String er) {
                                            if (er != null) {
                                                PingItem data = new PingItem();
                                                data.info = er;
                                                data.timestamp = System.currentTimeMillis();
                                                data.addressId = mPingItem != null ? mPingItem._id : null;
                                                getContentResolver().insert(PingContentProvider.URI_CONTENT, data.toContentValues(true));
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            mPingProgram = null;
                                            PingItem data = new PingItem();
                                            data.addressId = mPingItem._id;
                                            data.info = getResources().getString(R.string.ping_info_finish);
                                            data.timestamp = System.currentTimeMillis();
                                            getContentResolver().insert(PingContentProvider.URI_CONTENT, data.toContentValues(true));
                                            BusProvider.getInstance().post(SERVICE_STATE_IDLE);
                                            stopForeground(true);
                                        }
                                    })
                                    .count(mPingItem.pings != null ? mPingItem.pings : 0)
                                    .packetSize(mPingItem.packet != null ? mPingItem.packet : 0)
                                    .address(mPingItem.addres)
                                    .build();
                            mPingProgram.start();
                        }
                    } else {
                        mPingProgram.terminate();
                        mPingProgram = null;
                    }
                    break;

                case ACTION_CHECK:
                    BusProvider.getInstance().post(mPingProgram != null ? SERVICE_STATE_WORKING : SERVICE_STATE_IDLE);
                    break;
            }

        }

        return START_NOT_STICKY;
    }
}
