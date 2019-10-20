package com.wt.pinger.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.data.api.Api;
import com.wt.pinger.data.api.NewUser;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.utils.Prefs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Kenumir on 2016-12-29.
 *
 */

public class UserSync {

    private volatile static UserSync singleton;

    public static UserSync get() {
        if (singleton == null) {
            UserSync res = new UserSync();
            synchronized (UserSync.class) {
                singleton = res;
            }
        }
        return singleton;
    }

    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private UserSync() {

    }

    public void saveUser(@NonNull Context c) {
        new AsyncTask<Context, Void, Void>(){
            @Override
            protected Void doInBackground(Context... params) {
                boolean andyDataToSend = false;
                Prefs prefs = Prefs.get(params[0]);
                String uuid = prefs.load(Constants.PREF_UUID, (String) null);
                if (uuid != null) {
                    String referrer = prefs.load(Constants.PREF_REFERRER, (String) null);
                    long initTime = prefs.load(Constants.PREF_FIRST_INIT_TIME, 0L);
                    if (!prefs.load(Constants.PREF_REFERRER_SAVED, false) && referrer != null) {
                        andyDataToSend = true;
                    }
                    if (!prefs.load(Constants.PREF_FIRST_INIT_TIME_SAVED, false) && initTime > 0L) {
                        andyDataToSend = true;
                    }
                    if (andyDataToSend) {
                        NewUser user = NewUser.init(params[0], uuid, initTime, referrer);
                        if (BuildConfig.DEBUG) {
                            Console.logd("Save User Info, data=" + user);
                        }
                        boolean saveSuccess = Api.getInstance().saveNewUser(user);
                        if (saveSuccess) {
                            if (!prefs.load(Constants.PREF_FIRST_INIT_TIME_SAVED, false) && initTime > 0L) {
                                prefs.save(Constants.PREF_FIRST_INIT_TIME_SAVED, true);
                            }
                            if (!prefs.load(Constants.PREF_REFERRER_SAVED, false) && referrer != null) {
                                prefs.save(Constants.PREF_REFERRER_SAVED, true);
                            }
                        }
                    }
                }
                return null;
            }
        }.executeOnExecutor(exec, c.getApplicationContext());
    }

}
