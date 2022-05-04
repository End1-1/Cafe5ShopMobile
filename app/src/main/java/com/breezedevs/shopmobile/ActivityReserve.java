package com.breezedevs.shopmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;

import com.breezedevs.shopmobile.databinding.ActivityReserveBinding;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ActivityReserve extends ActivityClass {

    private ActivityReserveBinding _b;
    private GoodsQtyClass mGoodsQtyClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityReserveBinding.inflate(getLayoutInflater());
        _b.txtDateFor.setOnClickListener(this);
        _b.btnCancelDateFor.setOnClickListener(this);
        _b.btnSetDateFor.setOnClickListener(this);
        _b.btnCancelReserve.setOnClickListener(this);
        _b.btnSaveReserve.setOnClickListener(this);
        _b.edtQty.addTextChangedListener(mQtyWatcher);
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        _b.dateReserveFor.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        _b.dateReserveFor.setMinDate(date.getTime());
        _b.txtDateCreated.setText(String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
        _b.txtDateFor.setText(String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH) , calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
        mViewExpandCollapse.collapseMenu(_b.clDateFor);
        GoodsQtyClass qtyClass = (GoodsQtyClass) getIntent().getSerializableExtra("goods");
        if (qtyClass == null) {
            finish();
        }
        mGoodsQtyClass = qtyClass;
        DecimalFormat format = new DecimalFormat("0.#");
        _b.txtGoodsName.setText(qtyClass.goodsName);
        _b.txtScancode.setText(qtyClass.scancode);
        _b.txtReserveStoreName.setText(qtyClass.storeName);
        _b.txtInStock.setText(format.format(qtyClass.qty - qtyClass.reserveQty));
        setContentView(_b.getRoot());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtDateFor:
                mViewExpandCollapse.expandMenu(_b.clDateFor);
                break;
            case R.id.btnSetDateFor:
                _b.txtDateFor.setText(dateFromEdit(_b.dateReserveFor));
                mViewExpandCollapse.collapseMenu(_b.clDateFor);
                break;
            case R.id.btnCancelDateFor:
                mViewExpandCollapse.collapseMenu(_b.clDateFor);
                break;
            case R.id.btnCancelReserve:
                finish();
                break;
            case R.id.btnSaveReserve:
                save();
                break;
        }
    }

    private ViewExpandCollapse mViewExpandCollapse = new ViewExpandCollapse(new ViewExpandCollapse.ViewExpandCollapseListener() {
        @Override
        public void beforeExpand() {
            _b.dateReserveFor.setVisibility(View.VISIBLE);
            _b.btnCancelDateFor.setVisibility(View.VISIBLE);
            _b.btnSetDateFor.setVisibility(View.VISIBLE);
        }

        @Override
        public void beforeCollapse() {

        }

        @Override
        public void expanded(int id) {

        }

        @Override
        public void collapsed(int id) {
            _b.dateReserveFor.setVisibility(View.GONE);
            _b.btnCancelDateFor.setVisibility(View.GONE);
            _b.btnSetDateFor.setVisibility(View.GONE);
        }
    });

    private String dateFromEdit(DatePicker dp) {
        return String.format("%02d/%02d/%d", dp.getDayOfMonth(), dp.getMonth() + 1, dp.getYear());
    }

    private void save() {
        if (_b.edtQty.getText().toString().isEmpty()) {
            return;
        }
        if (Double.valueOf(_b.edtQty.getText().toString()) < 0.01) {
            return;
        }
        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
        messageMaker.putString("rwshop");
        messageMaker.putString(Preference.getString("server_database"));
        messageMaker.putByte((byte) 4);
        messageMaker.putString(_b.txtDateFor.getText().toString());
        messageMaker.putInteger(mGoodsQtyClass.goods);
        messageMaker.putInteger(mGoodsQtyClass.store);
        messageMaker.putInteger(Integer.valueOf(Preference.getString("server_storecode")));
        messageMaker.putDouble(Double.valueOf(_b.edtQty.getText().toString()));
        messageMaker.putString(_b.edtMsg.getText().toString());
        messageMaker.putString(mGoodsQtyClass.goodsName);
        messageMaker.putString(mGoodsQtyClass.scancode);
        messageMaker.putString(mGoodsQtyClass.unit);
        sendMessage(messageMaker);
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
                    case 4:
                        finish();
                        break;
                }
                break;
        }
    }

    private TextWatcher mQtyWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (_b.edtQty.getText().toString().isEmpty()) {
                return;
            }
            if (Double.valueOf(_b.edtQty.getText().toString()) > Double.valueOf(_b.txtInStock.getText().toString())) {
                _b.edtQty.setText(_b.txtInStock.getText().toString());
            }
        }
    };
}