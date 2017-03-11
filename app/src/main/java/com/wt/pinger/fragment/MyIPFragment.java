package com.wt.pinger.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.hivedi.era.ERA;
import com.wt.pinger.R;
import com.wt.pinger.providers.NetworkInfoProvider;
import com.wt.pinger.providers.data.NetworkInfo;
import com.wt.pinger.utils.SystemCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Kenumir on 2016-08-11.
 *
 */
public class MyIPFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.ip_swipe) SwipeRefreshLayout swipe;
    @BindView(R.id.ip_list) ListView list;
    @BindView(R.id.ip_placeholder) LinearLayout placeholder;

    private SimpleCursorAdapter adapter;
    public MyIPFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_my_ip, container, false);
        ButterKnife.bind(this, res);
        adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.item_my_ip,
                null,
                new String[]{"name"},
                new int[]{R.id.ip_text1},
                0
        ){
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                final NetworkInfo ni = NetworkInfo.fromCursor(cursor);
                if(ni != null) {
                    TextView text1 = (TextView) view.findViewById(R.id.ip_text1);
                    TextView text2 = (TextView) view.findViewById(R.id.ip_text2);

                    if (ni.name.startsWith("wlan")) {
                        text1.setText(R.string.label_wifi);
                    } else if (ni.name.contains("rmnet")) {
                        text1.setText(R.string.label_mobile);
                    } else {
                        text1.setText(ni.name);
                    }

                    text2.setText(ni.getIP());
                }
            }
        };
        list.setAdapter(adapter);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(1, null, MyIPFragment.this);
            }
        });
        return res;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(1, null, this);
        registerForContextMenu(list);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (adapter != null) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Cursor c = (Cursor) adapter.getItem(info.position);
            final NetworkInfo ni = NetworkInfo.fromCursor(c);
            if (ni != null) {
                final boolean hasIP = ni.getIP() != null && ni.getIP().length() > 0;
                final boolean hasIP4 = ni.ipv4 != null && ni.ipv4.length() > 0;
                final boolean hasIP6 = ni.ipv6 != null && ni.ipv6.length() > 0;

                if (hasIP) {
                    menu.add(R.string.label_copy).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            SystemCompat.copyToClipboard(getActivity(), ni.getIP());
                            return false;
                        }
                    });
                }
                if (hasIP4) {
                    menu.add(R.string.label_copy_ip4).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            SystemCompat.copyToClipboard(getActivity(), ni.ipv4);
                            return false;
                        }
                    });
                }
                if (hasIP6) {
                    menu.add(R.string.label_copy_ip6).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            SystemCompat.copyToClipboard(getActivity(), ni.ipv6);
                            return false;
                        }
                    });
                }
                if (hasIP) {
                    menu.add(R.string.label_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            try {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, ni.getIP());
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                            } catch (ActivityNotFoundException e) {
                                ERA.logException(e);
                            }
                            return false;
                        }
                    });
                }
                if (hasIP4 || hasIP6) {
                    menu.add(R.string.label_ip_info).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            String ip = "https://db-ip.com/" + (hasIP4 ? ni.ipv4 : ni.ipv6);
                            if (!SystemCompat.openInBrowser(getActivity(), ip)) {
                                Toast.makeText(getActivity(), R.string.toast_no_default_browser, Toast.LENGTH_LONG).show();
                            }
                            return false;
                        }
                    });
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        swipe.setRefreshing(true);
        return new CursorLoader(getActivity(), NetworkInfoProvider.URI_CONTENT, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        swipe.setRefreshing(false);
        placeholder.setVisibility(data == null || data.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        swipe.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("my-ip-fragment")
                        .putContentName("My IP Fragment")
                        .putContentType("fragment")
        );
    }
}
