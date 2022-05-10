package com.breezedevs.shopmobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.breezedevs.shopmobile.databinding.ActivityImageBinding;

public class ActivityImage extends ActivityClass {

    private ActivityImageBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityImageBinding.inflate(getLayoutInflater());
        _b.llBack.setOnClickListener(this);
        byte[] b = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(b,0, b.length);
        _b.img.setImageBitmap(bmp);
        _b.txtImageName.setText(getIntent().getStringExtra("name"));
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