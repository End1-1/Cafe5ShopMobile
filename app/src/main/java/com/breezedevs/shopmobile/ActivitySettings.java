package com.breezedevs.shopmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.breezedevs.shopmobile.databinding.ActivitySettingsBinding;

public class ActivitySettings extends Activity {

    private ActivitySettingsBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        _b.llBack.setOnClickListener(this);
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