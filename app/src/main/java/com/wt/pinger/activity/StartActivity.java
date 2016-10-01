package com.wt.pinger.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.wt.pinger.App;
import com.wt.pinger.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity implements App.OnAppReady {

    @BindView(R.id.progress_wheel) ProgressWheel progress;

    private final Runnable startRun = new Runnable() {
        @Override
        public void run() {
            if (mApp.isAppReady()) {
                progress.stopSpinning();
                progress.setVisibility(View.INVISIBLE);
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            } else {
                mApp.setOnAppReady(StartActivity.this);
            }
        }
    };
    private App mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (App) getApplication();
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
    }

    @Override
    protected void onPause() {
        progress.removeCallbacks(startRun);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progress.spin();
        progress.post(startRun);
    }

    @Override
    public void onBackPressed() {
        // nop
    }

    @Override
    public void onAppReady() {
        progress.post(startRun);
    }
}
