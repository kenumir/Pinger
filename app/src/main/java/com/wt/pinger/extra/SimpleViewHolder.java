package com.wt.pinger.extra;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.wt.pinger.proto.ItemTouchHelperViewHolder;

/**
 * Created by Kenumir on 2016-08-21.
 *
 */
public class SimpleViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

    public TextView[] views;
    public View itemView;

    public SimpleViewHolder (View itemView, int[] to)
    {
        super(itemView);
        this.itemView = itemView;
        views = new TextView[to.length];
        for(int i = 0 ; i < to.length ; i++) {
            views[i] = (TextView) itemView.findViewById(to[i]);
        }
    }

    @Override
    public void onItemSelected() {

    }

    @Override
    public void onItemClear() {

    }
}
