package com.wt.pinger.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.utils.DbWrapper;

public class DbContentProvider extends ContentProvider {

    private static final String PROVIDER_AUTHORITY = "com.wt.pinger.providers.db";
    public static final Uri URI_CONTENT = Uri.parse("content://" + PROVIDER_AUTHORITY + "/");

    private DbWrapper mDbWrapper;

    public DbContentProvider() {
    }

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            mDbWrapper = DbWrapper.get(getContext());
            return true;
        }
        return false;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (uri.equals(URI_CONTENT)) {
            int res = 0;
            if (selectionArgs != null && selectionArgs.length >= 1) {
                res = mDbWrapper.delEntry(Long.valueOf(selectionArgs[0]));
                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
            }
            return res;
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
        if (uri.equals(URI_CONTENT)) {
            long id = mDbWrapper.addEntry(
                    values.getAsString(AddressItem.FIELD_ADDRESS),
                    values.get(AddressItem.FIELD_PACKET) != null ? values.getAsInteger(AddressItem.FIELD_PACKET) : 0,
                    values.get(AddressItem.FIELD_PINGS) != null ? values.getAsInteger(AddressItem.FIELD_PINGS) : 0,
                    values.getAsString(AddressItem.FIELD_DISPLAY_NAME),
                    values.get(AddressItem.FIELD_INTERVAL) != null ? values.getAsInteger(AddressItem.FIELD_INTERVAL) : 0
            );
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(itemUri, null);
            }
            return itemUri;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uri.equals(URI_CONTENT)) {
            Cursor res = mDbWrapper.getItems();
            if (getContext() != null) {
                res.setNotificationUri(getContext().getContentResolver(), uri);
            }
            return res;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (uri.equals(URI_CONTENT)) {
            int a = mDbWrapper.updateEntry(
                    values.getAsLong(AddressItem.FIELD_ID),
                    values.getAsString(AddressItem.FIELD_ADDRESS),
                    values.get(AddressItem.FIELD_PACKET) != null ? values.getAsInteger(AddressItem.FIELD_PACKET) : 0,
                    values.get(AddressItem.FIELD_PINGS) != null ? values.getAsInteger(AddressItem.FIELD_PINGS) : 0,
                    values.getAsString(AddressItem.FIELD_DISPLAY_NAME),
                    values.get(AddressItem.FIELD_INTERVAL) != null ? values.getAsInteger(AddressItem.FIELD_INTERVAL) : 0
            );

            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return a;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
