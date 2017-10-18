package com.wt.pinger.proto;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;

/**
 * Created by Hivedi2 on 2017-10-18.
 *
 */

public class AlarmLock {
	
	private static volatile AlarmLock sAlarmLock;

	private PowerManager.WakeLock mWakeLock;
	
	public static AlarmLock get() {
		if (sAlarmLock == null) {
			sAlarmLock = new AlarmLock();
		}
		return sAlarmLock;
	}
	
	public AlarmLock release() {
		if (BuildConfig.DEBUG) {
			Console.logi("AlarmLock.release()");
		}
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
			mWakeLock = null;
		}
		return this;
	}
	
	public AlarmLock acquire(@NonNull Context ctx) {
		if (BuildConfig.DEBUG) {
			Console.logi("AlarmLock.acquire()");
		}
		release();
		mWakeLock = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ReplaioAlarmLock");
		mWakeLock.acquire();
		return this;
	}
	
}
