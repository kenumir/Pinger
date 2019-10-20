package com.wt.pinger.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.hivedi.console.Console;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.utils.DbWrapper;

public class DbContentProvider extends ContentProvider {

    private static final String PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".providers.db";
    public static final Uri URI_CONTENT = Uri.parse("content://" + PROVIDER_AUTHORITY + "/");
    public static final Uri URI_CONTENT_COMMANDS = Uri.parse("content://" + PROVIDER_AUTHORITY + "/commands");
    public static final Uri URI_CONTENT_COMMANDS_RESET = Uri.parse("content://" + PROVIDER_AUTHORITY + "/commands_reset");

	public static class Commands {
		public static final String FIELD_COMMAND_TEXT = "command_text";
		public static final String FIELD_COMMAND_ID = "_id";
	}

	public static final CharSequence[] DEFAULT_COMMAND_LIST = new CharSequence[]{
			"pwd", "date", "dumpsys", "id", "ifconfig", "ime list", "iptables -h",
			"logcat", "lsof", "netstat", "ps", "pm", "uptime", "vmstat"
	};

    private DbWrapper mDbWrapper;
	private SQLiteOpenHelper commandsDb;

    public DbContentProvider() {
    }

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            mDbWrapper = DbWrapper.get(getContext());
	        commandsDb = new SQLiteOpenHelper(getContext(), "commands.sqlite", null, 1) {
		        @Override
		        public void onCreate(SQLiteDatabase db) {
			        db.execSQL("CREATE TABLE IF NOT EXISTS commands (" + Commands.FIELD_COMMAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + Commands.FIELD_COMMAND_TEXT + " TEXT)");
			        /**
			         * init database with default values
			         */
			        db.beginTransaction();
			        try {
				        ContentValues cv = new ContentValues();
				        for(CharSequence s : DEFAULT_COMMAND_LIST) {
					        cv.put(Commands.FIELD_COMMAND_TEXT, s.toString());
					        db.insert("commands", null, cv);
				        }
				        db.setTransactionSuccessful();
			        } catch (Exception e) {
				        // ignore
			        } finally {
				        db.endTransaction();
			        }
		        }

		        @Override
		        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			        switch(oldVersion + 1) {
				        case 2: // future updates
			        }
		        }
	        };
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
        } else if (uri.equals(URI_CONTENT_COMMANDS)) {
	        Console.logi("del id=" + selectionArgs[0]);
	        int res = commandsDb.getWritableDatabase().delete("commands", selection, selectionArgs);
	        if (getContext() != null) {
		        getContext().getContentResolver().notifyChange(uri, null);
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
        } else if (uri.equals(URI_CONTENT_COMMANDS)) {
	        long id = commandsDb.getWritableDatabase().insert("commands", null, values);
	        Uri itemUri = ContentUris.withAppendedId(uri, id);
	        if (getContext() != null) {
		        getContext().getContentResolver().notifyChange(itemUri, null);
	        }
	        return itemUri;
        } else if (uri.equals(URI_CONTENT_COMMANDS_RESET)) {
	        SQLiteDatabase db = commandsDb.getWritableDatabase();
	        db.beginTransaction();
	        try {
		        db.delete("commands", null, null);
		        ContentValues cv = new ContentValues();
		        for(CharSequence s : DEFAULT_COMMAND_LIST) {
			        cv.put(Commands.FIELD_COMMAND_TEXT, s.toString());
			        db.insert("commands", null, cv);
		        }
		        db.setTransactionSuccessful();
	        } catch (Exception e) {
		        // ignore
	        } finally {
		        db.endTransaction();
	        }
	        return uri;
	    }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
	    if (getContext() != null) {
		    if (uri.equals(URI_CONTENT)) {
			    Cursor res = mDbWrapper.getItems();
			    if (getContext() != null) {
				    res.setNotificationUri(getContext().getContentResolver(), uri);
			    }
			    return res;
		    } else if (uri.equals(URI_CONTENT_COMMANDS)) {
			    String where = selection != null ? " WHERE " + selection : "";
			    String orderBy = sortOrder != null ? " ORDER BY " + sortOrder : "";
			    Cursor commandsRes = commandsDb.getReadableDatabase().rawQuery("SELECT * FROM commands" + where + orderBy, selectionArgs);
			    commandsRes.setNotificationUri(getContext().getContentResolver(), uri);
			    return commandsRes;
		    }
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
