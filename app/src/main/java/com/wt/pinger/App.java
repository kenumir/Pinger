package com.wt.pinger;

import android.app.Application;
import android.os.SystemClock;

import com.crashlytics.android.Crashlytics;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.hivedi.era.ReportInterface;
import com.wt.pinger.data.api.Api;
import com.wt.pinger.data.api.NewUser;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.utils.PingProgram;
import com.wt.pinger.utils.Prefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by Kenumir on 2016-08-11.
 *
 */
public class App extends Application {

    public interface OnAppReady {
        void onAppReady();
    }

    private OnAppReady mOnAppReady;
    private boolean appReady = false;

    public void setOnAppReady(OnAppReady e) {
        mOnAppReady = e;
    }

    public boolean isAppReady() {
        return appReady;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appReady = false;

        if (BuildConfig.DEBUG) {
            /**
             * simple log
             */
            Console.setEnabled(true);
            Console.setTag("pinger");
            Console.addLogWriterLogCat();
        } else {
            Fabric.with(this, new Crashlytics());
            Crashlytics.setLong("Build Time", BuildConfig.APP_BUILD_TIMESTAMP);
            ERA.registerAdapter(new ReportInterface() {
                @Override
                public void logException(Throwable throwable, Object... objects) {
                    Crashlytics.logException(throwable);
                }

                @Override
                public void log(String s, Object... objects) {
                    Crashlytics.log(s);
                }

                @Override
                public void breadcrumb(String s, Object... objects) {
                    // nothing
                }
            });
            new ANRWatchDog().setReportMainThreadOnly().setANRListener(new ANRWatchDog.ANRListener() {
                @Override
                public void onAppNotResponding(ANRError error) {
                    ERA.logException(error);
                }
            }).start();
        }

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                final long startTime = SystemClock.elapsedRealtime();

                Prefs prefs = Prefs.get(App.this);

                // UUID
                String uuid = prefs.load(Constants.PREF_UUID, (String) null);
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                    prefs.save(Constants.PREF_UUID, uuid);
                }

                if (!BuildConfig.DEBUG) {
                    Crashlytics.setUserIdentifier(uuid);
                }

                int valid_ping_program;

                File pingFile = new File("/system/bin/ping");
                if (pingFile.exists()) {
                    PingProgram.setPingExec("/system/bin/ping");
                    valid_ping_program = 1;
                } else {
                    try {
                        Process ping = Runtime.getRuntime().exec("ping -c 1 127.0.0.1");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(ping.getInputStream()));
                        String line;
                        int dataCounter = 0;
                        while ((line = reader.readLine()) != null) {
                            if (line.length() > 0) {
                                dataCounter++;
                            }
                        }
                        reader.close();
                        ping.destroy();
                        if (dataCounter > 1) {
                            PingProgram.setPingExec("ping");
                            valid_ping_program = 2;
                        } else {
                            PingProgram.setPingExec(null);
                            valid_ping_program = -1;
                        }
                    } catch (Exception ignore) {
                        PingProgram.setPingExec(null);
                        valid_ping_program = -2;
                    }
                }

                if (BuildConfig.BUILD_FLAG_USE_API) {
                    if (!prefs.load(Constants.PREF_UUID_SAVED, false)) {
                        if (Api.getInstance().saveNewUser(NewUser.init(uuid, SystemClock.elapsedRealtime() - startTime, App.this, valid_ping_program))) {
                            prefs.save(Constants.PREF_UUID_SAVED, true);
                        }
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                appReady = true;
                if (mOnAppReady != null) {
                    mOnAppReady.onAppReady();
                }
            }
        }.execute();
    }
}
