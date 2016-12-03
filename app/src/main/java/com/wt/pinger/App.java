package com.wt.pinger;

import android.app.Application;
import android.os.SystemClock;

import com.crashlytics.android.Crashlytics;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.hivedi.era.ReportInterface;
import com.hivedi.eventclip.EventClip;
import com.wt.pinger.data.api.Api;
import com.wt.pinger.data.api.NewUser;
import com.wt.pinger.events.providers.FireBaseEventProvider;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.utils.PingProgram;
import com.wt.pinger.utils.Prefs;
import com.wt.pinger.utils.RemoteConfig;

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

    public interface OnAppReady {
        void onAppReady();
    }

    private final static ExecutorService exec = Executors.newSingleThreadExecutor();

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
                    FirebaseCrash.report(throwable);
                }

                @Override
                public void log(String s, Object... objects) {
                    Crashlytics.log(s);
                    FirebaseCrash.log(s);
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

        final long t1 = SystemClock.elapsedRealtime();
        EventClip.registerProvider(new FireBaseEventProvider(App.this));
        ERA.log("App:FireBaseEventProvider init time " + (SystemClock.elapsedRealtime() - t1) + " ms");

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                final long startTime = SystemClock.elapsedRealtime();

                ERA.log("App.AsyncTask:begin");
                Prefs prefs = Prefs.get(App.this);

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
                ERA.log("App.AsyncTask:Setup ping program");

                if (BuildConfig.BUILD_FLAG_USE_API) {
                    if (!prefs.load(Constants.PREF_UUID_SAVED, false)) {
                        if (Api.getInstance().saveNewUser(NewUser.init(uuid, SystemClock.elapsedRealtime() - startTime, App.this, valid_ping_program))) {
                            prefs.save(Constants.PREF_UUID_SAVED, true);
                            ERA.log("App.AsyncTask:Save new user");
                        }
                    }
                }

                RemoteConfig.get().getConfig(new RemoteConfig.OnGetListener() {
                    @Override
                    protected void onSuccess(FirebaseRemoteConfig config) {
                        Console.loge("show_replaio_promo=" + config.getLong("show_replaio_promo"));
                    }
                });
                ERA.log("App.AsyncTask:FirebaseRemoteConfig fetch");

                ERA.log("App.AsyncTask:end with time " + (SystemClock.elapsedRealtime() - startTime) + " ms");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                ERA.log("App.AsyncTask:onPostExecute");
                appReady = true;
                if (mOnAppReady != null) {
                    mOnAppReady.onAppReady();
                }
            }
        }.executeOnExecutor(exec);
    }
}
