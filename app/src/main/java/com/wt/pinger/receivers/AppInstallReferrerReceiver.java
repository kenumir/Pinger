package com.wt.pinger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.data.UserSync;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.utils.Prefs;

/**
 * Created by Kenumir on 2016-12-29.
 *
 */

public class AppInstallReferrerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String referrer = intent.getStringExtra("referrer");
        if (referrer != null && referrer.length() > 0) {
            Prefs prefs = Prefs.get(context);
            prefs.save(Constants.PREF_REFERRER, referrer);
            String uuid = prefs.load(Constants.PREF_UUID);
            if (uuid != null) {
                if (BuildConfig.DEBUG) {
                    Console.logi("AppInstallReferrerReceiver: uuid=" + uuid + ", referrer=" + referrer);
                }
                UserSync.get().saveUser(context);
            } else {
                if (BuildConfig.DEBUG) {
                    Console.logi("AppInstallReferrerReceiver: no UUID, skip sync");
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Console.logi("AppInstallReferrerReceiver: INSTALL_REFERRER, referrer=" + referrer);
        }
    }

}