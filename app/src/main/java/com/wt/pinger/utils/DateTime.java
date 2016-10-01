package com.wt.pinger.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Kenumir on 2016-09-08.
 *
 */
public class DateTime {

    private static final SimpleDateFormat fmt24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat fmt12 = new SimpleDateFormat("KK:mm:ss a", Locale.getDefault());

    public static String formatTime(@NonNull Context ctx, Long dateTime) {
        return android.text.format.DateFormat.is24HourFormat(ctx) ?
                fmt24.format(dateTime) :
                fmt12.format(dateTime);
    }

}
