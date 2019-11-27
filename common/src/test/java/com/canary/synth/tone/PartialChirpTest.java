package com.canary.synth.tone;

import com.canary.synth.FakeImage;
import com.canary.synth.ImageFormat;
import com.canary.synth.Score;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PartialChirpTest {

    @Test
    public void finalize_chirpAndNoteHaveSameTimbre() {
        // first half is note, second half is chirp
        FakeImage image = FakeImage.createV13()
                .withNoteWithCharacteristics(0x001FFF, 0x001755, 1, 1) // start chirp
                .withNoteWithCharacteristics(0x001FFF, 0x001855, 2, 2); // end chirp
        Score score = ImageFormat.load(image);

        Tone tone = score.getNotes().get(0).get(0);
        assertTrue("Tone was " + tone.getClass() + " not Chirp",
                tone instanceof PartialChirp);
        PartialChirp partialChirp = (PartialChirp) tone;

        assertEquals(partialChirp.timbre, partialChirp.noteComponent.timbre);
    }

    @Test
    public void v13_bIs0_sineWave() {
        // first half is note, second half is chirp
        FakeImage image = FakeImage.createV13()
                .withNoteWithCharacteristics(0x001FFF, 0x001700, 1, 1) // start chirp
                .withNoteWithCharacteristics(0x001FFF, 0x001800, 2, 2); // end chirp
        Score score = ImageFormat.load(image);

        Tone partialChirp = score.getNotes().get(0).get(0);
        assertEquals(0.0, partialChirp.getValue(0, 0), 0.01);
        assertEquals(1/Math.sqrt(2), partialChirp.getValue(0.125/880, 0), 0.01);
        assertEquals(1.0, partialChirp.getValue(0.25/880, 0), 0.01);
    }

    @Test
    public void v13_bIsFF_squareWave() {
        // first half is note, second half is chirp
        FakeImage image = FakeImage.createV13()
                .withNoteWithCharacteristics(0x001FFF, 0x0017FF, 1, 1) // start chirp
                .withNoteWithCharacteristics(0x001FFF, 0x0018FF, 2, 2); // end chirp
        Score score = ImageFormat.load(image);

        Tone partialChirp = score.getNotes().get(0).get(0);
        assertEquals(1.0, partialChirp.getValue(0.01/880, 0), 0.01);
        assertEquals(1.0, partialChirp.getValue(0.25/880, 0), 0.01);
        assertEquals(-1.0, partialChirp.getValue(0.75/880, 0), 0.01);
    }
}