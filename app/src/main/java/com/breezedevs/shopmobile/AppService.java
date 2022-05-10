package com.breezedevs.shopmobile;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AppService extends Service {

    public static final String LOCAL_DATA = "LOCAL_DATA";
    private static final String CHANNEL_ID = "1250013";
    public static boolean mLoginSuccess = false;
    private ServiceSocket mSocketService;

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Парковка")
                    .setContentText("Служба парковки").build();
            startForeground(1, notification);
        } else {

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("socket", false)) {
            System.out.println(String.format("SERVICE RECEIVED TIMEEEE %d", System.currentTimeMillis() - Preference.getLong("op_doc")));
            mSocketService.mMessageBuffer.add(intent.getByteArrayExtra("data"));
            return super.onStartCommand(intent, flags, startId);
        }
        mSocketService = new ServiceSocket();
        mSocketService.startSocketThread();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(MessageMaker.BROADCAST_DATA));
        return START_STICKY;
    }

    public void stopService() {
        mSocketService.stop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println(String.format("ServiceParking message: %d", intent.getShortExtra("type", (short) 0)));
            if (!intent.getBooleanExtra("local", false)) {
                return;
            }
            Intent reply = new Intent(MessageMaker.BROADCAST_DATA);
            switch (intent.getShortExtra("type", (short) 0)) {
                case MessageList.login_status:
                    mLoginSuccess = intent.getBooleanExtra("value", false);
                    break;
            }
        }
    };
}
