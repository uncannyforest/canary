package com.canary.synth.tone;

import com.canary.synth.tone.mod.NoteEnvelope;
import com.canary.synth.tone.mod.Timbre;

public class Note extends Tone {
    public double frequency;
    public Timbre timbre;
    public NoteEnvelope envelope = null;

    /*
     * note, in semitones above baseNote
     * volume, [1,255]
     * pitchCorrection, [0,3] in semitones where 2 = natural
     * timbre, [0,255]
     */
    public Note(double note, int volume, int pitchCorrection, Timbre timbre) {
        super(volume);
        double pitch = note + pitchCorrection - 2;
        frequency = Math.pow(2, pitch/12)*440;
        this.timbre = timbre;
    }

    public Note(double note, int volume, int pitchCorrection, Timbre timbre, NoteEnvelope envelope) {
        this(note, volume, pitchCorrection, timbre);

        if (!envelope.isNoop()) {
            this.envelope = envelope;
        }
    }

    public Note(double pitch, double volumeFactor, Timbre timbre) {
        super(volumeFactor);
        frequency = Math.pow(2, pitch/12)*440;
        this.timbre = timbre;
    }

    public double getValue(double time, double fraction) {
        double value = this.volumeFactor * timbre.getWaveformValue(this.frequency * time);
        if (envelope != null) {
            value *= envelope.getValue(fraction);
        }
        return value;
    }
}