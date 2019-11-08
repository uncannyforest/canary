package com.canary.android.gui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created on 1/23/2017.
 */

public class BitmapDrawableWithNoDensity extends BitmapDrawable {

    int mRealBitmapWidth;
    int mRealBitmapHeight;

    /**
     * bleaaaah
     *
     * this whole class just exists to get around height/width rounding in BitmapDrawable with
     * densities other than 1
     *
     * and this is a mess to accomplish it
     */
    public BitmapDrawableWithNoDensity(Bitmap bitmap) {
        super(null, bitmap);
        computeRealBitmapSize(); // the important part
        setTargetDensity(1); // not needed, but for good measure
    }

    public BitmapDrawableWithNoDensity(java.io.InputStream is) {
        this(BitmapFactory.decodeStream(is));
    }

    @Override
    public int getIntrinsicWidth() {
        return mRealBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mRealBitmapHeight;
    }

    private void computeRealBitmapSize() {
        final Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            mRealBitmapWidth = bitmap.getWidth();
            mRealBitmapHeight = bitmap.getHeight();
        } else {
            mRealBitmapWidth = mRealBitmapHeight = -1;
        }
    }
}
