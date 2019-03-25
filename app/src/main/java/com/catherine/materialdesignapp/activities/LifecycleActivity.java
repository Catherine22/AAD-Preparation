package com.catherine.materialdesignapp.activities;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.catherine.materialdesignapp.R;
import com.catherine.materialdesignapp.utils.LifecycleObserverImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LifecycleActivity extends BaseActivity implements LifecycleOwner {
    private final static String TAG = LifecycleActivity.class.getSimpleName();
    private final static String STATE_TIMESTAMP = "timestamp";
    private long savedTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enable back arrow on the top left area

        getLifecycle().addObserver(new LifecycleObserverImpl());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        long timestamp = System.currentTimeMillis();
        savedInstanceState.putLong(STATE_TIMESTAMP, timestamp);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedTimestamp = savedInstanceState.getLong(STATE_TIMESTAMP);
        Log.e(TAG, String.format("restored saved instance state: %s", stampToDate(savedTimestamp)));
    }

    private String stampToDate(long timeMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }
}