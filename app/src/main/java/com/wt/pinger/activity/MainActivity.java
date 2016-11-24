package com.wt.pinger.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.wt.pinger.R;
import com.wt.pinger.fragment.AddressFragment;
import com.wt.pinger.fragment.ConsoleFragment;
import com.wt.pinger.fragment.MoreFragment;
import com.wt.pinger.fragment.MyIPFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) AHBottomNavigation tabs;

    private final Fragment[] mainFragments = new Fragment[]{
            new AddressFragment(), new ConsoleFragment(), new MyIPFragment(), new MoreFragment()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        List<AHBottomNavigationItem> items = new ArrayList<>();
        items.add(new AHBottomNavigationItem(R.string.menu_address_list, R.drawable.ic_list_white_24dp, R.color.colorPrimary));
        items.add(new AHBottomNavigationItem(R.string.menu_cmd, R.drawable.ic_rate_review_white_24dp, R.color.colorPrimary));
        items.add(new AHBottomNavigationItem(R.string.menu_info, R.drawable.ic_info_outline_white_24dp, R.color.colorPrimary));
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
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainFrame, mainFragments[position])
                            .commit();
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
        }
    }
}
