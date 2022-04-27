package com.breezedevs.shopmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.breezedevs.shopmobile.databinding.FragmentMenuBinding;


public class FragmentMenu extends FragmentClass {

    private FragmentMenuBinding _b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _b = FragmentMenuBinding.inflate(inflater, container, false);
        _b.llcheckqtyreserve.setOnClickListener(this);
        _b.lltransfertooffice.setOnClickListener(this);
        return _b.getRoot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llcheckqtyreserve:
                Intent intentCheckQuantity = new Intent(getContext(), ActivityCheckQuantity.class);
                startActivity(intentCheckQuantity);
                break;
            case R.id.lltransfertooffice:
                break;
        }
    }
}