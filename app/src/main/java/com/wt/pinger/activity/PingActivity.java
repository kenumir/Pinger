package com.wt.pinger.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.res.ResourcesCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.perf.metrics.AddTrace;
import com.hivedi.era.ERA;
import com.wt.pinger.R;
import com.wt.pinger.extra.SimpleCursorRecyclerAdapter;
import com.wt.pinger.extra.SimpleViewHolder;
import com.wt.pinger.proto.BaseActivity;
import com.wt.pinger.proto.ItemProto;
import com.wt.pinger.providers.PingContentProvider;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.providers.data.PingItem;
import com.wt.pinger.utils.DateTime;
import com.wt.pinger.utils.SystemCompat;

import java.util.Locale;


public class PingActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

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
        toolbar.setNavigationOnClickListener(v -> {
            Intent back = NavUtils.getParentActivityIntent(this);
            if (back != null) {
                startActivity(back);
                finish();
            }
        });
        toolbar.setNavigationContentDescription(R.string.label_back);
        fabAction.setOnClickListener(view -> {
            getPingManager().startStopPingWorker(PingActivity.this, mAddressItem);
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
        //recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        getSupportLoaderManager().initLoader(1, null, this);
        ERA.log("PingActivity.onCreate:end");
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, PingContentProvider.URI_CONTENT, null, null, new String[]{mAddressItem._id.toString()}, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        placeholder.setVisibility(data == null || data.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Nullable
    @Override
    public AddressItem getAddressItem() {
        return mAddressItem;
    }

    @Override
    public void onWorkerStatusUpdate(boolean isWorking) {
        super.onWorkerStatusUpdate(isWorking);
        updatePlayButton(isWorking);
    }

    private void updatePlayButton(boolean isWorking) {
        fabAction.setIconResource(isWorking ? R.drawable.ic_stop_white_32dp : R.drawable.ic_play_arrow_white_32dp);
        fabAction.setText(isWorking ? R.string.label_stop : R.string.label_start);
    }

    @Override
    public void onBackPressed() {
        Intent back = NavUtils.getParentActivityIntent(this);
        if (back != null) {
            startActivity(back);
            finish();
        }
    }
}
