package com.wt.pinger.proto;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.wt.pinger.activity.StartActivity;


public class StartActivityObserver implements Observer<Intent> {

	private final StartActivity mStartActivity;

	public StartActivityObserver(@NonNull StartActivity s) {
		mStartActivity = s;
	}

	@Override
	public void onChanged(Intent intent) {
		mStartActivity.startActivity(intent);
		mStartActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		mStartActivity.finish();
	}
}
