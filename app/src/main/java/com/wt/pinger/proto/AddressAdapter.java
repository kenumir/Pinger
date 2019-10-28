package com.wt.pinger.proto;

import android.database.Cursor;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.wt.pinger.R;
import com.wt.pinger.activity.PingActivity;
import com.wt.pinger.extra.SimpleCursorRecyclerAdapter;
import com.wt.pinger.extra.SimpleViewHolder;
import com.wt.pinger.providers.data.AddressItem;
import com.wt.pinger.utils.Prefs;

/**
 * Created by Kenumir on 2016-08-22.
 *
 */
public class AddressAdapter extends SimpleCursorRecyclerAdapter implements ItemTouchHelperAdapter {

    public interface OnItemClick {
        void onClick(AddressItem item);
    }

    private FragmentActivity mContext;
    private SimpleViewHolder dragHolder;
    private ItemTouchHelper mItemTouchHelper;
    private OnItemClick mOnItemClick;

    public AddressAdapter(@NonNull FragmentActivity ctx) {
        super(R.layout.item_address, null, new String[]{AddressItem.FIELD_DISPLAY_NAME, AddressItem.FIELD_PINGS}, new int[]{R.id.address_text1, R.id.address_text2});
        mContext = ctx;
        setHasStableIds(true);
    }

    public void setOnItemClick(OnItemClick o) {
        mOnItemClick = o;
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder holder, Cursor cursor) {
        super.onBindViewHolder(holder, cursor);
        holder.itemView.setVisibility(View.VISIBLE);
        final AddressItem item = ItemProto.fromCursor(cursor, AddressItem.class);
        if (item != null) {
            holder.views[0].setText(item.display_name != null && item.display_name.length() > 0 ? item.display_name : item.addres);

            String secText = item.packet != null && item.packet > 0 ? Integer.toString(item.packet) + " " + mContext.getResources().getString(R.string.label_bytes) : mContext.getResources().getString(R.string.label_default_packet);
            if (item.pings != null && item.pings > 0) {
                secText += " / " + mContext.getResources().getString(R.string.label_pings, item.pings);
            }
            if (item.interval != null && item.interval > 0) {
                secText += " / " + mContext.getResources().getString(R.string.label_interval, item.interval);
            }
            holder.views[1].setText(secText);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item != null) {
                    Prefs.getAsync(mContext, new Prefs.OnPrefsReady() {
                        @Override
                        public void onReady(Prefs prefs) {
                            if (prefs.load(Constants.PREF_START_PING_FROM_LIST, false)) {
                                //PingService.startStop(mContext, item);
                                if (mContext instanceof BaseActivity) {
                                    ((BaseActivity) mContext).getPingManager().startStopPingWorker(mContext, item);
                                }
                            } else {
                                PingActivity.show(mContext, item);
                            }
                        }
                    });
                }
            }
        });
        /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //mDragStartListener.onStartDrag(holder);
                dragHolder = holder;
                if (mItemTouchHelper != null) {
                    mItemTouchHelper.startDrag(dragHolder);
                }
                return true;
            }
        });*/
        holder.itemView.findViewById(R.id.address_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClick != null) {
                    mOnItemClick.onClick(item);
                }
            }
        });
    }

    public void onItemIdle() {
        //onItemDismiss(dragHolder.getAdapterPosition());
        //dragHolder.itemView.setVisibility(View.GONE);
        dragHolder = null;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        notifyItemRemoved(position);
    }

    public void setItemTouchHelper(ItemTouchHelper d) {
        mItemTouchHelper = d;
    }
}
