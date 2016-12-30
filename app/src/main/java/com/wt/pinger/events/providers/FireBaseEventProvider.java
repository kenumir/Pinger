package com.wt.pinger.events.providers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kenumir.eventclip.proto.EventClipProvider;
import com.kenumir.eventclip.proto.EventParam;
import com.kenumir.eventclip.proto.UserParam;

import java.util.Locale;
import java.util.Map;

/**
 * Created by Kenumir on 2016-12-03.
 *
 */

public class FireBaseEventProvider extends EventClipProvider {

    private FirebaseAnalytics mFirebaseAnalytics;

    public FireBaseEventProvider(@NonNull Context ctx) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(ctx.getApplicationContext());
    }

    @Override
    public void deliver(EventParam eventParam) {
        deliverInternal(eventParam.getName(), eventParam.getAllFields());
    }

    @Override
    public void userProperty(@NonNull UserParam userParam) {

    }

    private void deliverInternal(@NonNull String eventName, @Nullable Map<String, Object> params) {
        Bundle bundle = null;
        if (params != null && params.size() > 0 && params.size() % 2 == 0) {
            bundle = new Bundle();
            for(String name : params.keySet()) {
                Object value = params.get(name);
                if (value instanceof Long) {
                    bundle.putLong(parseEventName(name), (long) value);
                } else if (value instanceof String) {
                    bundle.putString(parseEventName(name), (String) value);
                } else if (value instanceof Integer) {
                    bundle.putInt(parseEventName(name), (int) value);
                } else if (value instanceof Boolean) {
                    bundle.putBoolean(parseEventName(name), (boolean) value);
                } else {
                    bundle.putString(parseEventName(name), value.toString());
                }
            }
        }
        mFirebaseAnalytics.logEvent(parseEventName(eventName), bundle);
    }

    private String parseEventName(String e) {
        return e.toLowerCase(Locale.getDefault()).replaceAll(" ", "_");
    }

}
