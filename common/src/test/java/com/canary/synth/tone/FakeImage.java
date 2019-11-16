package com.canary.synth.tone;

import com.canary.io.Image;

public class FakeImage implements Image {
    private static final int DEFAULT_NOTE = 0xFF8000;

    // [x][y] (note Java literal representation is transposed)
    private int[][] pixels;

    private FakeImage(int[][] pixels) {
        this.pixels = pixels;
    }

    public static FakeImage createV13() {
        int[][] pixels = new int[][]
                {new int[] {0x130001, 0xFF0000, 0x000000},
                 new int[] {0x000000, 0x000000, 0x000000},
                 new int[] {0x000000, 0x000000, 0x000000}};
        return new FakeImage(pixels);
    }

    public static FakeImage createV14() {
        int[][] pixels = new int[][]
                {new int[] {0x140001, 0xFF0000, 0x000000},
                        new int[] {0x000000, 0x000000, 0x000000},
                        new int[] {0x000000, 0x000000, 0x000000}};
        return new FakeImage(pixels);
    }
    /**
     * Places a single note in the Image.  Where testMask is true, uses value specified; otherwise
     * creates default note.
     */
    public FakeImage withNoteWithCharacteristics(int testMask, int value) {
        pixels[1][1] = (DEFAULT_NOTE & ~testMask) | (value & testMask);
        return this;
    }

    /**
     * Places a single note in the Image.  Where testMask is true, uses value specified; otherwise
     * creates default note.
     */
    public FakeImage withNoteWithCharacteristics(int testMask, int value, int xPos, int yPos) {
        pixels[xPos][yPos] = (DEFAULT_NOTE & ~testMask) | (value & testMask);
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
