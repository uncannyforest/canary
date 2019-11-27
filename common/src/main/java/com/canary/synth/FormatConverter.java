package com.canary.synth;

import com.canary.io.EditableImage;

public final class FormatConverter {
    public static boolean canConvertToV14(EditableImage image) {
        return ImageFormat.hasValidFormatColumn(image)
                && ImageFormat.getFormatVersion(image) == 0x13;
    }

    public static void toV14(EditableImage image) {
        if (!canConvertToV14(image)) {
            throw new IllegalArgumentException("Format must be 1.3");
        }
        v13ToV14(image);
    }

    public static void v13ToV14(EditableImage image) {
        int yBoundary = ImageFormat.getYBoundary(image);
        int formatVersion = ImageFormat.getFormatVersion(image, yBoundary);
        if (formatVersion != 0x13) {
            throw new IllegalArgumentException("Format must be 1.3");
        }
        for (int x = 1; x < image.getWidth(); x++) {
            for (int y = yBoundary + 1; y < image.getHeight(); y++) {
                int v13Pixel = image.getRGB(x, y);
                int v13Timbre = v13Pixel & 0x000000FF;
                int v14Timbre = (v13Timbre + 1) / 2; // add one so as to round up
                int v14Pixel = (v13Pixel &  0x00FFFF00) | v14Timbre;
                image.setRGB(x, y, v14Pixel);
            }
        }
        int oldFormatPixel = image.getRGB(0, yBoundary);
        int newFormatPixel = (oldFormatPixel & 0x0000FFFF) | (0x14 << 16);
        image.setRGB(0, yBoundary, newFormatPixel);
    }
}
