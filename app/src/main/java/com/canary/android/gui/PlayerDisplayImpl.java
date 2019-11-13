package com.canary.android.gui;

import android.util.Log;
import android.widget.HorizontalScrollView;

import com.canary.io.PlayerDisplay;

/**
 * Created on 1/20/2017.
 */

public class PlayerDisplayImpl implements PlayerDisplay {

    HorizontalScrollView scroll;
    int pixelSize = 0;

    public PlayerDisplayImpl(HorizontalScrollView scroll) {
        this.scroll = scroll;
    }

    public void setPixelSize(int pixelSize) {
        if (pixelSize < 1) {
            throw new IllegalArgumentException("Pixel size must be at least 1");
        }

        this.pixelSize = pixelSize;
    }

    @Override
    public double getNotePosition() {
        if (pixelSize == 0) {
            throw new IllegalStateException("Pixel size not initialized");
        }

        return (double)scroll.getScrollX() / pixelSize;
    }

    @Override
    public void setNotePosition(double position) {
        if (pixelSize == 0) {
            throw new IllegalStateException("Pixel size not initialized");
        }

        //Log.d("notePos", position + " " + (int)(position * pixelSize));

        scroll.smoothScrollTo((int)(position * pixelSize), 0);
    }
}
