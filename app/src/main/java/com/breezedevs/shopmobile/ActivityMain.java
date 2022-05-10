package com.breezedevs.shopmobile;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.breezedevs.shopmobile.databinding.ActivityMainBinding;

public class ActivityMain extends ActivityClass {

    private static final int PERMISSION_CAMERA_REQUEST = 1;
    private ActivityMainBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        _b.btnConfig.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST);
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CAMERA_REQUEST:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    @Override
    protected void messageHandler(Intent intent) {
        switch (intent.getShortExtra("type", (short) 0)) {
            case MessageList.connection:
                replaceFragment(FragmentConnectingToServer.create(getString(R.string.connecting_to_server)));
                if (intent.getBooleanExtra("value", false)) {
                    _b.imgStatus.setImageDrawable(getDrawable(R.drawable.wifib));
                } else {
                    _b.imgStatus.setImageDrawable(getDrawable(R.drawable.wifi_off));
                }
                break;
            case MessageList.check_connection:
                if (intent.getBooleanExtra("connected", false)) {
                    _b.imgStatus.setImageDrawable(getDrawable(R.drawable.wifi_on));
                    replaceFragment(new FragmentMenu());
                } else {
                    _b.imgStatus.setImageDrawable(getDrawable(R.drawable.wifib));
                    replaceFragment(FragmentConnectingToServer.create(getString(R.string.connecting_to_server)));
                }
                break;
            case MessageList.silent_auth:
                if (intent.getIntExtra("login_status", 0) == 0) {
                    _b.imgStatus.setImageDrawable(getDrawable(R.drawable.wifib));
                    replaceFragment(FragmentConnectingToServer.create(getString(R.string.connecting_to_server)));
                } else {
                    _b.imgStatus.setImageDrawable(getDrawable(R.drawable.wifi_on));
                    getData();
                }
                break;
            case MessageList.dll_op:
                byte[] data = intent.getByteArrayExtra("data");
                MessageMaker mm = new MessageMaker(MessageList.utils);
                byte op = mm.getByte(data);
                switch (op) {
                    case DllOp.op_data_list:
                        byte lst = mm.getByte(data);
                        switch (lst) {
                            case DllOp.list_storages:
                                int rowcount = mm.getInt(data);
                                DataClass.mStorages.clear();
                                for (int i = 0; i < rowcount; i++) {
                                    DataClass.DataStore ds = new DataClass.DataStore();
                                    ds.id = mm.getInt(data);
                                    ds.name = mm.getString(data);
                                    DataClass.mStorages.add(ds);
                                }
                                break;
                        }
                        break;
                }
                replaceFragment(new FragmentMenu());
                break;
        }
    }

    private void openConfig() {
        Intent intent = new Intent(this, ActivitySettings.class);
        startActivity(intent);
    }

    private void getData() {
        replaceFragment(FragmentConnectingToServer.create(getString(R.string.get_data)));
        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
        messageMaker.putString("rwshop");
        messageMaker.putString(Preference.getString("server_database"));
        messageMaker.putByte(DllOp.op_data_list);
        messageMaker.putByte(DllOp.list_storages);
        sendMessage(messageMaker);
    }
}