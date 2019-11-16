package com.canary.synth.tone;

import com.canary.synth.ImageFormat;
import com.canary.synth.Score;

import org.junit.Test;

import static org.junit.Assert.*;

public class NoteTest {
    @Test
    public void v13_bIs0_sineWave() {
        FakeImage image = FakeImage.createV13().withNoteWithCharacteristics(0x0000FF, 0x000000);
        Score score = ImageFormat.load(image);

        Tone note = score.getNotes().get(0).get(0);
        assertEquals(0.0, note.getValue(0, 0), 0.01);
        assertEquals(1/Math.sqrt(2), note.getValue(0.125/880, 0), 0.01);
        assertEquals(1.0, note.getValue(0.25/880, 0), 0.01);
    }

    @Test
    public void v13_bIsFF_squareWave() {
        FakeImage image = FakeImage.createV13().withNoteWithCharacteristics(0x0000FF, 0x0000FF);
        Score score = ImageFormat.load(image);

        Tone note = score.getNotes().get(0).get(0);
        assertEquals(1.0, note.getValue(0.01/880, 0), 0.01);
        assertEquals(1.0, note.getValue(0.25/880, 0), 0.01);
        assertEquals(-1.0, note.getValue(0.75/880, 0), 0.01);
    }

    @Test
    public void v14_bIs0_sineWave() {
        FakeImage image = FakeImage.createV14().withNoteWithCharacteristics(0x0000FF, 0x000000);
        Score score = ImageFormat.load(image);

        Tone note = score.getNotes().get(0).get(0);
        assertEquals(0.0, note.getValue(0, 0), 0.01);
        assertEquals(1/Math.sqrt(2), note.getValue(0.125/880, 0), 0.01);
        assertEquals(1.0, note.getValue(0.25/880, 0), 0.01);
    }

    @Test
    public void v14_bIs7F_squareWave() {
        FakeImage image = FakeImage.createV14().withNoteWithCharacteristics(0x0000FF, 0x00007F);
        Score score = ImageFormat.load(image);

        Tone note = score.getNotes().get(0).get(0);
        assertEquals(1.0, note.getValue(0.01/880, 0), 0.01);
        assertEquals(1.0, note.getValue(0.25/880, 0), 0.01);
        assertEquals(-1.0, note.getValue(0.75/880, 0), 0.01);
    }

    @Test
    public void v14_bIs80_squareWave() {
        FakeImage image = FakeImage.createV14().withNoteWithCharacteristics(0x0000FF, 0x000080);
        Score score = ImageFormat.load(image);

        Tone note = score.getNotes().get(0).get(0);
        assertEquals(1.0, note.getValue(0, 0), 0.01);
        assertEquals(1.0, note.getValue(0.25/880, 0), 0.01);
        assertEquals(-1.0, note.getValue(0.75/880, 0), 0.01);
    }

    @Test
    public void v14_bIsBF_sawtoothWave() {
        FakeImage image = FakeImage.createV14().withNoteWithCharacteristics(0x0000FF, 0x0000BF);
        Score score = ImageFormat.load(image);

        Tone note = score.getNotes().get(0).get(0);
        assertEquals(1.0, note.getValue(0, 0), 0.01);
        assertEquals(0.5, note.getValue(0.25/880, 0), 0.01);
        assertEquals(-0.5, note.getValue(0.75/880, 0), 0.01);
    }

    @Test
    public void v14_bIsC0_sawtoothWave() {
        FakeImage image = FakeImage.createV14().withNoteWithCharacteristics(0x0000FF, 0x0000C0);
        Score score = ImageFormat.load(image);

        Tone note = score.getNotes().get(0).get(0);
        assertEquals(1.0, note.getValue(0.01/880, 0), 0.1);
        assertEquals(0.5, note.getValue(0.25/880, 0), 0.01);
        assertEquals(-0.5, note.getValue(0.75/880, 0), 0.01);
    }

    @Test
    public void v14_bIsFF_tick() {
        FakeImage image = FakeImage.createV14().withNoteWithCharacteristics(0x0000FF, 0x0000FF);
        Score score = ImageFormat.load(image);

        Tone note = score.getNotes().get(0).get(0);
        assertEquals(1.0, note.getValue(0.001/880, 0), 0.2);
        assertEquals(0.0, note.getValue(0.25/880, 0), 0.01);
        assertEquals(0.0, note.getValue(0.75/880, 0), 0.01);
        assertEquals(-1.0, note.getValue(0.999/880, 0), 0.2);
    }
}