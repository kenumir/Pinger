package com.wt.pinger.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.squareup.otto.Subscribe;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.R;
import com.wt.pinger.proto.SimpleQueryHandler;
import com.wt.pinger.providers.CmdContentProvider;
import com.wt.pinger.service.CmdService;
import com.wt.pinger.utils.BusProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kenumir on 2016-08-11.
 *
 */
public class ConsoleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.cmd_edit) EditText edit;
    @BindView(R.id.cmd_list) ListView list;
    @BindView(R.id.cmd_placeholder) LinearLayout placeholder;
    @BindView(R.id.cmdBtn) ImageView cmdBtn;

    private SimpleCursorAdapter adapter;

    public ConsoleFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_console, container, false);
        ButterKnife.bind(this, res);
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.item_cmd, null, new String[]{"data"}, new int[]{R.id.cmd_item}, 0);
        list.setAdapter(adapter);
        cmdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.setSelectionAfterHeaderView();
                cmdBtn.setImageResource(R.drawable.ic_clear_black_24dp);
                CmdService.executeCmd(getActivity(), edit.getText().toString());
            }
        });
        edit.setText("");
        TextKeyListener.clear(edit.getText());
        return res;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItemCompat.setShowAsAction(
            menu.add(R.string.label_share).setIcon(R.drawable.ic_share_white_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (isAdded()) { // <- fox NPE on getActivity()
                        SimpleQueryHandler qh = new SimpleQueryHandler(getActivity().getContentResolver(), new SimpleQueryHandler.QueryListener() {
                            @Override
                            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                                if (cursor != null) {
                                    new AsyncTask<Cursor, Void, String>() {
                                        @Override
                                        protected String doInBackground(Cursor... params) {
                                            StringBuilder sb = new StringBuilder();
                                            Cursor cursor = params[0];
                                            final int maxShareSize = 250 * 1024;
                                            if (cursor.moveToFirst()) {
                                                do {
                                                    sb.append(cursor.getString(cursor.getColumnIndex("data")));
                                                    sb.append("\n");

                                                    int len = sb.length();
                                                    if (len > maxShareSize) {
                                                        // trim
                                                        sb.setLength(maxShareSize);
                                                        ERA.log("Share length trim from " + len);
                                                        ERA.logException(new Exception("Share length trim from " + len));
                                                        break;
                                                    }
                                                } while (cursor.moveToNext());
                                            }
                                            cursor.close();
                                            return sb.toString();
                                        }

                                        @Override
                                        protected void onPostExecute(String s) {
                                            if (s.length() > 0) {
                                                try {
                                                    Intent sendIntent = new Intent();
                                                    sendIntent.setAction(Intent.ACTION_SEND);
                                                    sendIntent.putExtra(Intent.EXTRA_TEXT, s);
                                                    sendIntent.setType("text/plain");
                                                    startActivity(sendIntent);
                                                } catch (ActivityNotFoundException e) {
                                                    ERA.logException(e);
                                                }
                                            } else {
                                                Toast.makeText(getActivity(), R.string.toast_no_data_to_share, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursor);
                                } else {
                                    Toast.makeText(getActivity(), R.string.toast_no_data_to_share, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        qh.startQuery(0, null, CmdContentProvider.URI_CONTENT, null, null, null, null);
                    }
                    return false;
                }
            }), MenuItemCompat.SHOW_AS_ACTION_ALWAYS
        );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("console-fragment")
                        .putContentName("Console Fragment")
                        .putContentType("fragment")
        );
        BusProvider.getInstance().register(this);
        CmdService.checkService(getActivity());
    }

    @Override
    public void onPause() {
        BusProvider.getInstance().unregister(this);
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), CmdContentProvider.URI_CONTENT, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        placeholder.setVisibility(data != null && data.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    @Subscribe @SuppressWarnings("unused")
    public void serviceMessages(CmdService.CmdServiceMessage msg) {
        if (BuildConfig.DEBUG) {
            Console.logi("serviceMessages " + msg.type + ", data=" + msg.data);
        }
        switch(msg.type) {
            case CmdService.CMD_MSG_CHECK:
                boolean isWorking = msg.getDataAsBool(false);
                cmdBtn.setImageResource(!isWorking ? R.drawable.ic_send_black_24dp : R.drawable.ic_clear_black_24dp);
                break;
        }
    }
}
