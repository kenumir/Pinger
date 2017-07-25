/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.wt.pinger.tv;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.Presenter;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wt.pinger.R;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
	private static final String TAG = "CardPresenter";

	private static final int CARD_WIDTH = 313;
	private static final int CARD_HEIGHT = 176;
	private static int sSelectedBackgroundColor;
	private static int sDefaultBackgroundColor;
	private Drawable mDefaultCardImage;

	private static void updateCardBackgroundColor(CardView view, boolean selected) {
		int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
		// Both background colors should be set because the view's background is temporarily visible
		// during animations.
		//view.setBackgroundColor(color);
		//view.findViewById(R.id.info_field).setBackgroundColor(color);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		Log.d(TAG, "onCreateViewHolder");

		sDefaultBackgroundColor = 0x00000000; //parent.getResources().getColor(R.color.default_background);
		sSelectedBackgroundColor = 0xFFFF0000; //parent.getResources().getColor(R.color.selected_background);

		CardView itemView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tv_entry, parent, false);
		itemView.setFocusable(true);
		itemView.setFocusableInTouchMode(true);
		itemView.setOnHoverListener(new View.OnHoverListener() {
			@Override
			public boolean onHover(View v, MotionEvent event) {
				return false;
			}
		});
		itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				//updateCardBackgroundColor((CardView) v, hasFocus);
			}
		});
		updateCardBackgroundColor(itemView, false);
		return new ViewHolder(itemView);

		/*mDefaultCardImage = parent.getResources().getDrawable(R.drawable.pinger_banner);

		ImageCardView cardView = new ImageCardView(parent.getContext()) {
			@Override
			public void setSelected(boolean selected) {
				updateCardBackgroundColor(this, selected);
				super.setSelected(selected);
			}
		};

		cardView.setFocusable(true);
		cardView.setFocusableInTouchMode(true);
		updateCardBackgroundColor(cardView, false);
		return new ViewHolder(cardView);*/
	}

	@Override
	public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
		//Movie movie = (Movie) item;
		CardView cv = (CardView) viewHolder.view;
		TextView text1 = (TextView) cv.findViewById(R.id.item_text1);
		if (item instanceof String && item.toString().equals("+")) {
			text1.setText("Add new entry");
			text1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_black_48dp, 0, 0, 0);
		} else {
			text1.setText(item.toString());
			text1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		/*
		ImageCardView cardView = (ImageCardView) viewHolder.view;
		if (item instanceof String && item.toString().equals("+")) {
			cardView.setTitleText("Add new");
			cardView.setContentText("New entry");
			cardView.setBadgeImage(viewHolder.view.getContext().getResources().getDrawable(R.drawable.ic_add_black_48dp));
		} else {
			cardView.setTitleText(item.toString());
		}
		cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
		*/
/*
		Log.d(TAG, "onBindViewHolder");
		if (movie.getCardImageUrl() != null) {
			cardView.setTitleText(movie.getTitle());
			cardView.setContentText(movie.getStudio());
			cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
			Glide.with(viewHolder.view.getContext())
					.load(movie.getCardImageUrl())
					.centerCrop()
					.error(mDefaultCardImage)
					.into(cardView.getMainImageView());
		}*/
	}

	@Override
	public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
		Log.d(TAG, "onUnbindViewHolder");
		//ImageCardView cardView = (ImageCardView) viewHolder.view;
		// Remove references to images so that the garbage collector can free up memory
		//cardView.setBadgeImage(null);
		//cardView.setMainImage(null);
	}
}
