package com.wt.pinger.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.perf.metrics.AddTrace;
import com.hivedi.era.ERA;
import com.wt.pinger.R;
import com.wt.pinger.proto.StartActivityObserver;
import com.wt.pinger.proto.StartViewModel;

public class StartActivity extends AppCompatActivity {

    //private ProgressWheel progress;

    @Override
    @AddTrace(name = "StartActivity_onCreate")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ERA.log("StartActivity.onCreate:begin");
        setContentView(R.layout.activity_start);
        //progress = findViewById(R.id.progress_wheel);

        StartViewModel model = ViewModelProviders.of(this).get(StartViewModel.class);
        model.getData().observe(this, new StartActivityObserver(this));
        model.getData().setValue(
            new Intent(StartActivity.this, MainActivity.class)
        );

        ERA.log("StartActivity.onCreate:end");
    }

    @Override
    protected void onPause() {
        ERA.log("StartActivity.onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ERA.log("StartActivity.onResume");
    }

    @Override
    public void onBackPressed() {
        ERA.log("StartActivity.onBackPressed");
        // nop
    }

}
