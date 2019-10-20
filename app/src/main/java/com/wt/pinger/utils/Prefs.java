package com.wt.pinger.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Kenumir on 2016-08-30.
 *
 */
public class Prefs {

    public interface OnPrefsReady {
        void onReady(Prefs prefs);
    }

    private volatile static Prefs singleton;

    private static final Executor exec = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "PrefsTask");
        }
    });

    public static Prefs get(@NonNull Context ctx) {
        if (singleton == null) {
            Prefs res = new Prefs(ctx);
            synchronized (Prefs.class) {
                if (singleton == null) {
                    singleton = res;
                }
            }
        }
        return singleton;
    }

    public static void getAsync(@NonNull Context ctx, final @Nullable OnPrefsReady cb) {
        if (singleton == null) {
            new AsyncTask<Context, Void, Prefs>(){
                @Override
                protected Prefs doInBackground(Context... params) {
                    return Prefs.get(params[0]);
                }
                @Override
                protected void onPostExecute(Prefs prefs) {
                    if (cb != null) {
                        cb.onReady(prefs);
                    }
                }
            }.executeOnExecutor(exec, ctx);
        } else {
            if (cb != null) {
                cb.onReady(singleton);
            }
        }
    }

    private SharedPreferences pref;

    private Prefs(@NonNull Context ctx) {
        pref = ctx.getApplicationContext()
                .getSharedPreferences(ctx.getPackageName() + ".PREFS", Context.MODE_PRIVATE);
    }

    // String
    public Prefs save(@NonNull String key, String value) {
        pref.edit().putString(key, value).apply();
        return this;
    }
    @Nullable
    public String load(@NonNull String key, @Nullable String defaultValue) {
        return pref.getString(key, defaultValue);
    }
    @Nullable
    public String load(@NonNull String key) {
        return load(key, (String) null);
    }

    // Boolean
    public Prefs save(@NonNull String key, boolean value) {
        pref.edit().putBoolean(key, value).apply();
        return this;
    }

    public boolean load(@NonNull String key, boolean defaultValue) {
        return pref.getBoolean(key, defaultValue);
    }

    // int
    public Prefs save(@NonNull String key, int value) {
        pref.edit().putInt(key, value).apply();
        return this;
    }

    public int load(@NonNull String key, int defaultValue) {
        return pref.getInt(key, defaultValue);
    }

    // long
    public Prefs save(@NonNull String key, long value) {
        pref.edit().putLong(key, value).apply();
        return this;
    }

    public long load(@NonNull String key, long defaultValue) {
        return pref.getLong(key, defaultValue);
    }

    // float
    public Prefs save(@NonNull String key, float value) {
        pref.edit().putFloat(key, value).apply();
        return this;
    }

    public float load(@NonNull String key, float defaultValue) {
        return pref.getFloat(key, defaultValue);
    }

    // Set<String>
    public Prefs save(@NonNull String key, Set<String> value) {
        pref.edit().putStringSet(key, value).apply();
        return this;
    }

    public Set<String> load(@NonNull String key, Set<String> defaultValue) {
        return pref.getStringSet(key, defaultValue);
    }

}
