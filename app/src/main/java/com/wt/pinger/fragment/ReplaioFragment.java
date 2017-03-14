package com.wt.pinger.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.kenumir.eventclip.EventClip;
import com.squareup.picasso.Picasso;
import com.wt.pinger.R;
import com.wt.pinger.R2;
import com.wt.pinger.events.EventNames;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kenumir on 2016-12-12.
 *
 */

public class ReplaioFragment extends Fragment {

    @BindView(R2.id.pager) ViewPager pager;

    @OnClick(R.id.replaio_app) void replaio_appClick(View v) {
        openReplaio();
    }

    @OnClick(R.id.replaioButton) void replaioButtonClick(View v) {
        openReplaio();
    }

    private void openReplaio() {
        openPlayStore(getActivity(), "com.hv.replaio&referrer=utm_source%3Dkenumir%26utm_medium%3Dpinger");
        EventClip.deliver(EventNames.REPLAIO_AD_CLICKED);
    }

    public ReplaioFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_replaio, container, false);
        ButterKnife.bind(this, res);
        pager.setAdapter(new ViewPagerAdapter(getActivity()));
        return res;
    }

    public static boolean openPlayStore(@NonNull Context ctx, @NonNull String appId) {
        // &referrer=<custom name>
        try {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        } catch (Exception e) {
            try {
                ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appId)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            } catch (Exception e2) {
                // ignore
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("replaio-fragment")
                        .putContentName("Replaio Fragment")
                        .putContentType("fragment")
        );
    }

    private static class ViewPagerAdapter extends PagerAdapter {

        private String[] images = new String[]{
                "file:///android_asset/images/s1.webp",
                "file:///android_asset/images/s2.webp",
                "file:///android_asset/images/s3.webp",
                "file:///android_asset/images/s4.webp",
        };
        private Picasso mPicasso;

        ViewPagerAdapter(Context ctx) {
            mPicasso = new Picasso.Builder(ctx)
                    //.downloader(new OkHttp3Downloader(new OkHttpClient.Builder().build()))
                    .build();
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }


        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final ImageView img = new ImageView(container.getContext());
            container.addView(img);

            mPicasso
                .load(images[position])
                .centerInside()
                .fit()
                .into(img);

            return img;
        }
    }
}
