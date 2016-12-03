package com.wt.pinger.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.wt.pinger.BuildConfig;

/**
 * Created by Kenumir on 2016-12-03.
 *
 */

public class RemoteConfig {

    public static abstract class OnGetListener {
        protected abstract void onSuccess(FirebaseRemoteConfig config);
        void onError(FirebaseRemoteConfig config) {}
    }

    private volatile static RemoteConfig singleton;

    public static RemoteConfig get() {
        if (singleton == null) {
            RemoteConfig res = new RemoteConfig();
            synchronized (RemoteConfig.class) {
                if (singleton == null) {
                    singleton = res;
                }
            }
        }
        return singleton;
    }

    private RemoteConfig() {
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        config.setConfigSettings(configSettings);
    }

    public void getConfig(final @Nullable OnGetListener listener) {
        FirebaseRemoteConfig.getInstance().fetch(1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
                config.activateFetched();
                if (task.isSuccessful()) {
                    if (listener != null) {
                        listener.onSuccess(config);
                    }
                } else {
                    if (listener != null) {
                        listener.onError(config);
                    }
                }
            }
        });
    }

}
