package com.breezedevs.shopmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.breezedevs.shopmobile.databinding.ActivityCheckQuantityBinding;
import com.breezedevs.shopmobile.databinding.ItemCheckQuantityBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ActivityCheckQuantity extends ActivityClass {

    private ActivityCheckQuantityBinding _b;
    private DataAdapter mDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityCheckQuantityBinding.inflate(getLayoutInflater());
        _b.llBack.setOnClickListener(this);
        _b.btnScancode.setOnClickListener(this);
        _b.edtScancode.setOnKeyListener(editListener);
        _b.txtName.setText(getString(R.string.name) + ": ?");
        _b.txtPrice.setText(getString(R.string.price) + ": ?");
        _b.btn34.setOnClickListener(this);
        _b.btn36.setOnClickListener(this);
        _b.btn38.setOnClickListener(this);
        _b.btn40.setOnClickListener(this);
        _b.btn42.setOnClickListener(this);
        _b.btn44.setOnClickListener(this);
        _b.btn46.setOnClickListener(this);
        mDataAdapter = new DataAdapter();
        _b.rv.setLayoutManager(new GridLayoutManager(this, 1));
        _b.rv.setAdapter(mDataAdapter);
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
            case R.id.btn34:
            case R.id.btn36:
            case R.id.btn38:
            case R.id.btn40:
            case R.id.btn42:
            case R.id.btn44:
            case R.id.btn46:
                replaceSize(((Button) view).getText().toString());
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
                MessageMaker mm = new MessageMaker(MessageList.utils);
                byte op = mm.getByte(data);
                switch (op) {
                    case 0:
                    case 1:
                    case 2:
                        System.out.println("ERROR");
                        break;
                    case 3:
                        byte columnCount = mm.getByte(data);
                        int rowCount = mm.getInt(data);
                        //Types
                        List<Byte> columnTypes = new ArrayList<>();
                        for (byte i = 0; i < columnCount; i++) {
                            columnTypes.add(mm.getByte(data));
                        }
                        //Column widths
                        List<Short> columnWidths = new ArrayList<>();
                        for (byte i = 0; i < columnCount; i++) {
                            columnWidths.add(mm.getShort(data));
                        }
                        List<String> columnTitles = new ArrayList<>();
                        for (byte i = 0; i < columnCount; i++) {
                            columnTitles.add(mm.getString(data));
                        }
                        int[] dataMap = new int[rowCount * columnCount];
                        int stop = data.length - (rowCount * columnCount * 4)  - 4;
                        int cc = 1;
                        mm.mPosition = data.length - 4;
                        while (mm.mPosition > stop) {
                            dataMap[(rowCount * columnCount) - cc] = mm.getInt(data);
                            cc++;
                            mm.mPosition = data.length - (cc * 4);
                        }
                        for (int r = 0; r < rowCount; r++) {
                            mm.mPosition = dataMap[(r * columnCount) + 0];
                            OneRow or = new OneRow();
                            or.storeName = mm.getString(data);
                            or.goodsName = mm.getString(data);
                            or.scancode = mm.getString(data);
                            or.unit = mm.getString(data);
                            or.retailPrice = mm.getDouble(data);
                            or.whosalePrice = mm.getDouble(data);
                            or.qty = mm.getDouble(data);
                            or.reserveQty = mm.getDouble(data);
                            or.store = mm.getInt(data);
                            or.goods = mm.getInt(data);
                            mDataAdapter.data.add(or);
                        }
                        mDataAdapter.notifyDataSetChanged();
                        if (mDataAdapter.data.size() > 0) {
                            OneRow or = mDataAdapter.data.get(0);
                            _b.txtName.setText(getString(R.string.name) + ": " + or.goodsName);
                            _b.txtPrice.setText(getString(R.string.price) + ": " + String.valueOf(or.retailPrice));
                        } else {
                            _b.txtName.setText(getString(R.string.name) + ": ?");
                            _b.txtPrice.setText(getString(R.string.price) + ": ?");
                        }
                        break;
                }
                break;
        }
    }

    private void readScancode() {
        Intent intent = new Intent(this, ActivityCodeReader.class);
        mCodeResult.launch(intent);
    }

    private void replaceSize(String sz) {
        String code = sz + _b.edtScancode.getText().toString().substring(2);
        _b.edtScancode.setText(code);
        checkQuantity();
    }

    void checkQuantity() {
        String code = _b.edtScancode.getText().toString();
        if (code.isEmpty()) {
            return;
        }
        mDataAdapter.data.clear();
        mDataAdapter.notifyDataSetChanged();
        _b.txtName.setText(getString(R.string.name) + ": ?");
        _b.txtPrice.setText(getString(R.string.price) + ": ?");
        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
        messageMaker.putString("rwshop");
        messageMaker.putString(Preference.getString("server_database"));
        messageMaker.putByte((byte) 3);
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

    private class OneRow {
        public String storeName;
        public String goodsName;
        public String scancode;
        public String unit;
        public double qty;
        public double reserveQty;
        public double retailPrice;
        public double whosalePrice;
        public int store;
        public int goods;
    }

    private class DataAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

        public List<OneRow> data = new ArrayList<>();
        private DecimalFormat format = new DecimalFormat("0.#");

        @Override
        public void onClick(View view) {
            int pos = _b.rv.getChildAdapterPosition(view);
            Intent reserveIntent = new Intent(ActivityCheckQuantity.this, ActivityReserve.class);
            startActivity(reserveIntent);
            System.out.println(pos);
        }

        private class VH extends RecyclerView.ViewHolder {
            private ItemCheckQuantityBinding _i;

            public VH(ItemCheckQuantityBinding i) {
                super(i.getRoot());
                _i = i;
                _i.getRoot().setOnClickListener(DataAdapter.this);
            }

            public void bind(int index) {
                OneRow or = data.get(index);
                _i.txtStoreName.setText(or.storeName);
                _i.txtQty.setText(format.format(or.qty));
                _i.txtReserved.setText(format.format(or.reserveQty));
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCheckQuantityBinding i = ItemCheckQuantityBinding.inflate(getLayoutInflater());
            return new VH(i);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((VH) holder).bind(position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}