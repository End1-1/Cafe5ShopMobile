package com.breezedevs.shopmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityClass extends AppCompatActivity implements View.OnClickListener {

    protected int mMessageId;
    protected NetworkTimerTask mTimerTask;
    private ProgressDialogClass mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Locale locale = new Locale("hy");
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mSocketData, new IntentFilter(MessageMaker.BROADCAST_DATA));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSocketData);
    }

    @Override
    public void onClick(View view) {

    }

    protected void createProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialogClass(this);
            mProgressDialog.show();
        }
    }

    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    protected void sendMessage(MessageMaker mm) {
        mTimerTask = new NetworkTimerTask(mm);
        mMessageId = mm.send();
        new Timer().schedule(mTimerTask, 10000);
    }

    protected void messageHandler(Intent intent) {

    }

    void replaceFragment(FragmentClass fr) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fr, fr, fr.getClass().toString());
        fragmentTransaction.commit();
    }

    protected void login() {
        if (Preference.getString("server_username").length() > 0 && Preference.getString("server_password").length() > 0) {
            MessageMaker messageMaker = new MessageMaker(MessageList.silent_auth);
            messageMaker.putString(Preference.getString("server_username"));
            messageMaker.putString(Preference.getString("server_password"));
            sendMessage(messageMaker);
        } else {
            Intent loginIntent = new Intent(ActivityClass.this, ActivitySettings.class);
            startActivity(loginIntent);
        }
    }

    private class NetworkTimerTask extends TimerTask {

        public MessageMaker mMessage;

        public NetworkTimerTask(MessageMaker mm) {
            mMessage = mm;
        }

        @Override
        public void run() {
            Intent timeoutIntent = new Intent(MessageMaker.BROADCAST_DATA);
            timeoutIntent.putExtra("local", true);
            timeoutIntent.putExtra("type", mMessage.getType());
            timeoutIntent.putExtra("message_id", mMessage.getMessageId());
            timeoutIntent.putExtra(MessageMaker.NETWORK_ERROR, true);
            LocalBroadcastManager.getInstance(ActivityClass.this).sendBroadcast(timeoutIntent);
        }
    }

    protected BroadcastReceiver mSocketData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println(String.format("Activity message: %d, message id and timer task message id: %d - %d", intent.getShortExtra("type", (short) 0), mMessageId, mTimerTask == null ? 0 : mTimerTask.mMessage.getMessageId()));
            if (mTimerTask != null) {
                if (intent.getBooleanExtra(MessageMaker.NETWORK_ERROR, false)) {
                    mTimerTask.cancel();
                    mTimerTask = null;
                }
                if (mTimerTask != null) {
                    if (mMessageId == mTimerTask.mMessage.getMessageId() && intent.getBooleanExtra("local", false)) {
                        mTimerTask.cancel();
                        mTimerTask = null;
                    }
                }
            }
            if (!intent.getBooleanExtra("local", false)) {
                return;
            }
            switch (intent.getShortExtra("type", (short) 0)) {
                case MessageList.connection:
                    if (intent.getBooleanExtra("value", false)) {
                        login();
                    } else {
                        Intent loginIntent = new Intent(MessageMaker.BROADCAST_DATA);
                        loginIntent.putExtra("local", true);
                        loginIntent.putExtra("type", MessageList.login_status);
                        loginIntent.putExtra("value", false);
                        LocalBroadcastManager.getInstance(ActivityClass.this).sendBroadcast(loginIntent);
                    }
                    messageHandler(intent);
                    break;
                case MessageList.silent_auth:
                    if (intent.getBooleanExtra(MessageMaker.NETWORK_ERROR, false)) {
                        return;
                    }
                    MessageMaker mm = new MessageMaker(MessageList.utils);
                    byte[] data = intent.getByteArrayExtra("data");
                    int reply = mm.getInt(data);
                    if (reply > 0) {
                        Preference.setInt("server_userid", reply);
                        Intent loginSuccess = new Intent(MessageMaker.BROADCAST_DATA);
                        loginSuccess.putExtra("local", true);
                        loginSuccess.putExtra("type", MessageList.login_status);
                        loginSuccess.putExtra("value", true);
                        LocalBroadcastManager.getInstance(ActivityClass.this).sendBroadcast(loginSuccess);
                    } else {
//                        Preference.setString("server_password", "");
//                        Intent loginIntent = new Intent(ActivityClass.this, ActivitySettings.class);
//                        startActivity(loginIntent);
                    }
                    intent.putExtra("login_status", reply);
                    messageHandler(intent);
                    break;
                default:
                    messageHandler(intent);
                    break;
            }
        }
    };
}
