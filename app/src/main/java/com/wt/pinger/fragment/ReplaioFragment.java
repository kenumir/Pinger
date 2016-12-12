package com.wt.pinger.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wt.pinger.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kenumir on 2016-12-12.
 *
 */

public class ReplaioFragment extends Fragment {

    @OnClick(R.id.replaio_app) void replaio_appClick(View v) {
        openReplaio();
    }

    @OnClick(R.id.replaioAd) void replaioAdClick(View v) {
        openReplaio();
    }

    private void openReplaio() {
        openPlayStore(getActivity(), "com.hv.replaio&referrer=utm_source%3Dkenumir%26utm_medium%3Dpinger");
    }

    public ReplaioFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_replaio, container, false);
        ButterKnife.bind(this, res);
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

}
