package com.canary.desktop.prc;

import java.awt.image.BufferedImage;

import com.canary.io.Image;

public class ImageImpl implements Image {

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
}
