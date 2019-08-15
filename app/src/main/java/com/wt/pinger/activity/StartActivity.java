package com.wt.pinger.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.firebase.perf.metrics.AddTrace;
import com.hivedi.era.ERA;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.wt.pinger.App;
import com.wt.pinger.R;

public class StartActivity extends AppCompatActivity implements App.OnAppReady {

    private ProgressWheel progress;

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
    @AddTrace(name = "StartActivity_onCreate")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ERA.log("StartActivity.onCreate:begin");
        mApp = (App) getApplication();
        setContentView(R.layout.activity_start);
        progress = findViewById(R.id.progress_wheel);
        ERA.log("StartActivity.onCreate:end");
    }

    @Override
    protected void onPause() {
        ERA.log("StartActivity.onPause");
        progress.removeCallbacks(startRun);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Answers.getInstance().logContentView(
                new ContentViewEvent()
                        .putContentId("start-activity")
                        .putContentName("Start Activity")
                        .putContentType("activity")
        );
        ERA.log("StartActivity.onResume");
        progress.spin();
        progress.post(startRun);
    }

    @Override
    public void onBackPressed() {
        ERA.log("StartActivity.onBackPressed");
        // nop
    }

    @Override
    public void onAppReady() {
        progress.post(startRun);
    }
}
