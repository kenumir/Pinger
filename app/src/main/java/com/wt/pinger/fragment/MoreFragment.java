package com.wt.pinger.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.R;
import com.wt.pinger.proto.CheckableRelativeLayout;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.proto.UserTheme;
import com.wt.pinger.utils.Networking;
import com.wt.pinger.utils.Prefs;

/**
 * Created by Kenumir on 2016-09-25.
 *
 */

public class MoreFragment extends Fragment {

    private TextView text2c;
    private CheckableRelativeLayout settingRunFromList;
    private CheckableRelativeLayout settingMemberOldSessions;
    private CheckableRelativeLayout settingTheme;
    private TextView settingThemeValue;

    public MoreFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_more, container, false);

        text2c = res.findViewById(R.id.text2c) ;
        settingRunFromList = res.findViewById(R.id.settingRunFromList);
        settingMemberOldSessions = res.findViewById(R.id.settingMemberOldSessions);
        settingTheme = res.findViewById(R.id.settingTheme);
        settingThemeValue = res.findViewById(R.id.settingThemeValue);

        res.findViewById(R.id.itemRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        res.findViewById(R.id.itemAuthor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+Micha%C5%82Szwarc")));
                } catch (android.content.ActivityNotFoundException e) {
                    // ignore
                }
            }
        });
        res.findViewById(R.id.itemWeb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://pinger.kenumir.pl/")));
                } catch (android.content.ActivityNotFoundException e) {
                    // ignore
                }
            }
        });
        res.findViewById(R.id.itemLibs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
        res.findViewById(R.id.itemSuggest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

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


        settingTheme.setOnClickListener(view -> {
            final Prefs prefs = Prefs.get(getActivity());
            int sel = prefs.loadTheme();
            switch(sel) {
                case UserTheme.DEFAULT: sel = 0; break;
                case UserTheme.FOLLOW_SYSTEM: sel = 1; break;
                case UserTheme.FOLLOW_BATTERY: sel = 1; break;
                case UserTheme.LIGHT: sel = 2; break;
                case UserTheme.DARK: sel = 3; break;
            }

            new MaterialDialog.Builder(getActivity())
                    .title(R.string.settings_theme)
                    .items(
                            getResources().getString(R.string.theme_default),
                            Build.VERSION.SDK_INT >= 29 ?
                                    getResources().getString(R.string.theme_follow_system) :
                                    getResources().getString(R.string.theme_battery),
                            getResources().getString(R.string.theme_light),
                            getResources().getString(R.string.theme_dark)
                    )
                    .itemsCallbackSingleChoice(sel, (dialog, itemView, which, text) -> {
                        int mode = Build.VERSION.SDK_INT >= 29 ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
                        switch(which) {
                            case 0:
                                prefs.saveTheme(UserTheme.DEFAULT);
                                mode = Build.VERSION.SDK_INT >= 29 ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
                                break;
                            case 1:
                                if (Build.VERSION.SDK_INT >= 29) {
                                    prefs.saveTheme(UserTheme.FOLLOW_SYSTEM);
                                } else {
                                    prefs.saveTheme(UserTheme.FOLLOW_BATTERY);
                                }
                                mode = Build.VERSION.SDK_INT >= 29 ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
                                break;
                            case 2:
                                prefs.saveTheme(UserTheme.LIGHT);
                                mode = AppCompatDelegate.MODE_NIGHT_NO;
                                break;
                            case 3:
                                prefs.saveTheme(UserTheme.DARK);
                                mode = AppCompatDelegate.MODE_NIGHT_YES;
                                break;
                        }
                        int themeName = R.string.theme_default;
                        switch(prefs.loadTheme()) {
                            case UserTheme.FOLLOW_SYSTEM:
                                themeName = R.string.theme_follow_system;
                                break;
                            case UserTheme.FOLLOW_BATTERY:
                                themeName = R.string.theme_battery;
                                break;
                            case UserTheme.LIGHT:
                                themeName = R.string.theme_light;
                                break;
                            case UserTheme.DARK:
                                themeName = R.string.theme_dark;
                                break;
                        }
                        settingThemeValue.setText(themeName);
                        AppCompatDelegate.setDefaultNightMode(mode);
                        return false;
                    })
                    .build().show();
        });

        Prefs.getAsync(getActivity(), new Prefs.OnPrefsReady() {
            @Override
            public void onReady(Prefs prefs) {
                if (isAdded()) {
                    settingRunFromList.setChecked(prefs.load(Constants.PREF_START_PING_FROM_LIST, false), true);
                    settingMemberOldSessions.setChecked(prefs.load(Constants.PREF_MEMBER_OLD_SESSIONS, true), true);
                    int themeName = R.string.theme_default;
                    switch(prefs.loadTheme()) {
                        case UserTheme.FOLLOW_SYSTEM:
                            themeName = R.string.theme_follow_system;
                            break;
                        case UserTheme.FOLLOW_BATTERY:
                            themeName = R.string.theme_battery;
                            break;
                        case UserTheme.LIGHT:
                            themeName = R.string.theme_light;
                            break;
                        case UserTheme.DARK:
                            themeName = R.string.theme_dark;
                            break;
                    }
                    settingThemeValue.setText(themeName);
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
