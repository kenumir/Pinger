package com.wt.pinger.data.api;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Kenumir on 2016-01-25.
 *
 */
class ApiInterceptor implements Interceptor {

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();
		Request compressedRequest = originalRequest.newBuilder()
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.header("User-Agent", Api.userAgent())
                .header("X-Api-Secret", BuildConfig.API_SECRET)
				.build();

        if (BuildConfig.DEBUG) {
            Console.logi("URL: " + originalRequest.url());
        }

		return chain.proceed(compressedRequest);
	}
}
