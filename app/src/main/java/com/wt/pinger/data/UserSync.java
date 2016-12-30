package com.wt.pinger.data;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

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

    public static UserSync get(@NonNull Context ctx) {
        if (singleton == null) {
            UserSync res = new UserSync(ctx);
            synchronized (UserSync.class) {
                singleton = res;
            }
        }
        return singleton;
    }

    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private Prefs prefs;

    private UserSync(Context ctx) {
        new AsyncTask<Context, Void, Prefs>(){
            @Override
            protected Prefs doInBackground(Context... params) {
                return Prefs.get(params[0]);
            }

            @Override
            protected void onPostExecute(Prefs p) {
                prefs = p;
            }
        }.executeOnExecutor(exec, ctx.getApplicationContext());
    }

    public void saveUser() {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                String uuid = null;
                String referrer = null;
                if (!prefs.load(Constants.PREF_UUID_SAVED, false)) {
                    uuid = prefs.load(Constants.PREF_UUID, (String) null);
                }
                if (!prefs.load(Constants.PREF_REFERRER_SAVED, false)) {
                    referrer = prefs.load(Constants.PREF_REFERRER, (String) null);
                }
                //if () {
                //    Api.getInstance().saveNewUser(NewUser.init())
                //}
                return null;
            }
        }.executeOnExecutor(exec);
    }

}
