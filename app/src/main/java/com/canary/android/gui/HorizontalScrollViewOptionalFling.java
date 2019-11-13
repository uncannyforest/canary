package com.canary.android.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created on 1/23/2017.
 */

public class HorizontalScrollViewOptionalFling extends HorizontalScrollView {
    boolean flingEnabled = true;

    public HorizontalScrollViewOptionalFling(Context context) {
        super(context);
    }

    public HorizontalScrollViewOptionalFling(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollViewOptionalFling(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void fling(int velocityX) {
        if (flingEnabled) {
            super.fling(velocityX);
        }
    }

    public void setFlingEnabled(boolean flingEnabled) {
        this.flingEnabled = flingEnabled;
    }
}
