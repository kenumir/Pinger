package com.wt.pinger.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.hivedi.console.Console;
import com.wt.pinger.R;

/**
 * Created by Hivedi2 on 2017-10-18.
 *
 */

public class AlarmService extends Service {
	
	private static PowerManager.WakeLock mWakeLock;
	
	public static void start(Context ctx) {
		mWakeLock = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmAlertWakeLockXX");
		mWakeLock.acquire();
		Intent it = new Intent(ctx, AlarmService.class);
		it.setAction("start");
		ctx.startService(it);
	}
	
	public static void stop(Context ctx) {
		Intent it = new Intent(ctx, AlarmService.class);
		it.setAction("stop");
		ctx.startService(it);
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private NotificationCompat.Builder mBuilder;
	private Thread mThread;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Intent stopIntent = new Intent(this, AlarmService.class);
		stopIntent.setPackage(getPackageName());
		stopIntent.setAction("stop");
		
		mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_stat_notify)
				.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText("Alarm");
		mBuilder.addAction(
				R.drawable.ic_close_x,
				getString(R.string.label_stop),
				PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
		);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action != null) {
			switch (action) {
				case "start":
					startForeground(123, mBuilder.build());
					mThread = new Thread(){
						@Override
						public void run() {
							while (true) {
								Console.logi("Alarm service sleep work");
										
								if (Thread.currentThread().isInterrupted()) {
									break;
								}
								try {
									Thread.sleep(1_000);
								} catch (InterruptedException e) {
									break;
								}
							}
							mThread = null;
						}
					};
					mThread.start();
					break;
				case "stop":
					if (mThread != null) {
						mThread.interrupt();
					}
					if (mWakeLock != null && mWakeLock.isHeld()) {
						mWakeLock.release();
						mWakeLock = null;
					}
					stopForeground(true);
					break;
			}
		}
		return START_NOT_STICKY;
	}
}
