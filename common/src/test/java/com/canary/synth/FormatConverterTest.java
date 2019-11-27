package com.canary.synth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormatConverterTest {
    @Test
    public void v13Tov14_doesNotChangeLeftmostRow() {
        FakeImage originalImage = FakeImage.createV13();
        FakeImage image = originalImage.copy();
        FormatConverter.v13ToV14(image);
        assertEquals(originalImage.getRGB(0, 0), image.getRGB(0, 0));
        assertEquals(originalImage.getRGB(0, 1), image.getRGB(0, 1));
        assertEquals(originalImage.getRGB(0, 2), image.getRGB(0, 2));
    }

    @Test
    public void v13Tov14_doesNotChangePercussionRows() {
        FakeImage originalImage = FakeImage.createV13()
                .withNoiseWithCharacteristics(0x00FFFFFF, 0x00FFFFFF, 1, 0)
                .withNoiseWithCharacteristics(0x00FFFFFF, 0x00FFFFFF, 2, 0);
        FakeImage image = originalImage.copy();
        FormatConverter.v13ToV14(image);
        assertEquals(originalImage.getRGB(1, 0), image.getRGB(1, 0));
        assertEquals(originalImage.getRGB(2, 0), image.getRGB(2, 0));
    }

    @Test
    public void v13Tov14_halvesMelodyRowTimbreCodes() {
        FakeImage originalImage = FakeImage.createV13()
                .withNoteWithCharacteristics(0x000000FF, 0x00000000, 1, 1)
                .withNoteWithCharacteristics(0x000000FF, 0x00000001, 2, 1)
                .withNoteWithCharacteristics(0x000000FF, 0x000000FE, 1, 2)
                .withNoteWithCharacteristics(0x000000FF, 0x000000FF, 2, 2);
        FakeImage image = originalImage.copy();
        FormatConverter.v13ToV14(image);
        assertEquals((originalImage.getRGB(1, 1) & 0x00FFFF00) | 0x00000000, image.getRGB(1, 1));
        assertEquals((originalImage.getRGB(2, 1) & 0x00FFFF00) | 0x00000001, image.getRGB(2, 1));
        assertEquals((originalImage.getRGB(1, 2) & 0x00FFFF00) | 0x0000007F, image.getRGB(1, 2));
        assertEquals((originalImage.getRGB(2, 2) & 0x00FFFF00) | 0x00000080, image.getRGB(2, 2));
    }
}
