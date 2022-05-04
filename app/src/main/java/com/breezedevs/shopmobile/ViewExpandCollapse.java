package com.breezedevs.shopmobile;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

public class ViewExpandCollapse {

    public interface ViewExpandCollapseListener {
        void beforeExpand();
        void beforeCollapse();
        void expanded(int id);
        void collapsed(int id);
    }

    private ViewExpandCollapseListener mViewExpandCollapseListener;

    public ViewExpandCollapse(ViewExpandCollapseListener listener) {
        mViewExpandCollapseListener = listener;
    }

    protected void expandMenu(final View v) {
        if (mViewExpandCollapseListener != null) {
            mViewExpandCollapseListener.beforeExpand();
        }
        //v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        final int h = v.getMeasuredHeight();

        ValueAnimator anim = ValueAnimator.ofInt(1, h);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (mViewExpandCollapseListener != null) {
                    mViewExpandCollapseListener.expanded(v.getId());
                }
            }
        });
        anim.start();
    }

    protected void collapseMenu(final View v) {
        if (mViewExpandCollapseListener != null) {
            mViewExpandCollapseListener.beforeCollapse();
        }
        final int h = 1;

        ValueAnimator anim = ValueAnimator.ofInt(v.getMeasuredHeight(), h);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(200);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mViewExpandCollapseListener != null) {
                    mViewExpandCollapseListener.collapsed(v.getId());
                }
            }
        });
        anim.start();
    }
}
