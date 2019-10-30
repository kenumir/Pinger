package com.wt.pinger.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.firebase.perf.metrics.AddTrace;
import com.hivedi.console.Console;
import com.hivedi.era.ERA;
import com.kenumir.eventclip.EventClip;
import com.wt.pinger.BuildConfig;
import com.wt.pinger.R;
import com.wt.pinger.data.UserSync;
import com.wt.pinger.events.EventNames;
import com.wt.pinger.fragment.AddressFragment;
import com.wt.pinger.fragment.ConsoleFragment;
import com.wt.pinger.fragment.MoreFragment;
import com.wt.pinger.fragment.MyIPFragment;
import com.wt.pinger.fragment.ReplaioFragment;
import com.wt.pinger.proto.BaseActivity;
import com.wt.pinger.proto.Constants;
import com.wt.pinger.utils.Prefs;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements InstallReferrerStateListener {

    private Toolbar toolbar;
    private AHBottomNavigation tabs;

    private final Fragment[] mainFragments = new Fragment[]{
            new AddressFragment(), new ConsoleFragment(), new MyIPFragment(), new ReplaioFragment(), new MoreFragment()
    };

    private boolean saveInstanceStateCalled = false;
    private InstallReferrerClient mReferrerClient;

    @Override
    @AddTrace(name = "MainActivity_onCreate")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ERA.log("MainActivity.onCreate:begin");
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        tabs = findViewById(R.id.tabs);
        setSupportActionBar(toolbar);

        if (BuildConfig.DEBUG) {
            toolbar.getMenu().add("Test Exception").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Crashlytics.getInstance().crash();
                    return false;
                }
            });
        }

        List<AHBottomNavigationItem> items = new ArrayList<>();
        items.add(new AHBottomNavigationItem(R.string.menu_address_list, R.drawable.ic_list_white_24dp, R.color.colorPrimary));
        items.add(new AHBottomNavigationItem(R.string.menu_cmd, R.drawable.ic_rate_review_white_24dp, R.color.colorPrimary));
        items.add(new AHBottomNavigationItem(R.string.menu_info, R.drawable.ic_info_outline_white_24dp, R.color.colorPrimary));
        items.add(new AHBottomNavigationItem(R.string.menu_replaio, R.drawable.replaio_icon_status_bar, R.color.colorPrimary));
        items.add(new AHBottomNavigationItem(R.string.menu_more, R.drawable.ic_menu_white_24dp, R.color.colorPrimary));
        tabs.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        tabs.addItems(items);
        tabs.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        tabs.setAccentColor(0xFFFFFFFF);
        tabs.setInactiveColor(0x99FFFFFF);
        tabs.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (!wasSelected) {
                    /*
                     * prevent error: IllegalStateException: Can not perform this action after onSaveInstanceState
                     * issue: https://github.com/kenumir/Pinger/issues/9
                     */
                    if (!saveInstanceStateCalled) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.mainFrame, mainFragments[position])
                                .commit();
                        EventClip.deliver(EventNames.REPLAIO_TAB_OPENED);
                    }
                }
                return true;
            }
        });

        if (savedInstanceState == null) {
            tabs.setCurrentItem(0);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFrame, mainFragments[0])
                    .commit();
            UserSync.get().saveUser(this);
        }

        mReferrerClient = InstallReferrerClient.newBuilder(this).build();
        try {
            mReferrerClient.startConnection(this);
        } catch (Exception e) {
            ERA.logException(e);
        }

        ERA.log("MainActivity.onCreate:end");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            menu.add("Test exception").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ERA.logException(new RuntimeException("Test error"));
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveInstanceStateCalled = false;
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("main-activity")
                        .putContentName("Main Activity")
                        .putContentType("activity")
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveInstanceStateCalled = true;
        super.onSaveInstanceState(outState);
    }

    public boolean isSaveInstanceStateCalled() {
        return saveInstanceStateCalled;
    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        if (BuildConfig.DEBUG) {
            Console.logi("onInstallReferrerSetupFinished: responseCode=" + responseCode);
        }
        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                ERA.log("onInstallReferrerSetupFinished: responseCode=OK");
                final Context ctx = getApplicationContext();
                try {
                    final ReferrerDetails response = mReferrerClient.getInstallReferrer();
                    if (BuildConfig.DEBUG) {
                        Console.logi("onInstallReferrerSetupFinished: InstallReferrer=" + response.getInstallReferrer());
                    }
                    ERA.log("onInstallReferrerSetupFinished: InstallReferrer=" + response.getInstallReferrer());
                    Prefs.getAsync(ctx, new Prefs.OnPrefsReady() {
                        @Override
                        public void onReady(Prefs prefs) {
                            if (!prefs.load(Constants.PREF_REFERRER_SAVED, false)) {
                                prefs.save(Constants.PREF_REFERRER, response.getInstallReferrer());
                                UserSync.get().saveUser(ctx);
                            }
                        }
                    });
                    mReferrerClient.endConnection();
                } catch (RemoteException e) {
                    ERA.logException(e);
                }
                break;
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                ERA.log("onInstallReferrerSetupFinished: responseCode=FEATURE_NOT_SUPPORTED");
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                ERA.log("onInstallReferrerSetupFinished: responseCode=SERVICE_UNAVAILABLE");
                break;
            default:
                ERA.log("onInstallReferrerSetupFinished: responseCode=" + responseCode + ", response not found");
        }
    }

    @Override
    public void onInstallReferrerServiceDisconnected() {
        if (BuildConfig.DEBUG) {
            Console.logi("onInstallReferrerServiceDisconnected, retry after 5secs");
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && mReferrerClient != null) {
                    if (BuildConfig.DEBUG) {
                        Console.logi("onInstallReferrerServiceDisconnected, retry - startConnection");
                    }
                    ERA.log("onInstallReferrerServiceDisconnected: retry connect");
                    try {
                        mReferrerClient.startConnection(MainActivity.this);
                    } catch (Exception e) {
                        ERA.logException(e);
                    }
                }
            }
        }, 5_000);
    }
}
