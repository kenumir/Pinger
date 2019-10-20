package com.wt.pinger.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.perf.metrics.AddTrace;
import com.hivedi.era.ERA;
import com.squareup.otto.Subscribe;
import com.wt.pinger.R;
import com.wt.pinger.extra.SimpleCursorRecyclerAdapter;
import com.wt.pinger.extra.SimpleViewHolder;
import com.wt.pinger.proto.ItemProto;
import com.wt.pinger.providers.PingContentProvider;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.providers.data.PingItem;
import com.wt.pinger.service.PingService;
import com.wt.pinger.utils.BusProvider;
import com.wt.pinger.utils.DateTime;
import com.wt.pinger.utils.SystemCompat;

import java.util.Locale;


public class PingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static void show(@NonNull Context ctx, @NonNull AddressItem item) {
        Intent it = new Intent(ctx, PingActivity.class);
        item.saveToIntent(it);
        ctx.startActivity(it);
    }

    private Toolbar toolbar;
    private TextView subTitle;
    private RecyclerView recycler;
    private ExtendedFloatingActionButton fabAction;
    private View placeholder;
    private CollapsingToolbarLayout toolbar_layout;

    private SimpleCursorRecyclerAdapter adapter;
    private AddressItem mAddressItem;

    @Override
    @AddTrace(name = "PingActivity_onCreate")
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

        toolbar = findViewById(R.id.toolbar);
        subTitle = findViewById(R.id.subTitle);
        recycler = findViewById(R.id.recycler);
        fabAction = findViewById(R.id.fabAction);
        placeholder = findViewById(R.id.placeholder);
        toolbar_layout = findViewById(R.id.toolbar_layout);

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
        toolbar_layout.setCollapsedTitleTypeface(ResourcesCompat.getFont(this, R.font.regular));
        toolbar_layout.setExpandedTitleTypeface(ResourcesCompat.getFont(this, R.font.regular));

        adapter = new SimpleCursorRecyclerAdapter(R.layout.item_ping, null, new String[]{PingItem.FIELD_TIME}, new int[]{R.id.ping_text1}) {
            @Override
            public void onBindViewHolder(SimpleViewHolder holder, Cursor cursor) {
                super.onBindViewHolder(holder, cursor);
                PingItem item = ItemProto.fromCursor(cursor, PingItem.class);
                if (item != null) {
	                /*
	                 * display formats:
	                 * [HH:mm:ss]: [seq] - [ping]ms
	                 * [HH:mm:ss]: [seq] - Error info
	                 * [HH:mm:ss]: Error info
	                 */
                    if (item.isDataValid()) {
                        holder.views[0].setText(
                            SystemCompat.toHtml(
                                    DateTime.formatTime(PingActivity.this, item.timestamp) + ": " +
                                            String.format(Locale.getDefault(), "%04d", item.seq) +
                                            " - <b>" + item.time + "</b>ms"
                            )
                        );
                    } else {
	                    Spanned span;
                        if (item.seq != null) {
	                        if (item.seq == 0) {
		                        span = SystemCompat.toHtml(
		                        	DateTime.formatTime(PingActivity.this, item.timestamp) + ": " + String.format(Locale.getDefault(), "%04d", item.seq) + " - <font color='blue'><b>" + item.info + "</b></font>"
		                        );
	                        } else {
		                        span = SystemCompat.toHtml(
				                        DateTime.formatTime(PingActivity.this, item.timestamp) + ": " + String.format(Locale.getDefault(), "%04d", item.seq) + " - <font color='red'><b>" + item.info + "</b></font>"
		                        );
	                        }
                        } else {
	                        span = SystemCompat.toHtml(DateTime.formatTime(PingActivity.this, item.timestamp) + ": <font color='blue'><b>" + item.info + "</b></font>");
                        }
	                    holder.views[0].setText(span);
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
    protected void onStart() {
        super.onStart();
        ERA.log("PingActivity.onStart");
        BusProvider.getInstance().register(this);
        PingService.check(this);
    }

    @Override
    protected void onStop() {
        ERA.log("PingActivity.onStop");
        BusProvider.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ERA.log("PingActivity.onResume");
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("ping-activity")
                        .putContentName("Ping Activity")
                        .putContentType("activity")
        );
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
        fabAction.setIcon(ContextCompat.getDrawable(this, isWorking ? R.drawable.ic_stop_white_32dp : R.drawable.ic_play_arrow_white_32dp));
        fabAction.setText(isWorking ? R.string.label_stop : R.string.label_stop);
    }
}
