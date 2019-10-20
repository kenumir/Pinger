package com.wt.pinger.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.kenumir.eventclip.EventClip;
import com.wt.pinger.R;
import com.wt.pinger.events.EventNames;
import com.wt.pinger.utils.PicassoUtil;

/**
 * Created by Kenumir on 2016-12-12.
 *
 */

public class ReplaioFragment extends Fragment {

    private ViewPager pager;
    private void openReplaio() {

    }

    public ReplaioFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_replaio, container, false);
        pager = res.findViewById(R.id.pager);
        pager.setAdapter(new ViewPagerAdapter(getActivity()));
        res.findViewById(R.id.replaio_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReplaio();
            }
        });
        res.findViewById(R.id.replaioButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayStore(getActivity(), "com.hv.replaio&referrer=utm_source%3Dkenumir%26utm_medium%3Dpinger");
                EventClip.deliver(EventNames.REPLAIO_AD_CLICKED);
            }
        });
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
        private Context c;

        ViewPagerAdapter(Context ctx) {
            c = ctx;
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

            PicassoUtil.get(c)
                .load(images[position])
                .centerInside()
                .fit()
                .into(img);

            return img;
        }
    }
}
