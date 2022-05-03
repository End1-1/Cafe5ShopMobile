package com.breezedevs.shopmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import com.breezedevs.shopmobile.databinding.ActivityReserveBinding;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ActivityReserve extends ActivityClass {

    private ActivityReserveBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityReserveBinding.inflate(getLayoutInflater());
        _b.txtDateFor.setOnClickListener(this);
        _b.btnCancelDateFor.setOnClickListener(this);
        _b.btnSetDateFor.setOnClickListener(this);
        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        _b.dateReserveFor.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), null);
        _b.txtDateCreated.setText(String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
        _b.txtDateFor.setText(String.format("%02d/%02d/%d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
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
        }
    }

    private ViewExpandCollapse mViewExpandCollapse = new ViewExpandCollapse(new ViewExpandCollapse.ViewExpandCollapseListener() {
        @Override
        public void expanded(int id) {

        }

        @Override
        public void collapsed(int id) {

        }
    });

    private String dateFromEdit(DatePicker dp) {
        return String.format("%02d/%02d/%d", dp.getDayOfMonth(), dp.getMonth(), dp.getYear());
    }
}