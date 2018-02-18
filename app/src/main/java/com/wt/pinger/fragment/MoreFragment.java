package com.wt.pinger.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.R;
import com.wt.pinger.proto.CheckableRelativeLayout;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.utils.Networking;
import com.wt.pinger.utils.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kenumir on 2016-09-25.
 *
 */

public class MoreFragment extends Fragment {

    @OnClick(R.id.itemRate) void itemRateClick(View v) {
        final String appPackageName = getActivity().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @OnClick(R.id.itemAuthor) void itemAuthorClick(View v) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+Micha%C5%82Szwarc")));
        } catch (android.content.ActivityNotFoundException e) {
            // ignore
        }
    }

    @OnClick(R.id.itemWeb) void itemWebClick(View v) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://pinger.kenumir.pl/")));
        } catch (android.content.ActivityNotFoundException e) {
            // ignore
        }
    }

    @OnClick(R.id.itemLibs) void itemLibsClick(View v) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.open_source_libs)
                .items(R.array.lib_names)
                .typeface(ResourcesCompat.getFont(getActivity(), R.font.medium), ResourcesCompat.getFont(getActivity(), R.font.regular))
                .positiveText(R.string.label_ok)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        try {
                            String[] s = getResources().getStringArray(R.array.lib_urls);
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(s[which])));
                        } catch (android.content.ActivityNotFoundException e) {
                            // ignore
                        }
                    }
                })
                .show();
    }

    @OnClick(R.id.itemSuggest) void itemSuggestClick(View v) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.suggestions)
                .content(R.string.suggestions_desc)
                .typeface(ResourcesCompat.getFont(getActivity(), R.font.medium), ResourcesCompat.getFont(getActivity(), R.font.regular))
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(R.string.suggestions_hint, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.length() > 0) {
                            new AsyncTask<CharSequence, Void, Void>() {
                                @Override
                                protected Void doInBackground(CharSequence... params) {
                                    Networking.postSupport(
                                            params[0].toString(),
                                            Prefs.get(getActivity()).load(Constants.PREF_UUID)
                                    );
                                    return null;
                                }
                            }.execute(input);
                        }
                    }
                }).show();
    }

    @BindView(R.id.text2c) TextView text2c;
    @BindView(R.id.settingRunFromList) CheckableRelativeLayout settingRunFromList;
    @BindView(R.id.settingMemberOldSessions) CheckableRelativeLayout settingMemberOldSessions;

    public MoreFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_more, container, false);
        ButterKnife.bind(this, res);
        text2c.setText(BuildConfig.VERSION_NAME);
        settingRunFromList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                Prefs.getAsync(getActivity(), new Prefs.OnPrefsReady() {
                    @Override
                    public void onReady(Prefs prefs) {
                        prefs.save(Constants.PREF_START_PING_FROM_LIST, isChecked);
                    }
                });
            }
        });
        settingMemberOldSessions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                Prefs.getAsync(getActivity(), new Prefs.OnPrefsReady() {
                    @Override
                    public void onReady(Prefs prefs) {
                        prefs.save(Constants.PREF_MEMBER_OLD_SESSIONS, isChecked);
                    }
                });
            }
        });

        Prefs.getAsync(getActivity(), new Prefs.OnPrefsReady() {
            @Override
            public void onReady(Prefs prefs) {
                if (isAdded()) {
                    settingRunFromList.setChecked(prefs.load(Constants.PREF_START_PING_FROM_LIST, false), true);
                    settingMemberOldSessions.setChecked(prefs.load(Constants.PREF_MEMBER_OLD_SESSIONS, true), true);
                }
            }
        });

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("more-fragment")
                        .putContentName("More Fragment")
                        .putContentType("fragment")
        );
    }
}
