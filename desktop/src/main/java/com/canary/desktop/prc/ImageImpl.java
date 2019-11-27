package com.canary.desktop.prc;

import java.awt.image.BufferedImage;

import com.canary.io.EditableImage;

public class ImageImpl implements EditableImage {

    private BufferedImage img;

    public ImageImpl(BufferedImage img) {
        this.img = img;
    }

    @Override
    public int getWidth() {
        return img.getWidth();
    }

    @Override
    public int getHeight() {
        return img.getHeight();
    }

    @Override
    public int getRGB(int x, int y) {
        return img.getRGB(x, y);
    }

    @Override
    public void setRGB(int x, int y, int value) {
        img.setRGB(x, y, value);
    }
}
