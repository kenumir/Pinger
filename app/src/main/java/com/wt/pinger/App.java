package com.wt.pinger;

import android.app.Application;
import android.os.SystemClock;

import androidx.appcompat.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.hivedi.era.ReportInterface;
import com.kenumir.eventclip.EventClip;
import com.wt.pinger.events.providers.FireBaseEventProvider;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.proto.UserTheme;
import com.wt.pinger.proto.ping.PingManager;
import com.wt.pinger.utils.PingProgram;
import com.wt.pinger.utils.Prefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by Kenumir on 2016-08-11.
 *
 */
public class App extends Application {

    private final static ExecutorService exec = Executors.newSingleThreadExecutor();
    private PingManager mPingManager;

    @Override
    public void onCreate() {
        super.onCreate();


        Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .build();
        Fabric.with(fabric);
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

        if (BuildConfig.DEBUG) {
            /*
             * simple log
             */
            Console.setEnabled(true);
            Console.setTag("pinger");
            Console.addLogWriterLogCat();
        } else {
            new ANRWatchDog().setReportMainThreadOnly().setANRListener(new ANRWatchDog.ANRListener() {
                @Override
                public void onAppNotResponding(ANRError error) {
                    ERA.logException(error);
                }
            }).start();
        }

        final Prefs prefs = Prefs.get(this);
        switch(prefs.loadTheme()) {
            case UserTheme.DEFAULT:
                // skip
                break;
            case UserTheme.FOLLOW_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case UserTheme.FOLLOW_BATTERY:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            case UserTheme.LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case UserTheme.DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }

        final long t1 = SystemClock.elapsedRealtime();
        EventClip.registerProvider(new FireBaseEventProvider(App.this));
        ERA.log("App:FireBaseEventProvider init time " + (SystemClock.elapsedRealtime() - t1) + " ms");

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                final long startTime = SystemClock.elapsedRealtime();

                ERA.log("App.AsyncTask:begin");

                ERA.log("App.AsyncTask:Prefs init");

                // UUID
                String uuid = prefs.load(Constants.PREF_UUID, (String) null);
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                    prefs.save(Constants.PREF_UUID, uuid);
                    ERA.log("App.AsyncTask:UUID generate and save");
                } else {
                    ERA.log("App.AsyncTask:UUID load");
                }

                if (!BuildConfig.DEBUG) {
                    Crashlytics.setUserIdentifier(uuid);
                }

                File pingFile = new File("/system/bin/ping");
                if (pingFile.exists()) {
                    PingProgram.setPingExec("/system/bin/ping");
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
                        } else {
                            PingProgram.setPingExec(null);
                        }
                    } catch (Exception ignore) {
                        PingProgram.setPingExec(null);
                    }
                }
                ERA.log("App.AsyncTask:Setup ping program");

                long initTime = prefs.load(Constants.PREF_FIRST_INIT_TIME, 0L);
                if (initTime == 0L) {
                    prefs.save(Constants.PREF_FIRST_INIT_TIME, SystemClock.elapsedRealtime() - startTime);
                }

                ERA.log("App.AsyncTask:end with time " + (SystemClock.elapsedRealtime() - startTime) + " ms");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                ERA.log("App.AsyncTask:onPostExecute");
            }
        }.executeOnExecutor(exec);

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public synchronized PingManager getPingManager() {
        if (mPingManager == null) {
            mPingManager = new PingManager();
        }
        return mPingManager;
    }
}
