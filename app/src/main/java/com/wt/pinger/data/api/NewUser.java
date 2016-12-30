package com.wt.pinger.data.api;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.wt.pinger.BuildConfig;

/**
 * Created by Kenumir on 2016-08-30.
 *
 */
public class NewUser {

    public String uuid;

    public String system_api_name;
    public int system_api_sdk;
    public String system_brand;
    public String system_manufacturer;
    public String system_model;
    public String system_product;
    public String system_build;
    public String referrer;
    public float device_density;
    public int device_width;
    public int device_height;

    public int app_version;
    public String app_version_name;

    public long first_init_time = 0;

    public static NewUser init(@NonNull Context ctx, @NonNull String uuid, long initTime, String referrer) {
        NewUser res = new NewUser();
        res.uuid = uuid;
        res.first_init_time = initTime;

        res.system_api_name = Build.VERSION.RELEASE;
        res.system_api_sdk = Build.VERSION.SDK_INT;

        res.system_brand = Build.BRAND;
        res.system_manufacturer = Build.MANUFACTURER;
        res.system_model = Build.MODEL;
        res.system_product = Build.PRODUCT;
        res.system_build = Build.DISPLAY;

        res.referrer = referrer;

        res.app_version = BuildConfig.VERSION_CODE;
        res.app_version_name = BuildConfig.VERSION_NAME;

        res.device_density = ctx.getResources().getDisplayMetrics().density;
        res.device_width = ctx.getResources().getDisplayMetrics().widthPixels;
        res.device_height = ctx.getResources().getDisplayMetrics().heightPixels;
        return res;
    }

}
