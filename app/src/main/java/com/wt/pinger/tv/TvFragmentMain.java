package com.wt.pinger.tv;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wt.pinger.R;

/**
 * Created by kenumir on 01.07.2017.
 *
 */

public class TvFragmentMain extends VerticalGridFragment {

	private static final int GRID_ITEM_WIDTH = 200;
	private static final int GRID_ITEM_HEIGHT = 200;

	private ArrayObjectAdapter mRowsAdapter;

	private ArrayObjectAdapter mAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.app_name));

		VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
		gridPresenter.setNumberOfColumns(4);
		setGridPresenter(gridPresenter);

		mAdapter = new ArrayObjectAdapter(new CardPresenter());

		mAdapter.add("+");
		mAdapter.add("Aaaaaa");
		mAdapter.add("Aaaaaa");
		mAdapter.add("Aaaaaa");
		mAdapter.add("192.168.1.100");
		mAdapter.add("wp.pl");
		mAdapter.add("fg fgdg");
		mAdapter.add("Aaa");

		setAdapter(mAdapter);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);



		//setHeadersState(HEADERS_ENABLED);
		//setHeadersTransitionOnBackEnabled(true);
		/*

		mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

		CardPresenter cardPresenter = new CardPresenter();
		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
		listRowAdapter.add("+");
		listRowAdapter.add("192.168.100.1");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");
		listRowAdapter.add("wp.pl");

		mRowsAdapter.add(new ListRow(new HeaderItem(0, "Lista Ping"), listRowAdapter));*/

		/*
		GridItemPresenter mGridPresenter = new GridItemPresenter();
		ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
		gridRowAdapter.add("Opcja 1");
		gridRowAdapter.add("Opcja 2");
		mRowsAdapter.add(new ListRow(new HeaderItem(0, "Ustawienia"), gridRowAdapter));
		*/

		//setAdapter(mRowsAdapter);
	}

	private class GridItemPresenter extends Presenter {
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent) {
			TextView view = new TextView(parent.getContext());
			view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			//view.setBackgroundColor(getResources().getColor(R.color.default_background));
			view.setTextColor(Color.WHITE);
			view.setGravity(Gravity.CENTER);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, Object item) {
			((TextView) viewHolder.view).setText((String) item);
		}

		@Override
		public void onUnbindViewHolder(ViewHolder viewHolder) {
		}
	}
}
