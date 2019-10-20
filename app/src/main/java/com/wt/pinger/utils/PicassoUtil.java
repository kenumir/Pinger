package com.wt.pinger.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

/**
 * Created by Kenumir on 2017-03-15.
 *
 */

public class PicassoUtil {

    private static volatile Picasso sPicasso;

    public static Picasso get(@NonNull Context ctx) {
        if (sPicasso == null) {
            Context c = ctx.getApplicationContext();
            sPicasso = new Picasso.Builder(c)
                    .memoryCache(new LruCache(c))
                    .build();
        }
        return sPicasso;
    }

}
