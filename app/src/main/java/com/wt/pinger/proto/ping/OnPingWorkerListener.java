package com.wt.pinger.proto.ping;

import androidx.annotation.Nullable;

import com.wt.pinger.providers.data.AddressItem;

public interface OnPingWorkerListener {

    @Nullable
    AddressItem getAddressItem();

    void onWorkerStatusUpdate(boolean isWorking);

}
