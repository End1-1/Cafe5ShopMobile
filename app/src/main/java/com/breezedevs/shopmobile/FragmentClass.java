package com.breezedevs.shopmobile;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FragmentClass extends Fragment implements View.OnClickListener {

    protected ActivityClass mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ActivityClass) {
            mActivity = (ActivityClass) context;
        }
    }

    @Override
    public void onClick(View view) {

    }
}
