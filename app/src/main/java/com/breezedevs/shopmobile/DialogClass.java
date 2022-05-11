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

    public interface DialogYesNo {
        void yes();
        void no();
    }

    private int mContentId;
    private String mMessage;
    private DialogYesNo mDialogYesNo;

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
        Button btn = findViewById(R.id.btnClose);
        if (btn != null) {
            btn.setOnClickListener(this);
        }
        btn = findViewById(R.id.btnYes);
        if (btn != null) {
            btn.setOnClickListener(this);
        }
        btn = findViewById(R.id.btnNo);
        if (btn != null) {
            btn.setOnClickListener(this);
        }
    }

    public static void error(Context c, String s) {
         DialogClass dc = new DialogClass(c, R.layout.dialog_class_error_ok, s);
         dc.show();
    }

    public static void question(Context c, String s, DialogYesNo d) {
        DialogClass dc = new DialogClass(c, R.layout.dialog_class_question_yesno, s);
        dc.mDialogYesNo = d;
        dc.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnClose:
                dismiss();
                break;
            case R.id.btnYes:
                if (mDialogYesNo != null) {
                    mDialogYesNo.yes();
                }
                dismiss();
                break;
            case R.id.btnNo:
                if (mDialogYesNo != null) {
                    mDialogYesNo.no();
                }
                dismiss();
                break;
        }
    }
}
