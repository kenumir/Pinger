package com.wt.pinger.proto;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.wt.pinger.BuildConfig;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenumir on 2015-03-02.
 *
 */
public class ItemProto implements Cloneable, Serializable {

	public static final String FIELD_ID = "_id";
	public static final String INTENT_ITEM_NAME = "intent_item_name";

	public static String getRootKeyName(@NonNull Class<?> className) {
		return className.getCanonicalName() + INTENT_ITEM_NAME;
	}

	@DataFieldAnnotation()
	public Long _id;

	public ContentValues toContentValues(boolean withoutPrimaryKey) {
		return toContentValues(withoutPrimaryKey, false);
	}

	public ContentValues toContentValues() {
		return toContentValues(false, false);
	}
	
	public ContentValues toContentValues(boolean withoutPrimaryKey, boolean withVirtualFields) {
		ContentValues res = new ContentValues();

		try {
			for(Field f : this.getClass().getFields()) {
				f.setAccessible(true);
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				if (f.getType().isPrimitive()) continue;

				// TODO sprawdzić baze czy wszędzie są poustawiane adnotacje
				DataFieldAnnotation dfa = f.getAnnotation(DataFieldAnnotation.class);
				if (dfa == null) continue;

				if (!withVirtualFields) {
					//DataFieldAnnotation dfa = f.getAnnotation(DataFieldAnnotation.class);
					if (dfa.isVirtualField()) continue;
				}
				
				if (f.get(this) == null) {
					res.putNull(f.getName());
				} else {
					if (f.getType().equals(Double.class)) {
						res.put(f.getName(), (Double) f.get(this));
					} else if (f.getType().equals(Long.class)) {
						res.put(f.getName(), (Long) f.get(this));
					} else if (f.getType().equals(Integer.class)) {
						res.put(f.getName(), (Integer) f.get(this));
					} else if (f.getType().equals(Float.class)) {
						res.put(f.getName(), (Float) f.get(this));
					} else if (f.getType().equals(String.class)) {
						res.put(f.getName(), (String) f.get(this));
					} else {
						res.put(f.getName(), f.get(this).toString());
					}
				}
			}
			
			if (withoutPrimaryKey) {
				res.remove(FIELD_ID);
			}
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			if (BuildConfig.DEBUG) {
				Console.loge("toContentValues: " + e, e);
			}
		}
		
		return res;
	}

	public void saveToBundle(Bundle it) {
		try {
			final String prefixName = getClass().getCanonicalName();

			it.putString(prefixName + INTENT_ITEM_NAME, getClass().getName());
			for(Field f : this.getClass().getFields()) {
				f.setAccessible(true);
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				if (f.getType().isPrimitive()) continue;

				DataFieldAnnotation dfa = f.getAnnotation(DataFieldAnnotation.class);
				if (dfa == null) continue;

				if (f.get(this) != null) {
					if (f.getType().equals(Double.class)) {
						it.putDouble(prefixName + f.getName(), (Double) f.get(this));
					} else if (f.getType().equals(Long.class)) {
						it.putLong(prefixName + f.getName(), (Long) f.get(this));
					} else if (f.getType().equals(Integer.class)) {
						it.putInt(prefixName + f.getName(), (Integer) f.get(this));
					} else if (f.getType().equals(Float.class)) {
						it.putFloat(prefixName + f.getName(), (Float) f.get(this));
					} else if (f.getType().equals(String.class)) {
						it.putString(prefixName + f.getName(), (String) f.get(this));
					} else {
						it.putString(prefixName + f.getName(), f.get(this).toString());
					}
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			if (BuildConfig.DEBUG) {
				Console.loge("saveToBundle: " + e, e);
			}
		}
	}

	@Nullable
	@SuppressWarnings("TryWithIdenticalCatches")
	public static <T> T fromBundle(@NonNull Bundle it, @NonNull Class<T> className) {
		T res = null;

		final String prefixName = className.getCanonicalName();
		String itemClassName = it.getString(prefixName + INTENT_ITEM_NAME);
		if (itemClassName == null) return null;
		if (!itemClassName.equals(className.getName())) return null;

		try {
			res = className.newInstance();
			int fieldCount = 0;
			for(Field f : className.getFields()) {
				f.setAccessible(true);
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				if (f.getType().isPrimitive()) continue;
				if (it.containsKey(prefixName + f.getName())) {
					if (f.getType().equals(Double.class)) {
						f.set(res, it.getDouble(prefixName + f.getName(), 0D));
					} else if (f.getType().equals(Long.class)) {
						f.set(res, it.getLong(prefixName + f.getName(), 0L));
					} else if (f.getType().equals(Integer.class)) {
						f.set(res, it.getInt(prefixName + f.getName(), 0));
					} else if (f.getType().equals(Float.class)) {
						f.set(res, it.getFloat(prefixName + f.getName(), 0F));
					} else if (f.getType().equals(String.class)) {
						f.set(res, it.getString(prefixName + f.getName()));
					} else {
						f.set(res, it.getString(prefixName + f.getName()));
					}
					// ignore _id field - all tables has this field
					if (!f.getName().equals(FIELD_ID))
						fieldCount++;
				}
			}
			if (fieldCount == 0)
				res = null;
		} catch (InstantiationException e) {
			if (BuildConfig.DEBUG) {
				Console.loge("fromBundle: " + e, e);
			}
		} catch (IllegalAccessException ignore) {
			if (BuildConfig.DEBUG) {
				Console.loge("fromBundle: " + ignore, ignore);
			}
		}
		return res;
	}

	public void saveToIntent(Intent it) {
		Bundle b = new Bundle();
		saveToBundle(b);
		it.putExtras(b);
	}

	@Nullable
	public static <T> T fromIntent(@NonNull Intent it, @NonNull Class<T> className) {
		if (it.getExtras() != null) {
			return fromBundle(it.getExtras(), className);
		} else {
			return null;
		}
	}

	@NonNull
	public static <T> List<T> fromCursorToList(@NonNull Cursor c, Class<T> className) {
		List<T> res = new ArrayList<>();
		if (c.moveToFirst()) {
			do {
				T item = fromCursor(c, className);
				if (item != null) {
					res.add(item);
				}
			} while (c.moveToNext());
		}
		return res;
	}

	@Nullable
	public static <T> T fromCursor(@NonNull Cursor c, Class<T> className) {
		T res = null;
		if (c.isBeforeFirst() || c.isAfterLast()) {
			ERA.logException(new Exception("Cursor isBeforeFirst or isAfterLast"));
			return null;
		}
		try {

			try {
				res = className.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
				return null;
			}

			for(String col : c.getColumnNames()) {
				int colIdx = c.getColumnIndex(col);
				if (colIdx == -1) continue;
				if (c.isNull(colIdx)) continue;

				Field field = null;
				try {
					field = className.getDeclaredField(col);
				} catch (NoSuchFieldException e) {
					try {
						field = className.getSuperclass().getDeclaredField(col);
					} catch (NoSuchFieldException ignore) {}
				}

				if (field != null) {
					field.setAccessible(true);
					if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) continue;
					if (field.getType().isPrimitive()) continue;

					if (field.getType().equals(Double.class)) {
						field.set(res, c.getDouble(colIdx));
					} else if (field.getType().equals(Long.class)) {
						field.set(res, c.getLong(colIdx));
					} else if (field.getType().equals(Integer.class)) {
						field.set(res, c.getInt(colIdx));
					} else if (field.getType().equals(Float.class)) {
						field.set(res, c.getFloat(colIdx));
					} else if (field.getType().equals(String.class)) {
						field.set(res, c.getString(colIdx));
					} else {
						field.set(res, c.getString(colIdx));
					}
				}
			}

		} catch (IllegalAccessException e) {
			if (BuildConfig.DEBUG) {
				Console.loge("fromCursor: " + e, e);
			}
		} catch (CursorIndexOutOfBoundsException e) {
			if (BuildConfig.DEBUG) {
				Console.loge("fromCursor[CursorIndexOutOfBounds]: " + e, e);
			}
		}

		return res;
	}

	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	@Override
	public String toString() {
		return toContentValues().toString();
	}

	public String toStringAllFields() {
		return toContentValues(false, true).toString();
	}
}
