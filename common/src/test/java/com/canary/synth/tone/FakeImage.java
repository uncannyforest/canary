package com.canary.synth.tone;

import com.canary.io.Image;

public class FakeImage implements Image {
    // [x][y] (note Java literal representation is transposed)
    private int[][] pixels;

    private FakeImage(int[][] pixels) {
        this.pixels = pixels;
    }

    public static FakeImage createV13() {
        int[][] pixels = new int[][]
                {new int[] {0x130001, 0xFF0000},
                 new int[] {0x000000, 0x000000}};
        return new FakeImage(pixels);
    }

    /**
     * Places a single note in the Image.  Where testMask is true, uses value specified; otherwise
     * creates default note.
     */
    public FakeImage withNoteWithCharacteristics(int testMask, int value) {
        int defaultNote = 0xFF8000;
        pixels[1][1] = (defaultNote & ~testMask) | (value & testMask);
        return this;
    }

    @Override
    public int getWidth() {
        return pixels.length;
    }

    @Override
    public int getHeight() {
        return pixels[0].length;
    }

    @Override
    public int getRGB(int x, int y) {
        return pixels[x][y];
    }
}
