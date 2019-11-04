package com.wt.pinger.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.wt.pinger.proto.ItemProto;
import com.wt.pinger.providers.data.PingItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public class PingContentProvider extends ContentProvider {

    public static final String WHERE_DELETE_OLD_SESSIONS = "delete_old_session";

    private static final String PROVIDER_AUTHORITY = "com.wt.pinger.providers.ping";
    public static final Uri URI_CONTENT = Uri.parse("content://" + PROVIDER_AUTHORITY + "/");

    private final Object dataSync = new Object();
    private ArrayList<PingItem> mData = new ArrayList<>();
    private AtomicLong lastId = new AtomicLong(1);

    public PingContentProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (uri.equals(URI_CONTENT)) {
            long addressId = 0;
            if (selectionArgs != null && selectionArgs.length > 0) {
                try {
                    addressId = Long.parseLong(selectionArgs[0]);
                } catch (Exception e) {
                    addressId = 0;
                }
            }

            if (selection != null && selection.equals(WHERE_DELETE_OLD_SESSIONS)) {
                synchronized (dataSync) {
                    Iterator<PingItem> it = mData.iterator();
                    while (it.hasNext()) {
                        PingItem item = it.next();
                        if (item.addressId != null && item.addressId != addressId) {
                            it.remove();
                        }
                    }
                }
            } else {
                if (addressId == 0) {
                    synchronized (dataSync) {
                        mData.clear();
                    }
                } else {
                    synchronized (dataSync) {
                        Iterator<PingItem> it = mData.iterator();
                        while (it.hasNext()) {
                            PingItem item = it.next();
                            if (item.addressId != null && item.addressId == addressId) {
                                it.remove();
                            }
                        }
                    }
                }
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
            }
            return 0;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Console.logi("insert: " + values);
        if (uri.equals(URI_CONTENT)) {
            try {
                PingItem newItem = new PingItem();
                newItem._id = lastId.getAndIncrement();
                newItem.time = values.get(PingItem.FIELD_TIME) != null ? values.getAsDouble(PingItem.FIELD_TIME) : 0D;
                newItem.seq = values.get(PingItem.FIELD_SEQ) != null ? values.getAsInteger(PingItem.FIELD_SEQ) : 0;
                newItem.ttl = values.get(PingItem.FIELD_TTL) != null ? values.getAsInteger(PingItem.FIELD_TTL) : 0;
                newItem.info = values.get(PingItem.FIELD_INFO) != null ? values.getAsString(PingItem.FIELD_INFO) : null;
                newItem.timestamp = values.get(PingItem.FIELD_TIMESTAMP) != null ? values.getAsLong(PingItem.FIELD_TIMESTAMP) : 0L;
                newItem.addressId = values.get(PingItem.FIELD_ADDRESS_ID) != null ? values.getAsLong(PingItem.FIELD_ADDRESS_ID) : 0L;
                synchronized (dataSync) {
                    mData.add(newItem);
                    // TODO optimize array size, get array size and trim unnecessary data (other ping address)
                }
                Uri itemUri = ContentUris.withAppendedId(uri, newItem._id);
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(itemUri, null);
                }
                return itemUri;
            } catch (Exception e) {
                ERA.logException(e);
            }
            return uri;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uri.equals(URI_CONTENT)) {
            MatrixCursor res = new MatrixCursor(new String[]{
                    ItemProto.FIELD_ID,
                    PingItem.FIELD_TIME,
                    PingItem.FIELD_SEQ,
                    PingItem.FIELD_TTL,
                    PingItem.FIELD_INFO,
                    PingItem.FIELD_TIMESTAMP,
                    PingItem.FIELD_ADDRESS_ID,
            });

            long addressId = 0;
            if (selectionArgs != null && selectionArgs.length > 0) {
                try {
                    addressId = Long.parseLong(selectionArgs[0]);
                } catch (Exception e) {
                    // igonre
                }
            }

            synchronized (dataSync) {
                if (mData.size() > 0) {
                    for (int i = mData.size() - 1; i >= 0; i--) {
                        PingItem d = mData.get(i);
                        if (addressId == 0 || d.addressId == null || d.addressId == 0) {
                            res.addRow(new Object[]{d._id, d.time, d.seq, d.ttl, d.info, d.timestamp, d.addressId});
                        } else {
                            if (d.addressId.equals(addressId)) {
                                res.addRow(new Object[]{d._id, d.time, d.seq, d.ttl, d.info, d.timestamp, d.addressId});
                            }
                        }
                    }
                }
            }

            if (getContext() != null) {
                res.setNotificationUri(getContext().getContentResolver(), uri);
            }
            return res;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
