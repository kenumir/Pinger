package com.wt.pinger.proto;

import android.content.Intent;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StartViewModel extends ViewModel {

	private final MutableLiveData<Intent> data = new MutableLiveData<>();

	public MutableLiveData<Intent> getData() {
		return data;
	}

}
