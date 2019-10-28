package com.wt.pinger.proto.ping;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.R;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.providers.PingContentProvider;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.providers.data.PingItem;
import com.wt.pinger.service.PingWorkerService;
import com.wt.pinger.utils.DateTime;
import com.wt.pinger.utils.PingProgram;
import com.wt.pinger.utils.Prefs;

public class PingWorker implements PingProgram.OnPingListener {

    private AddressItem mAddressItem;
    private PingProgram mPingProgram;
    private PingWorkerService mPingWorkerService;
    private Integer lastSequenceNum = null;
    private PingNotification mPingNotification;

    public PingWorker(@NonNull PingWorkerService service, @NonNull AddressItem a) {
        mAddressItem = a;
        mPingWorkerService = service;
        mPingNotification = new PingNotification(service, a);
        mPingProgram = new PingProgram.Builder()
                .listener(this)
                .count(mAddressItem.pings != null ? mAddressItem.pings : 0)
                .packetSize(mAddressItem.packet != null ? mAddressItem.packet : 0)
                .address(mAddressItem.addres)
                .interval(mAddressItem.interval != null ? mAddressItem.interval : 0)
                .build();
        mPingProgram.start();
    }

    @Override
    public void onStart() {
        setupWakeLocks();
        mPingWorkerService.getContentResolver().delete(
                PingContentProvider.URI_CONTENT,
                null,
                new String[]{mAddressItem._id.toString()}
        );

        PingItem data = new PingItem();
        data.addressId = mAddressItem._id; // NPE!
        data.info = mPingWorkerService.getResources().getString(R.string.ping_info_start, mAddressItem.addres);
        data.timestamp = System.currentTimeMillis();
        mPingWorkerService.getContentResolver().insert(PingContentProvider.URI_CONTENT, data.toContentValues(true));


        if (!Prefs.get(mPingWorkerService).load(Constants.PREF_MEMBER_OLD_SESSIONS, true)) {
            mPingWorkerService.getContentResolver().delete(
                    PingContentProvider.URI_CONTENT,
                    PingContentProvider.WHERE_DELETE_OLD_SESSIONS,
                    new String[]{mAddressItem._id.toString()}
            );
        }
    }

    @Override
    public void onResult(@Nullable PingItem data) {
        if (data != null) {
            if (lastSequenceNum != null && data.seq != null) {
                int diff = data.seq - lastSequenceNum;
                if (diff > 1) {
                    for(int i=lastSequenceNum + 1; i<data.seq; i++) {
                        PingItem fakeData = new PingItem();
                        fakeData.seq = i;
                        fakeData.addressId = mAddressItem._id;
                        fakeData.info = mPingWorkerService.getResources().getString(R.string.label_missing_sequence);
                        fakeData.infoType = PingItem.INFO_TYPE_ERROR;
                        fakeData.timestamp = System.currentTimeMillis();
                        mPingWorkerService.getContentResolver().insert(PingContentProvider.URI_CONTENT, fakeData.toContentValues(true));
                    }
                }
            }

            data.addressId = mAddressItem._id;
            mPingWorkerService.getContentResolver().insert(PingContentProvider.URI_CONTENT, data.toContentValues(true));
            if (data.isDataValid()) {
                mPingNotification.updateNotification(
                    DateTime.formatTime(mPingWorkerService, data.timestamp) +
                    " - " + data.seq + " - " + data.time + "ms"
                );
            } else {
                mPingNotification.updateNotification(data.info);
            }

            lastSequenceNum = data.seq;
        }
    }

    @Override
    public void onIPAddressResult(@Nullable String data) {
        PingItem d = new PingItem();
        d.addressId = mAddressItem._id;
        d.info = "IP: `" + data + "`";
        d.timestamp = System.currentTimeMillis();
        mPingWorkerService.getContentResolver().insert(PingContentProvider.URI_CONTENT, d.toContentValues(true));
    }

    @Override
    public void onError(String er) {
        if (er != null) {
            PingItem data = new PingItem();
            data.info = er;
            data.timestamp = System.currentTimeMillis();
            data.addressId = mAddressItem._id;
            mPingWorkerService.getContentResolver().insert(PingContentProvider.URI_CONTENT, data.toContentValues(true));
        }
    }

    @Override
    public void onFinish() {
        PingItem data = new PingItem();
        data.addressId = mAddressItem._id;
        data.info = mPingWorkerService.getResources().getString(R.string.ping_info_finish);
        data.timestamp = System.currentTimeMillis();
        mPingWorkerService.getContentResolver().insert(PingContentProvider.URI_CONTENT, data.toContentValues(true));
        mPingWorkerService.stopForeground(true);
        releaseWakeLocks();
    }

    public void terminate() {
        mPingProgram.terminate();
    }

    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;
    private void setupWakeLocks() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            if (BuildConfig.DEBUG) { Console.logd("WakeLock is held, no need to create next"); }
        } else {
            if (mWakeLock != null) {
                mWakeLock.release();
            }
            PowerManager pm = (PowerManager) mPingWorkerService.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wake:com.wt.pinger/PingWorker");
            mWakeLock.acquire();
            if (BuildConfig.DEBUG) {
                Console.logd("WakeLock acquire");
            }
        }
        if (mWifiLock != null && mWifiLock.isHeld()) {
            if (BuildConfig.DEBUG) { Console.logd("WifiLock is held, no need to create next"); }
        } else {
            if (mWifiLock != null) {
                mWifiLock.release();
            }
            mWifiLock = ((WifiManager) mPingWorkerService.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "wakeWiFi:com.wt.pinger/PingWorker");
            mWifiLock.acquire();
            if (BuildConfig.DEBUG) {
                Console.logd("WifiLock acquire");
            }
        }
    }
    private void releaseWakeLocks() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
            if (BuildConfig.DEBUG) { Console.logd("WakeLock release"); }
        }
        /*
         * sometimes happen: RuntimeException: WifiLock under-locked pinger
         */
        try {
            if (mWifiLock != null && mWifiLock.isHeld()) {
                mWifiLock.release();
                mWifiLock = null;
                if (BuildConfig.DEBUG) {
                    Console.logd("WifiLock release");
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
