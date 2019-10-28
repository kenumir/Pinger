package com.wt.pinger.proto;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wt.pinger.App;
import com.wt.pinger.proto.ping.OnPingWorkerListener;
import com.wt.pinger.proto.ping.PingManager;
import com.wt.pinger.providers.data.AddressItem;

public abstract class BaseActivity extends AppCompatActivity implements OnPingWorkerListener {

    public App getApp() {
        return (App) getApplication();
    }

    public PingManager getPingManager() {
        return getApp().getPingManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPingManager().addListener(this);
    }

    @Override
    protected void onStop() {
        getPingManager().delListener(this);
        super.onStop();
    }


    @Override
    public void onWorkerStatusUpdate(boolean isWorking) {

    }

    @Nullable
    @Override
    public AddressItem getAddressItem() {
        return null;
    }
}
