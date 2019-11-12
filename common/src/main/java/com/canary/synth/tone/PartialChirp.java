package com.canary.synth.tone;

import com.canary.synth.tone.mod.Timbre;

public class PartialChirp extends Chirp {

    boolean atEnd; // whether the chirp is at end or beginning
    double chirpFraction;
    Note noteComponent; // for the part that's not a chirp

    public PartialChirp(double note, int volume, int pitchCorrection, double initialTime, Timbre timbre, int timeCode) {
        super(note, volume, pitchCorrection, initialTime, timbre);

        this.atEnd = (timeCode & 0x4) == 0x4;
        int fractionCode = atEnd ? 4 - (timeCode & 0x3) : (timeCode & 0x3);
        this.chirpFraction = Math.pow(2, -fractionCode);
    }

    public void finalize(double note, int pitchCorrection, double finalTime, boolean reverse) {
        double totalDuration = finalTime - initialTime;
        if (atEnd) {
            initialTime += chirpFraction * totalDuration;
        } else {
            finalTime -= chirpFraction * totalDuration;
        }

        super.finalize(note, pitchCorrection, finalTime, reverse);

        // this is complicated
        // remember, initialPitch is simply the note the algorithm reached first
        // it's not the beginning pitch if reverse==true
        if (atEnd ^ reverse) {
            noteComponent = new Note(initialPitch, volumeFactor, timbre);
        } else {
            noteComponent = new Note(finalPitch, volumeFactor, timbre);
        }

    }

    public double getValue(double time, double fraction) {
        if ((atEnd && fraction >= 1 - chirpFraction) || (!atEnd && fraction < chirpFraction)) {
            return super.getValue(time, fraction);
        } else {
            return noteComponent.getValue(time, fraction);
        }
    }
}
