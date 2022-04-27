package com.breezedevs.shopmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.breezedevs.shopmobile.databinding.ActivityCheckQuantityBinding;
import com.google.android.material.textfield.TextInputEditText;

public class ActivityCheckQuantity extends ActivityClass {

    private ActivityCheckQuantityBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityCheckQuantityBinding.inflate(getLayoutInflater());
        _b.llBack.setOnClickListener(this);
        _b.btnScancode.setOnClickListener(this);
        _b.edtScancode.setOnKeyListener(editListener);
        setContentView(_b.getRoot());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.btnScancode:
                readScancode();
                break;
        }
    }


    @Override
    protected void messageHandler(Intent intent) {
        if (intent.getBooleanExtra(MessageMaker.NETWORK_ERROR, false)) {
            return;
        }
        switch (intent.getShortExtra("type", (short) 0)) {
            case MessageList.dll_op:
                byte[] data = intent.getByteArrayExtra("data");
                short op = MessageMaker.bytesToShort(data);
                switch (op) {
                    case 1:
                        break;
                }
                break;
        }
    }

    private void readScancode() {
        Intent intent = new Intent(this, ActivityCodeReader.class);
        mCodeResult.launch(intent);
    }

    void checkQuantity() {
        String code = _b.edtScancode.getText().toString();
        if (code.isEmpty()) {
            return;
        }
        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
        messageMaker.putString("rwshop");
        messageMaker.putString(Preference.getString("server_database"));
        messageMaker.putShort((short) 1);
        messageMaker.putString(code);
        sendMessage(messageMaker);
    }

    ActivityResultLauncher<Intent> mCodeResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    if (activityResult.getData() == null) {
                        return;
                    }
                    String code = activityResult.getData().getStringExtra("code");
                    if (code.isEmpty()) {
                        return;
                    }
                    _b.edtScancode.setText(code);
                    checkQuantity();
                }
            });

    View.OnKeyListener editListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                        checkQuantity();
                        break;
                }
            }
            return false;
        }
    };
}