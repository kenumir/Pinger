package com.wt.pinger.service;

import android.app.IntentService;
import android.content.Intent;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.activity.PingActivity;
import com.wt.pinger.proto.AlarmLock;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.receivers.StartAlarmReceiver;


public class StartAlarmService extends IntentService {

	public static final String KEY_ALARM_ID = "alarm_id";


	public StartAlarmService() {
		super("StartRecordingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			long alarmId = intent.getLongExtra(KEY_ALARM_ID, 0L);
			
			if (BuildConfig.DEBUG) {
				Console.logi("onHandleIntent alarmId=" + alarmId);
			}

			AddressItem ai = new AddressItem();
			ai._id = 1L;
			ai.addres = "wp.pl";

			getApplicationContext().sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
			//AlarmService.start(getApplicationContext());
			AlarmLock.get().acquire(getApplicationContext());
			Intent it = new Intent(getApplicationContext(), PingActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			//Intent it = new Intent(getApplicationContext(), PingActivity.class).setFlags(268697600);
			ai.saveToIntent(it);
			startActivity(it);

			StartAlarmReceiver.completeWakefulIntent(intent);
		} else {
			if (BuildConfig.DEBUG) {
				Console.logi("onHandleIntent intent is NULL");
			}
		}
	}

}
