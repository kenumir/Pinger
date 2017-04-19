package com.wt.pinger.data.api;

import android.os.Build;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.wt.pinger.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Kenumir on 2016-08-30.
 *
 */
public class Api {

    public static String userAgent() {
	    /**
	     * fix for: IllegalArgumentException: Unexpected char 0x56db at 62 in User-Agent value: Pinger 3.2.1 (Linux; Android 4.4.2; SAMSUNG-N9106 Build/N9106-四核-MT6582)
	     */
        return String.format(
                "Pinger %s (Linux; Android %s; %s Build/%s)",
                BuildConfig.VERSION_NAME,
                Build.VERSION.RELEASE.replaceAll("[^\\p{ASCII}]", ""),
                Build.MODEL.replaceAll("[^\\p{ASCII}]", ""),
                Build.DISPLAY.replaceAll("[^\\p{ASCII}]", "")
        );
    }

    private volatile static Api singleton;

    public static Api getInstance() {
        if (singleton == null) {
            synchronized (Api.class) {
                if (singleton == null) {
                    singleton = new Api();
                }
            }
        }
        return singleton;
    }

    private ApiService service;

    private Api() {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.connectTimeout(5, TimeUnit.SECONDS);
        b.readTimeout(5, TimeUnit.SECONDS);
        b.writeTimeout(5, TimeUnit.SECONDS);
        b.addInterceptor(new ApiInterceptor());
        b.retryOnConnectionFailure(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(b.build())
                .build();

        service = retrofit.create(ApiService.class);
    }

    public boolean saveNewUser(@NonNull NewUser user) {
        Call<JsonObject> mCall = service.saveUser(user);
        try {
            Response<JsonObject> resp = mCall.execute();
            if (resp.isSuccessful()) {
                JsonObject result = resp.body();
                if (BuildConfig.DEBUG) {
                    Console.logi("Api.saveNewUser: " + result);
                }
                return result != null && result.has("success") && result.get("success").getAsBoolean();
            } else {
                RuntimeException error = new RuntimeException("Api Error, code=" + resp.code() + ", saveNewUser");
                if (BuildConfig.DEBUG) {
                    Console.loge("Api Error[saveNewUser]: " + error.toString(), error);
                }
                ERA.logException(error);
                throw error;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Console.loge("Api Error[saveNewUser]: " + e.toString(), e);
            }
        }
        return false;
    }

}
