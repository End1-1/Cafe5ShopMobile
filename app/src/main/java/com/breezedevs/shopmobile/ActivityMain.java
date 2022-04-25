package com.breezedevs.shopmobile;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.breezedevs.shopmobile.databinding.ActivityMainBinding;
import com.breezedevs.shopmobile.databinding.ActivitySettingsBinding;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityMain extends Activity {

    private ConnectTask mConnectTask;
    private int mPointAnimate = 0;
    private ActivityMainBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        _b.txtConnecting.setText(getString(R.string.connecting_to_server) + "   ");
        _b.btnConfig.setOnClickListener(this);
    }

    @SuppressWarnings("deprecation")
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mConnectTask = new ConnectTask();
        new Timer().schedule(mConnectTask, 1000, 500);

        if (isServiceRunning(AppService.class)) {
            Intent intent = new Intent(MessageMaker.BROADCAST_DATA);
            intent.putExtra("local", true);
            intent.putExtra("type", MessageList.check_connection);
            intent.putExtra("request", true);
            LocalBroadcastManager.getInstance(ActivityMain.this).sendBroadcast(intent);
        } else {
            Intent srvLocation = new Intent(ActivityMain.this, AppService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(srvLocation);
            } else {
                startService(srvLocation);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConfig:
                openConfig();
                break;
        }
    }

    private void openConfig() {
        Intent intent = new Intent(this, ActivitySettings.class);
        startActivity(intent);
    }

    private class ConnectTask extends TimerTask {
        @Override
        public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPointAnimate++;
                        StringBuilder points = new StringBuilder("   ");
                        for (int i = 0; i < mPointAnimate % 4; i++) {
                            points.setCharAt(i, '.');
                        }
                        _b.txtConnecting.setText(getString(R.string.connecting_to_server) + points);
                    }
                });
            }
    };
}