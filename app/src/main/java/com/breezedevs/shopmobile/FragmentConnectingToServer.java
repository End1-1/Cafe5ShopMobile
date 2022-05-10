package com.breezedevs.shopmobile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.breezedevs.shopmobile.databinding.FragmentConnectingToServerBinding;

import java.util.Timer;
import java.util.TimerTask;


public class FragmentConnectingToServer extends FragmentClass {

    private FragmentConnectingToServerBinding _b;
    private ConnectTask mConnectTask;
    private String mText;

    public static FragmentConnectingToServer create(String s) {
        FragmentConnectingToServer fragment = new FragmentConnectingToServer();
        Bundle args = new Bundle();
        args.putString("text", s);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentConnectingToServerBinding.inflate(inflater, container, false);
        return _b.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getArguments() != null) {
            mText = getArguments().getString("text");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        _b.txtConnectingToServer.setText(mText);
        mConnectTask = new ConnectTask();
        new Timer().schedule(mConnectTask, 1000, 1000);
    }

    @Override
    public void onPause() {
        if (mConnectTask != null) {
            mConnectTask.cancel();
        }
        super.onPause();
    }

    private class ConnectTask extends TimerTask {

        private int mPointAnimate = 0;

        @Override
        public void run() {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getContext() == null) {
                        return;
                    }
                    mPointAnimate++;
                    StringBuilder points = new StringBuilder("   ");
                    for (int i = 0; i < mPointAnimate % 4; i++) {
                        points.setCharAt(i, '.');
                    }
                    _b.txtConnectingToServer.setText(getString(R.string.connecting_to_server) + points);
                }
            });
        }
    };
}