package com.breezedevs.shopmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.breezedevs.shopmobile.databinding.ActivityEditDocumentBinding;
import com.breezedevs.shopmobile.databinding.ItemDocumentGoodsBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ActivityEditDocument extends ActivityClass {

    private ActivityEditDocumentBinding _b;
    private byte mDocType = 0;
    private String mDocId;
    private DocAdapter mDocAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityEditDocumentBinding.inflate(getLayoutInflater());
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        _b.txtCreateDate.setText(String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
        _b.btnScan.setOnClickListener(this);
        _b.editScancode.setOnKeyListener(editListener);
        _b.llBack.setOnClickListener(this);
        _b.rv.setLayoutManager(new GridLayoutManager(this, 1));
        mDocAdapter = new DocAdapter();
        _b.rv.setAdapter(mDocAdapter);
        StoreSpinnerAdapter adapter = new StoreSpinnerAdapter(this, R.layout.spinner_storages);
        _b.spStoreInput.setAdapter(adapter);
        _b.spStoreOutput.setAdapter(adapter);
        _b.spStoreInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(String.format("1. onItemSelected: %d - %d", i, l));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        _b.spStoreOutput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println(String.format("2. onItemSelected: %d - %d", i, l));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (getIntent().getBooleanExtra("new", false)) {
            _b.txtTitle.setText(getString(R.string.create_document));
        }
        mDocType = (byte) getIntent().getIntExtra("type", 0);
        mDocId = getIntent().getStringExtra("id");
        setStoreFieldVisibility();
        _b.btnCreateDocument.setOnClickListener(this);
        _b.btnCreateDocument.setVisibility(mDocId.isEmpty() ? View.VISIBLE : View.GONE);
        setContentView(_b.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mDocId.isEmpty()) {
            createProgressDialog();
            MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
            messageMaker.putString("rwshop");
            messageMaker.putString(Preference.getString("server_database"));
            messageMaker.putByte(DllOp.op_open_document);
            messageMaker.putString(mDocId);
            sendMessage(messageMaker);
        }
    }

    @Override
    protected void onPause() {
        updateDocument();
        super.onPause();
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
            case R.id.btnScan:
                Intent intent = new Intent(this, ActivityCodeReader.class);
                mCodeResult.launch(intent);
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
                byte success;
                switch (op) {
                    case 0:
                    case 1:
                    case 2:
                        DialogClass.error(this, mm.getString(data));
                        break;
                    case DllOp.op_create_document:
                        mDocId = mm.getString(data);
                        _b.btnCreateDocument.setVisibility(View.GONE);
                        _b.editScancode.setEnabled(true);
                        break;
                    case DllOp.op_open_document:
                        success = mm.getByte(data);
                        if (success == 0) {
                            String error = mm.getString(data);
                            DialogClass.error(this, error);
                            return;
                        }
                        createProgressDialog();
                        mDocType = mm.getByte(data);
                        int storein = mm.getInt(data);
                        int storeout = mm.getInt(data);
                        _b.txtCreateDate.setText(mm.getString(data));
                        _b.spStoreInput.setSelection(DataClass.indexOfStoreId(storein));
                        _b.spStoreOutput.setSelection(DataClass.indexOfStoreId(storeout));
                        _b.editScancode.setEnabled(true);
                        setStoreFieldVisibility();
                        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
                        messageMaker.putString("rwshop");
                        messageMaker.putString(Preference.getString("server_database"));
                        messageMaker.putByte(DllOp.op_open_body);
                        messageMaker.putString(mDocId);
                        sendMessage(messageMaker);
                        break;
                    case DllOp.op_open_body:
                        success = mm.getByte(data);
                        if (success == 0) {
                            String error = mm.getString(data);
                            DialogClass.error(this, error);
                            return;
                        }
                        int count = mm.getInt(data);
                        mDocAdapter.mGoods.clear();
                        for (int i = 0; i < count; i++) {
                            GoodsRow gr = new GoodsRow();
                            gr.rowId = mm.getString(data);
                            gr.goodsName = mm.getString(data);
                            gr.scancode = mm.getString(data);
                            gr.qty = mm.getDouble(data);
                            gr.price = mm.getDouble(data);
                            mDocAdapter.mGoods.add(gr);
                        }
                        mDocAdapter.notifyDataSetChanged();
                        break;
                    case DllOp.op_add_goods_to_document:
                        success = mm.getByte(data);
                        _b.editScancode.setText("");
                        if (success == 0) {
                            String error = mm.getString(data);
                            DialogClass.error(this, error);
                            return;
                        }
                        GoodsRow gr = new GoodsRow();
                        gr.rowId = mm.getString(data);
                        gr.goodsName = mm.getString(data);
                        gr.scancode = mm.getString(data);
                        gr.qty = mm.getDouble(data);
                        mDocAdapter.mGoods.add(0, gr);
                        mDocAdapter.notifyDataSetChanged();
                        break;
                    case DllOp.op_remove_goods_from_document:
                        success = mm.getByte(data);
                        if (success == 0) {
                            String error = mm.getString(data);
                            DialogClass.error(this, error);
                            return;
                        }
                        String rowId = mm.getString(data);
                        for (int i = 0; i < mDocAdapter.mGoods.size(); i++) {
                            if ( mDocAdapter.mGoods.get(i).rowId.equals(rowId)) {
                                mDocAdapter.mRowIndex = -1;
                                mDocAdapter.mGoods.remove(i);
                                mDocAdapter.notifyDataSetChanged();
                                return;
                            }
                        }
                        break;
                    case DllOp.op_update_document:
                        success = mm.getByte(data);
                        if (success == 0) {
                            String error = mm.getString(data);
                            DialogClass.error(this, error);
                            return;
                        }
                        break;
                    case DllOp.op_update_goods:
                        success = mm.getByte(data);
                        if (success == 0) {
                            String error = mm.getString(data);
                            DialogClass.error(this, error);
                            return;
                        }
                        String rowid = mm.getString(data);
                        for (int i = 0; i < mDocAdapter.mGoods.size(); i++) {
                            if ( mDocAdapter.mGoods.get(i).rowId.equals(rowid)) {
                                mDocAdapter.mGoods.get(i).qty = mm.getDouble(data);
                                mDocAdapter.notifyDataSetChanged();
                                return;
                            }
                        }
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

    private void addGoods() {
        String scancode = _b.editScancode.getText().toString();
        if (scancode.isEmpty()) {
            return;
        }
        createProgressDialog();
        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
        messageMaker.putString("rwshop");
        messageMaker.putString(Preference.getString("server_database"));
        messageMaker.putByte(DllOp.op_add_goods_to_document);
        messageMaker.putString(mDocId);
        messageMaker.putString(scancode);
        messageMaker.putDouble(1);
        messageMaker.putDouble(0);
        sendMessage(messageMaker);
    }

    private void updateDocument() {
        createProgressDialog();
        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
        messageMaker.putString("rwshop");
        messageMaker.putString(Preference.getString("server_database"));
        messageMaker.putByte(DllOp.op_update_document);
        messageMaker.putString(mDocId);
        int index = _b.spStoreInput.getSelectedItemPosition();
        messageMaker.putInteger(index < 0 ? 0 : DataClass.mStorages.get(index).id);
        index = _b.spStoreOutput.getSelectedItemPosition();
        messageMaker.putInteger(index < 0 ? 0 : DataClass.mStorages.get(index).id);
        sendMessage(messageMaker);
    }

    void setStoreFieldVisibility() {
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
    }

    private ActivityResultLauncher<Intent> mCodeResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
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
                    _b.editScancode.setText(code);
                    addGoods();
                }
            });

    private View.OnKeyListener editListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                        addGoods();
                        break;
                }
            }
            return false;
        }
    };

    private ViewExpandCollapse mViewExpandCollapse = new ViewExpandCollapse(new ViewExpandCollapse.ViewExpandCollapseListener() {
        @Override
        public void beforeExpand() {

        }

        @Override
        public void beforeCollapse() {

        }

        @Override
        public void expanded(int id) {

        }

        @Override
        public void collapsed(int id) {

        }
    });

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

    private class GoodsRow {
        String rowId;
        String goodsName;
        String scancode;
        double qty;
        double price;
    }

    private class DocAdapter extends RecyclerView.Adapter {

        public List<GoodsRow> mGoods;
        private int mRowIndex = -1;

        public DocAdapter() {
            mGoods = new ArrayList();
        }

        private class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ItemDocumentGoodsBinding _i;

            public VH(ItemDocumentGoodsBinding i) {
                super(i.getRoot());
                _i = i;
                _i.getRoot().setOnClickListener(this);
                _i.btnRemove.setOnClickListener(this);
                _i.btnQty.setOnClickListener(this);
            }

            public void onBind(int position) {
                GoodsRow gr = mGoods.get(position);
                _i.txtGoodsName.setText(gr.goodsName);
                _i.txtScancode.setText(gr.scancode);
                _i.txtQty.setText(Preference.formatDouble(gr.qty));
                if (mRowIndex == position) {
                    _i.getRoot().setBackgroundColor(getColor(R.color.lightblue));
                    _i.btnRemove.setVisibility(View.VISIBLE);
                    _i.btnQty.setVisibility(View.VISIBLE);
                } else {
                    _i.getRoot().setBackgroundColor(getColor(R.color.white));
                    _i.btnRemove.setVisibility(View.GONE);
                    _i.btnQty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnRemove:
                        DialogClass.question(ActivityEditDocument.this, getString(R.string.confirm_to_delete), new DialogClass.DialogYesNo() {
                            @Override
                            public void yes() {
                                GoodsRow gr = mGoods.get(getAdapterPosition());
                                createProgressDialog();
                                MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
                                messageMaker.putString("rwshop");
                                messageMaker.putString(Preference.getString("server_database"));
                                messageMaker.putByte(DllOp.op_remove_goods_from_document);
                                messageMaker.putString(gr.rowId);
                                sendMessage(messageMaker);
                            }

                            @Override
                            public void no() {

                            }
                        });
                        break;
                    case R.id.btnQty:
                        GoodsRow gr = mGoods.get(getAdapterPosition());
                        DialogClass.qty(ActivityEditDocument.this, String.format("%s %s", gr.goodsName, gr.scancode), new DialogClass.DialogQty() {
                            @Override
                            public void qty(double v) {
                                createProgressDialog();
                                MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
                                messageMaker.putString("rwshop");
                                messageMaker.putString(Preference.getString("server_database"));
                                messageMaker.putByte(DllOp.op_update_goods);
                                messageMaker.putString(mDocId);
                                messageMaker.putString(gr.rowId);
                                messageMaker.putDouble(v);
                                sendMessage(messageMaker);
                            }

                            @Override
                            public void no() {

                            }
                        });
                        break;
                    default:
                        mRowIndex = getAdapterPosition();
                        notifyDataSetChanged();
                        break;
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDocumentGoodsBinding i = ItemDocumentGoodsBinding.inflate(getLayoutInflater());
            return new VH(i);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((VH) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return mGoods.size();
        }
    }
}