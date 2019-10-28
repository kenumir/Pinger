package com.wt.pinger.proto.ping;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.service.PingWorkerService;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PingManager {

    private LinkedHashMap<Long, PingWorker> workerInstances;
    private ArrayList<OnPingWorkerListener> mListeners;

    public PingManager() {
        workerInstances = new LinkedHashMap<>();
        mListeners = new ArrayList<>();
    }

    public synchronized void addListener(OnPingWorkerListener o) {
        mListeners.remove(o);
        mListeners.add(o);
        AddressItem ai = o.getAddressItem();
        if (ai != null) {
            o.onWorkerStatusUpdate(workerInstances.get(ai._id) != null);
        }
    }

    public synchronized void delListener(OnPingWorkerListener o) {
        mListeners.remove(o);
    }

    public synchronized void newWorkerInstance(@NonNull AddressItem a, @NonNull PingWorkerService service) {
        stopPingWorker(a);
        workerInstances.put(a._id, new PingWorker(service, a));
        for(OnPingWorkerListener l : mListeners) {
            AddressItem a2 = l.getAddressItem();
            l.onWorkerStatusUpdate(a2 != null && a2._id.equals(a._id));
        }
    }

    public void startPingWorker(@NonNull Context ctx, @NonNull AddressItem a) {
        Intent it = new Intent(ctx, PingWorkerService.class);
        it.setAction(PingWorkerService.ACTION_START_PING);
        a.saveToIntent(it);
        ContextCompat.startForegroundService(ctx, it);
    }

    public synchronized void startStopPingWorker(@NonNull Context ctx, @NonNull AddressItem a) {
        if (workerInstances.get(a._id) == null) {
            startPingWorker(ctx, a);
        } else {
            stopPingWorker(a);
        }
    }

    public synchronized void stopPingWorker(@NonNull AddressItem a) {
        PingWorker w = workerInstances.get(a._id);
        if (w != null) {
            w.terminate();
            workerInstances.remove(a._id);
            for(OnPingWorkerListener l : mListeners) {
                AddressItem a2 = l.getAddressItem();
                if (a2 != null && a._id.equals(a2._id)) {
                    l.onWorkerStatusUpdate(false);
                }
            }
        }
    }

}
