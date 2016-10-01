package com.wt.pinger.utils;

import com.wt.pinger.BuildConfig;
import com.wt.pinger.data.api.Api;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Kenumir on 2016-09-27.
 *
 */

public class Networking {

    private static volatile OkHttpClient client = new OkHttpClient.Builder().build();

    public static void postSupport(String s, String uuid) {
        RequestBody formBody = new FormBody.Builder()
                .add("text", s)
                .add("extra",
                    "MODEL: " + android.os.Build.MODEL + "; " +
                    "SDK: " + android.os.Build.VERSION.SDK_INT + "; " +
                    "BRAND: " + android.os.Build.BRAND + "; " +
                    "UUID: " + uuid
                )
                .build();
        Request r = new Request.Builder()
                .url(BuildConfig.SUPPORT_URL)
                .header("User-Agent", Api.userAgent())
                .post(formBody)
                .build();
        Call call = client.newCall(r);
        try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
