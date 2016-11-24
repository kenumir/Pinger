package com.wt.pinger.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.hivedi.console.Console;
import com.hivedi.querybuilder.QueryBuilder;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.providers.data.AddressItem;

public class DbWrapper extends SQLiteOpenHelper {
	
	private static final String TABLE_ITEMS = "items";

	private static final String KEY_ITEMS_ID = AddressItem.FIELD_ID;
	private static final String KEY_ITEMS_ADDRES = AddressItem.FIELD_ADDRESS;
	private static final String KEY_ITEMS_PACKET = AddressItem.FIELD_PACKET;
	private static final String KEY_ITEMS_PINGS = AddressItem.FIELD_PINGS;
	private static final String KEY_ITEMS_INTERVAL = AddressItem.FIELD_INTERVAL;
	private static final String KEY_ITEMS_DISPLAY_NAME = AddressItem.FIELD_DISPLAY_NAME;

	private static final int DB_VERSION = 7;
	private static final String DB_NAME = "pinger.sqlite";
	
	private SQLiteDatabase db;
	private volatile static DbWrapper sDbWrapper;
	
	public static String getDbFileName() {
		return DB_NAME;
	}
	
	public static synchronized DbWrapper get(@NonNull Context context) {
		if (sDbWrapper == null) {
            sDbWrapper = new DbWrapper(context);
        }
		return sDbWrapper;
	}

	public DbWrapper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		db = this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_ITEMS + " (" +
                        KEY_ITEMS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        //KEY_ITEMS_NAME + " TEXT, " +
                        KEY_ITEMS_ADDRES + " TEXT ," +
                        KEY_ITEMS_PACKET + " INTEGER, " +
                        KEY_ITEMS_PINGS + " INTEGER, " +
                        KEY_ITEMS_INTERVAL + " INTEGER, " +
                        KEY_ITEMS_DISPLAY_NAME + " TEXT" +
                        ")"
        );
	}
	
	public Cursor getItems() {
		QueryBuilder qb = new QueryBuilder()
			.from(TABLE_ITEMS)
            .orderBy(KEY_ITEMS_ID);
		return this.db.rawQuery(qb.getSelect(), qb.getParams());
	}

	public long addEntry(String address, int packet, int pings, String name, int interval) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_ITEMS_ADDRES, address);
        if (packet > 0) {
            cv.put(KEY_ITEMS_PACKET, packet);
        } else {
            cv.putNull(KEY_ITEMS_PACKET);
        }
        if (pings > 0) {
            cv.put(KEY_ITEMS_PINGS, pings);
        } else {
            cv.putNull(KEY_ITEMS_PINGS);
        }
        if (interval > 0) {
            cv.put(KEY_ITEMS_INTERVAL, interval);
        } else {
            cv.putNull(KEY_ITEMS_INTERVAL);
        }
        if (name != null && name.length() > 0) {
            cv.put(KEY_ITEMS_DISPLAY_NAME, name);
        } else {
            cv.putNull(KEY_ITEMS_DISPLAY_NAME);
        }
		return this.db.insert(TABLE_ITEMS, null, cv);
	}

	public int updateEntry(Long _id, String address, int packet, int pings, String name, int interval) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_ITEMS_ADDRES, address);
        if (packet > 0) {
            cv.put(KEY_ITEMS_PACKET, packet);
        } else {
            cv.putNull(KEY_ITEMS_PACKET);
        }
        if (pings > 0) {
            cv.put(KEY_ITEMS_PINGS, pings);
        } else {
            cv.putNull(KEY_ITEMS_PINGS);
        }
        if (interval > 0) {
            cv.put(KEY_ITEMS_INTERVAL, interval);
        } else {
            cv.putNull(KEY_ITEMS_INTERVAL);
        }
        if (name != null && name.length() > 0) {
            cv.put(KEY_ITEMS_DISPLAY_NAME, name);
        } else {
            cv.putNull(KEY_ITEMS_DISPLAY_NAME);
        }
		return this.db.update(TABLE_ITEMS, cv, KEY_ITEMS_ID + "=?", new String[]{_id.toString()});
	}

	public int delEntry(Long _id) {
		return this.db.delete(TABLE_ITEMS, KEY_ITEMS_ID + "=?", new String[]{_id.toString()});
	}
	
	// -------------------

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion > oldVersion) {
			// upgrade
			switch(oldVersion + 1) {
				case 2:
				case 3:
                    String sql3 = "ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + KEY_ITEMS_PACKET + " INTEGER";
                    db.execSQL(sql3);
                    if (BuildConfig.DEBUG) {
                        Console.logi("SQL3: " + sql3);
                    }
                case 4:
                    String sql4 = "ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + KEY_ITEMS_PINGS + " INTEGER";
                    db.execSQL(sql4);
                    if (BuildConfig.DEBUG) {
                        Console.logi("SQL4: " + sql4);
                    }
                case 5:
                    String sql5 = "ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + KEY_ITEMS_DISPLAY_NAME + " TEXT";
                    db.execSQL(sql5);
                    if (BuildConfig.DEBUG) {
                        Console.logi("SQL5: " + sql5);
                    }
                case 6:
                    // no db update - skip create filed `name`
                case 7:
                    String sql7 = "ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + KEY_ITEMS_INTERVAL + " INTEGER";
                    db.execSQL(sql7);
                    if (BuildConfig.DEBUG) {
                        Console.logi("SQL7: " + sql7);
                    }
                case 8:
			}
		}
	}

}
