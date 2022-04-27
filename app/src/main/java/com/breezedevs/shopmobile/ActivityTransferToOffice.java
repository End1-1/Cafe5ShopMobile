package com.breezedevs.shopmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.breezedevs.shopmobile.databinding.ActivityTransferToOfficeBinding;

public class ActivityTransferToOffice extends ActivityClass {

    private ActivityTransferToOfficeBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityTransferToOfficeBinding.inflate(getLayoutInflater());
        _b.llBack.setOnClickListener(this);
        setContentView(_b.getRoot());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llBack:
                finish();
                break;
        }
    }
}