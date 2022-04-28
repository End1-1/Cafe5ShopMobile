package com.breezedevs.shopmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.breezedevs.shopmobile.databinding.ActivitySettingsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivitySettings extends ActivityClass {

    private ActivitySettingsBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        _b.llBack.setOnClickListener(this);
        _b.btnReadFromQr.setOnClickListener(this);
        _b.edtServerAddress.setText(Preference.getString("server_address"));
        _b.edtServerPort.setText(Preference.getString("server_port"));
        _b.edtUsername.setText(Preference.getString("server_username"));
        _b.edtPassword.setText(Preference.getString("server_password"));
        _b.edtDatabase.setText(Preference.getString("server_database"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Preference.setString("server_address", _b.edtServerAddress.getText().toString());
        Preference.setString("server_port", _b.edtServerPort.getText().toString());
        Preference.setString("server_username", _b.edtUsername.getText().toString());
        Preference.setString("server_password", _b.edtPassword.getText().toString());
        Preference.setString("server_database", _b.edtDatabase.getText().toString());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.btnReadFromQr:
                readSettingsFromQr();
                break;
        }
    }

    private void readSettingsFromQr() {
        Intent intent = new Intent(this, ActivityCodeReader.class);
        mCodeResult.launch(intent);
    }

    ActivityResultLauncher<Intent> mCodeResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    if (activityResult.getData() == null) {
                        return;
                    }
                    String code = activityResult.getData().getStringExtra("code");
                    List<String> params = Arrays.asList(code.split(";"));
                    if (params.size() == 5) {
                        _b.edtServerAddress.setText(params.get(0));
                        _b.edtServerPort.setText(params.get(1));
                        _b.edtUsername.setText(params.get(2));
                        _b.edtPassword.setText(params.get(3));
                        _b.edtDatabase.setText(params.get(4));
                    }
                }
            });
}