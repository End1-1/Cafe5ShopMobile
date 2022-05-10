package com.breezedevs.shopmobile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class DialogClass extends Dialog implements View.OnClickListener {

    private int mContentId;
    private String mMessage;

    public DialogClass(@NonNull Context context, int contentId, String message) {
        super(context);
        mContentId = contentId;
        mMessage = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(mContentId);
        ((TextView) findViewById(R.id.txtMessage)).setText(mMessage);
        Button btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(this);
        }
    }

    public static void error(Context c, String s) {
         DialogClass dc = new DialogClass(c, R.layout.dialog_class_error_ok, s);
         dc.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnClose:
                dismiss();
                break;
        }
    }
}
