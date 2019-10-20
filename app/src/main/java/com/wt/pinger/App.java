package com.wt.pinger;

import android.app.Application;
import android.os.SystemClock;

import com.crashlytics.android.Crashlytics;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.hivedi.era.ReportInterface;
import com.kenumir.eventclip.EventClip;
import com.wt.pinger.events.providers.AnswersEventProvider;
import com.wt.pinger.events.providers.FireBaseEventProvider;
import com.wt.pinger.proto.Constants;
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

        Trace fabricTrace = FirebasePerformance.getInstance().newTrace("fabric_init");
	    fabricTrace.start();
        Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
	    fabricTrace.stop();
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
            /**
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

        final long t1 = SystemClock.elapsedRealtime();
        EventClip.registerProvider(new FireBaseEventProvider(App.this));
        EventClip.registerProvider(new AnswersEventProvider());
        ERA.log("App:FireBaseEventProvider init time " + (SystemClock.elapsedRealtime() - t1) + " ms");

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                final long startTime = SystemClock.elapsedRealtime();

                ERA.log("App.AsyncTask:begin");
                Prefs prefs = Prefs.get(App.this);

                ERA.log("App.AsyncTask:Prefs init");
                Trace uuidTrace = FirebasePerformance.getInstance().newTrace("load_uuid");
	            uuidTrace.start();
                // UUID
                String uuid = prefs.load(Constants.PREF_UUID, (String) null);
                if (uuid == null) {
                    uuid = UUID.randomUUID().toString();
                    prefs.save(Constants.PREF_UUID, uuid);
                    ERA.log("App.AsyncTask:UUID generate and save");
                } else {
                    ERA.log("App.AsyncTask:UUID load");
                }
	            uuidTrace.stop();

                if (!BuildConfig.DEBUG) {
                    Crashlytics.setUserIdentifier(uuid);
                }

	            Trace pingTrace = FirebasePerformance.getInstance().newTrace("ping_detect");
	            pingTrace.start();
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
	            pingTrace.stop();
                ERA.log("App.AsyncTask:Setup ping program");

                long initTime = prefs.load(Constants.PREF_FIRST_INIT_TIME, 0L);
                if (initTime == 0L) {
                    prefs.save(Constants.PREF_FIRST_INIT_TIME, SystemClock.elapsedRealtime() - startTime);
                }

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
