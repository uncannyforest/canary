package com.canary.synth.tone;

import com.canary.synth.FakeImage;
import com.canary.synth.ImageFormat;
import com.canary.synth.Score;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChirpTest {

    @Test
    public void v13_bIs0_timbreIsSineWave() {
        FakeImage image = FakeImage.createV13()
                .withNoteWithCharacteristics(0x001FFF, 0x001000, 1, 1) // start chirp
                .withNoteWithCharacteristics(0x001FFF, 0x001800, 2, 2); // end chirp
        Score score = ImageFormat.load(image);

        Tone tone = score.getNotes().get(0).get(0);
        assertTrue("Tone was " + tone.getClass() + " not Chirp",
                tone instanceof Chirp);
        Chirp chirp = (Chirp) tone;

        assertEquals(1/Math.sqrt(2), chirp.timbre.getWaveformValue(0.125), 0.01);
    }

    @Test
    public void v13_bIsFF_timbreIsSquareWave() {
        FakeImage image = FakeImage.createV13()
                .withNoteWithCharacteristics(0x001FFF, 0x0010FF, 1, 1) // start chirp
                .withNoteWithCharacteristics(0x001FFF, 0x0018FF, 2, 2); // end chirp
        Score score = ImageFormat.load(image);

        Tone tone = score.getNotes().get(0).get(0);
        assertTrue("Tone was " + tone.getClass() + " not Chirp",
                tone instanceof Chirp);
        Chirp chirp = (Chirp) tone;

        assertEquals(1.0, chirp.timbre.getWaveformValue(0.01), 0.01);
    }

}