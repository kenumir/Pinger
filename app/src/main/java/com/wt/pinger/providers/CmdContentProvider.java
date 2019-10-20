package com.wt.pinger.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CmdContentProvider extends ContentProvider {

    private static final String PROVIDER_AUTHORITY = "com.wt.pinger.providers.cmd";
    public static final Uri URI_CONTENT = Uri.parse("content://" + PROVIDER_AUTHORITY + "/");

    private final Object dataSync = new Object();
    private ArrayList<String> data;

    public CmdContentProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (uri.equals(URI_CONTENT)) {
            synchronized (dataSync) {
                data.clear();
            }
            //if (getContext() != null) {
            //    getContext().getContentResolver().notifyChange(uri, null);
            //}
            return 1;
        }
        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (uri.equals(URI_CONTENT)) {
            long id;
            synchronized (dataSync) {
                id = data.size() + 1;
                data.add(values.getAsString("data"));
            }
            Uri itemUri = ContentUris.withAppendedId(uri, id);

            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(itemUri, null);
            }

            return itemUri;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        data = new ArrayList<>();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uri.equals(URI_CONTENT)) {
            MatrixCursor res = new MatrixCursor(new String[]{"_id", "data"});
            long _id = 1;
            synchronized (dataSync) {
                for (String d : data) {
                    res.addRow(new Object[]{
                            _id,
                            d
                    });
                    _id++;
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
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
