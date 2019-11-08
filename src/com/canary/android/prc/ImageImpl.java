package com.canary.android.prc;

import android.graphics.Bitmap;

import com.canary.io.Image;

/**
 * Created on 1/12/2017.
 */

public class ImageImpl implements Image {

    private Bitmap bmp;

    public ImageImpl(Bitmap bmp) {
        this.bmp = bmp;
    }

    @Override
    public int getWidth() {
        return bmp.getWidth();
    }

    @Override
    public int getHeight() {
        return bmp.getHeight();
    }

    @Override
    public int getRGB(int x, int y) {
        return bmp.getPixel(x, y);
    }
}
