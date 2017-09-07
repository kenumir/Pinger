package com.wt.pinger.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.firebase.perf.metrics.AddTrace;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) AHBottomNavigation tabs;

    private final Fragment[] mainFragments = new Fragment[]{
            new AddressFragment(), new ConsoleFragment(), new MyIPFragment(), new ReplaioFragment(), new MoreFragment()
    };

    private boolean saveInstanceStateCalled = false;

    @Override
    @AddTrace(name = "MainActivity_onCreate")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ERA.log("MainActivity.onCreate:begin");
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

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
                    /**
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
}
