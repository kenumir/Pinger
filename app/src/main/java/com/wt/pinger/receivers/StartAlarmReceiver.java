package com.wt.pinger.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.service.StartAlarmService;


public class StartAlarmReceiver extends WakefulBroadcastReceiver {
	
	public StartAlarmReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		long alarmId = intent.getLongExtra(StartAlarmService.KEY_ALARM_ID, 0L);
		
		if (BuildConfig.DEBUG) {
			Console.logi("StartAlarmReceiver: onReceive, alarmId=" + alarmId);
		}

		if (alarmId > 0L) {
			Intent service = new Intent(context, StartAlarmService.class);
			service.putExtra(StartAlarmService.KEY_ALARM_ID, alarmId);
			startWakefulService(context, service);
		} else {
			if (BuildConfig.DEBUG) {
				Console.logi("StartAlarmReceiver: NO ALARM ID");
			}
		}
	}
}
