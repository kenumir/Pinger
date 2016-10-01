package com.wt.pinger.proto;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;

import java.lang.ref.WeakReference;

/**
 * Created by Kenumir on 2016-09-29.
 *
 */

public class SimpleQueryHandler extends AsyncQueryHandler {

    public interface QueryListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
    }

    private WeakReference<QueryListener> mListener;

    public SimpleQueryHandler(ContentResolver cr, QueryListener l) {
        super(cr);
        setQueryListener(l);
    }

    public SimpleQueryHandler setQueryListener(QueryListener listener) {
        mListener = new WeakReference<>(listener);
        return this;
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        final QueryListener listener = mListener.get();
        if (listener != null) {
            listener.onQueryComplete(token, cookie, cursor);
        } else if (cursor != null) {
            cursor.close();
        }
    }
}
