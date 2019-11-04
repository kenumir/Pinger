package com.wt.pinger.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hivedi.console.Console;
import com.wt.pinger.App;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.proto.ItemProto;
import com.wt.pinger.proto.ping.PingManager;
import com.wt.pinger.providers.data.AddressItem;

public class PingWorkerService extends Service {

    public static final String ACTION_START_PING = BuildConfig.APPLICATION_ID + ".START_PING";
    public static final String ACTION_STOP_PING = BuildConfig.APPLICATION_ID + ".STOP_PING";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_START_PING.equals(action)) {
            // start ping
            AddressItem address = ItemProto.fromIntent(intent, AddressItem.class);
            if (address != null) {
                getPingManager().newWorkerInstance(address, this);
            } else {
                Console.loge("PingWorkerService: onStartCommand: ACTION_START_PING no ping address");
            }
        } else if (ACTION_STOP_PING.equals(action)) {
            AddressItem address = ItemProto.fromIntent(intent, AddressItem.class);
            if (address != null) {
                getPingManager().stopPingWorker(address._id);
            } else {
                Console.loge("PingWorkerService: onStartCommand: ACTION_STOP_PING no ping address");
            }
        }
        return START_NOT_STICKY;
    }

    private PingManager getPingManager() {
        return ((App) getApplication()).getPingManager();
    }
}
