package com.canary.synth;

import java.util.List;

import com.canary.io.Image;
import com.canary.synth.tone.Tone;

public class Synthesizer implements com.canary.io.AudioSource {

    private Score score = null;

    public Synthesizer(Image image) {
        if (image == null) score = null;
        else score = ImageFormat.load(image);
    }

    ///// SCORE TO AUDIO

    @Override
    public double getValue(double time) {
        double result = 0;

        double timeRelativeToChord = time * score.getNotesPerSecond();
        int chordIndex = (int)(timeRelativeToChord) % score.getNotes().size();
        double chordFraction = timeRelativeToChord % 1;

        List<Tone> chord = score.getNotes().get(chordIndex);
        for (Tone note : chord) {
            result += note.getValue(time, chordFraction);
        }

        return AlgUtils.enforceBounds(score.getVolume() * result);
    }

    @Override
    public double getDuration() {
        if (score == null) {
            return 0.0;
        }

        return score.getNotes().size() / score.getNotesPerSecond();
    }

    public double getNotesPerSecond() {
        return score.getNotesPerSecond();
    }

}
