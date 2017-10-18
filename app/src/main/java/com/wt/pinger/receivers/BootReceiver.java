package com.wt.pinger.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;

public class BootReceiver extends BroadcastReceiver {

	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BuildConfig.DEBUG) {
			Console.logd("BOOT ACTION: " + (intent != null ? intent.getAction() : "Null intent"));
		}

	}

}
