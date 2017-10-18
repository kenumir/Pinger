package com.wt.pinger.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.activity.MainActivity;
import com.wt.pinger.service.StartAlarmService;

public class BootReceiver extends BroadcastReceiver {

	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BuildConfig.DEBUG) {
			Console.logd("BOOT ACTION: " + (intent != null ? intent.getAction() : "Null intent"));
		}
		
		/*
		AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent mIntent = new Intent(context, StartAlarmReceiver.class).putExtra(StartAlarmService.KEY_ALARM_ID, 1L);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		long time = System.currentTimeMillis() + (60_000 * 6);
		
		Console.logi("ALARM START AT: " + MainActivity.format(time));
		
		Intent it = new Intent(context, MainActivity.class);
		it.putExtra(StartAlarmService.KEY_ALARM_ID, 1L);
		PendingIntent alarmEditInfo = PendingIntent.getActivity(context, 2, it, PendingIntent.FLAG_UPDATE_CURRENT);
		mAlarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time, alarmEditInfo), pi);
		*/
	}

}
