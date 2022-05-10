package com.breezedevs.shopmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.breezedevs.shopmobile.databinding.ActivityEditDocumentBinding;

public class ActivityEditDocument extends ActivityClass {

    private ActivityEditDocumentBinding _b;
    private byte mDocType = 0;
    private String mDocId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityEditDocumentBinding.inflate(getLayoutInflater());
        _b.llBack.setOnClickListener(this);
        StoreSpinnerAdapter adapter = new StoreSpinnerAdapter(this, R.layout.spinner_storages);
        _b.spStoreInput.setAdapter(adapter);
        _b.spStoreOutput.setAdapter(adapter);
        if (getIntent().getBooleanExtra("new", false)) {
            _b.txtTitle.setText(getString(R.string.create_document));
        }
        mDocType = (byte) getIntent().getIntExtra("type", 0);
        mDocId = getIntent().getStringExtra("id");
        switch (mDocType) {
            case 1:
                _b.spStoreOutput.setVisibility(View.GONE);
                _b.txtStoreOutput.setVisibility(View.GONE);
                _b.spStoreInput.setSelection(DataClass.indexOfStoreId(Integer.valueOf(Preference.getString("server_storecode"))));
                break;
            case 2:
                _b.spStoreInput.setVisibility(View.GONE);
                _b.txtStoreInput.setVisibility(View.GONE);
                _b.spStoreOutput.setSelection(DataClass.indexOfStoreId(Integer.valueOf(Preference.getString("server_storecode"))));
                break;
            case 3:
                break;
            default:
                if (mDocId.isEmpty()) {
                    DialogClass.error(this, getString(R.string.invalid_doc_type));
                    finish();
                }
                break;
        }
        _b.btnCreateDocument.setOnClickListener(this);
        _b.btnCreateDocument.setVisibility(mDocId.isEmpty() ? View.VISIBLE : View.GONE);
        setContentView(_b.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mDocId.isEmpty()) {
            createProgressDialog();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.btnCreateDocument:
                createProgressDialog();
                MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
                messageMaker.putString("rwshop");
                messageMaker.putString(Preference.getString("server_database"));
                messageMaker.putByte(DllOp.op_create_document);
                messageMaker.putByte(mDocType);
                messageMaker.putByte((byte) 1);
                messageMaker.putInteger(Integer.valueOf(Preference.getString("server_storecode")));
                messageMaker.putInteger(getStoreIn());
                messageMaker.putInteger(getStoreOut());
                sendMessage(messageMaker);
                break;
        }
    }

    @Override
    protected void messageHandler(Intent intent) {
        dismissProgressDialog();
        if (intent.getBooleanExtra(MessageMaker.NETWORK_ERROR, false)) {
            DialogClass.error(this, getString(R.string.network_error));
            return;
        }
        switch (intent.getShortExtra("type", (short) 0)) {
            case MessageList.dll_op:
                byte[] data = intent.getByteArrayExtra("data");
                MessageMaker mm = new MessageMaker(MessageList.utils);
                byte op = mm.getByte(data);
                switch (op) {
                    case 0:
                    case 1:
                    case 2:
                        DialogClass.error(this, mm.getString(data));
                        break;
                    case DllOp.op_create_document:
                        _b.btnCreateDocument.setVisibility(View.GONE);
                        break;
                }
                break;
        }
    }

    private int getStoreIn() {
        switch (mDocType) {
            case 1:
            case 3:
                return DataClass.idOfIndex(_b.spStoreInput.getSelectedItemPosition());
            case 2:
                return 0;
        }
        return 0;
    }

    private int getStoreOut() {
        switch (mDocType) {
            case 1:
                return 0;
            case 2:
            case 3:
                return DataClass.idOfIndex(_b.spStoreOutput.getSelectedItemPosition());
        }
        return 0;
    }

    private class StoreSpinnerAdapter extends ArrayAdapter<String> {

        public StoreSpinnerAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return spinnerView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return spinnerView(position, convertView, parent);
        }

        @Override
        public int getCount() {
            return DataClass.mStorages.size();
        }

        public View spinnerView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View spinnerStorage = layoutInflater.inflate(R.layout.spinner_storages, parent, false);
            ((TextView) spinnerStorage.findViewById(R.id.spinnertext)).setText(DataClass.mStorages.get(position).name);
            return spinnerStorage;
        }
    }
}