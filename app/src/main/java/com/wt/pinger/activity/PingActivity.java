package com.wt.pinger.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.hivedi.era.ERA;
import com.squareup.otto.Subscribe;
import com.wt.pinger.R;
import com.wt.pinger.extra.SimpleCursorRecyclerAdapter;
import com.wt.pinger.extra.SimpleViewHolder;
import com.wt.pinger.proto.ItemProto;
import com.wt.pinger.proto.SimpleQueryHandler;
import com.wt.pinger.providers.PingContentProvider;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.providers.data.PingItem;
import com.wt.pinger.service.PingService;
import com.wt.pinger.utils.BusProvider;
import com.wt.pinger.utils.DateTime;
import com.wt.pinger.utils.SystemCompat;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static void show(@NonNull Context ctx, @NonNull AddressItem item) {
        Intent it = new Intent(ctx, PingActivity.class);
        item.saveToIntent(it);
        ctx.startActivity(it);
    }

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.subTitle) TextView subTitle;
    @BindView(R.id.app_bar) AppBarLayout app_bar;
    @BindView(R.id.recycler) RecyclerView recycler;
    @BindView(R.id.fabAction) FloatingActionButton fabAction;
    @BindView(R.id.placeholder) View placeholder;

    private SimpleCursorRecyclerAdapter adapter;
    private MenuItem playPauseMenu;
    private AddressItem mAddressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ERA.log("PingActivity.onCreate:begin");
        if (getIntent() == null) {
            finish();
            return;
        }
        mAddressItem = ItemProto.fromIntent(getIntent(), AddressItem.class);
        if (mAddressItem == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_ping);
        ButterKnife.bind(this);

        subTitle.setText(getResources().getString(R.string.label_address, mAddressItem.addres));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopService();
            }
        });

        playPauseMenu = toolbar.getMenu()
                .add(R.string.label_start_stop)
                .setIcon(R.drawable.ic_play_arrow_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        startStopService();
                        return false;
                    }
                });
        playPauseMenu.setVisible(false);
        MenuItemCompat.setShowAsAction(playPauseMenu, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);

        MenuItemCompat.setShowAsAction(toolbar.getMenu().add(R.string.label_share)
                .setIcon(R.drawable.ic_share_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        SimpleQueryHandler qh = new SimpleQueryHandler(getContentResolver(), new SimpleQueryHandler.QueryListener() {
                            @Override
                            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                                if (cursor != null) {
                                    new AsyncTask<Cursor, Void, String>(){
                                        @Override
                                        protected String doInBackground(Cursor... params) {
                                            StringBuilder sb = new StringBuilder();
                                            Cursor cursor = params[0];
                                            final int maxShareSize = 250 * 1024;
                                            if (cursor.moveToFirst()) {
                                                sb.append(getResources().getString(R.string.label_address, mAddressItem.addres)).append("\n");
                                                do {
                                                    PingItem item = ItemProto.fromCursor(cursor, PingItem.class);
                                                    if (item != null) {
                                                        if (item.isDataValid()) {
                                                            sb.append(
                                                                    SystemCompat.toHtml(DateTime.formatTime(PingActivity.this, item.timestamp) + ": " + item.seq + " - " + item.time + "ms")
                                                            ).append("\n");
                                                        } else {
                                                            sb.append(SystemCompat.toHtml(DateTime.formatTime(PingActivity.this, item.timestamp) + ": " + item.info)).append("\n");
                                                        }
                                                    }
                                                    int len = sb.length();
                                                    if (len > maxShareSize) {
                                                        // trim
                                                        sb.setLength(maxShareSize);
                                                        Crashlytics.log("Share length trim from " + len);
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
                                                Toast.makeText(PingActivity.this, R.string.toast_no_data_to_share, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursor);
                                }
                            }
                        });
                        qh.startQuery(0, null, PingContentProvider.URI_CONTENT, null, null, null, null);
                        return false;
                    }
                }), MenuItemCompat.SHOW_AS_ACTION_ALWAYS
        );

        app_bar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int pos = -(appBarLayout.getTotalScrollRange() - toolbar.getMeasuredHeight());
                playPauseMenu.setVisible(pos > verticalOffset);
            }
        });

        adapter = new SimpleCursorRecyclerAdapter(R.layout.item_ping, null, new String[]{PingItem.FIELD_TIME}, new int[]{R.id.ping_text1}) {
            @Override
            public void onBindViewHolder(SimpleViewHolder holder, Cursor cursor) {
                super.onBindViewHolder(holder, cursor);
                PingItem item = ItemProto.fromCursor(cursor, PingItem.class);
                if (item != null) {
                    if (item.isDataValid()) {
                        holder.views[0].setText(
                            SystemCompat.toHtml(
                                    DateTime.formatTime(PingActivity.this, item.timestamp) + ": " +
                                            String.format(Locale.getDefault(), "%04d", item.seq) +
                                            " - <b>" + item.time + "</b>ms"
                            )
                        );
                    } else {
                        holder.views[0].setText(SystemCompat.toHtml(DateTime.formatTime(PingActivity.this, item.timestamp) + ": <b>" + item.info + "</b>"));
                    }
                }
            }
        };
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        getSupportLoaderManager().initLoader(1, null, this);
        ERA.log("PingActivity.onCreate:end");
    }

    private void startStopService() {
        PingService.startStop(PingActivity.this, mAddressItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ERA.log("PingActivity.onResume:begin");
        BusProvider.getInstance().register(this);
        PingService.check(this);
        ERA.log("PingActivity.onResume:end");
    }

    @Override
    protected void onPause() {
	    try {
		    BusProvider.getInstance().unregister(this);
	    } catch (IllegalArgumentException e) {
		    // ignore
	    }
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, PingContentProvider.URI_CONTENT, null, null, new String[]{mAddressItem._id.toString()}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        placeholder.setVisibility(data == null || data.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @SuppressWarnings("unused") @Subscribe
    public void onEventData(Integer eventId) {
        switch(eventId) {
            case PingService.SERVICE_STATE_IDLE:
                updatePlayButton(false);
                break;
            case PingService.SERVICE_STATE_WORKING:
                updatePlayButton(true);
                break;
        }
    }

    private void updatePlayButton(boolean isWorking) {
        playPauseMenu.setIcon(isWorking ? R.drawable.ic_stop_white_24dp : R.drawable.ic_play_arrow_white_24dp);
        fabAction.setImageResource(isWorking ? R.drawable.ic_stop_white_32dp : R.drawable.ic_play_arrow_white_32dp);
    }
}
