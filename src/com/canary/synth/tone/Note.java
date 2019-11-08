package com.canary.synth.tone;

import com.canary.synth.AlgUtils;
import com.canary.synth.tone.mod.NoteEnvelope;

public class Note extends Tone {
	public double frequency;
	public double timbreFactor;
	public NoteEnvelope envelope = null;
	
	/*
	 * note, in semitones above baseNote
	 * volume, [1,255]
	 * pitchCorrection, [0,3] in semitones where 2 = natural
	 * timbre, [0,255]
	 */
	public Note(double note, int volume, int pitchCorrection, int timbre) {
        super(volume);
		double pitch = note + pitchCorrection - 2;
		frequency = Math.pow(2, pitch/12)*440;

		// higher timbre value makes more like square wave
		// 0 is sine wave
		// I really would like to come up with a better algorithm for intermediate values,
		//		but this will have to do for now
		timbreFactor = 256.0 / (256 - timbre);
	}

    public Note(double note, int volume, int pitchCorrection, int timbre, NoteEnvelope envelope) {
        this(note, volume, pitchCorrection, timbre);

        if (!envelope.isNoop()) {
            this.envelope = envelope;
        }
    }

    public Note(double pitch, double volumeFactor, double timbreFactor) {
        super(volumeFactor);
        frequency = Math.pow(2, pitch/12)*440;
        this.timbreFactor = timbreFactor;
    }

    public double getValue(double time, double fraction) {
        double value = this.volumeFactor * AlgUtils.enforceBounds(this.timbreFactor * Math.sin(AlgUtils.TAU * this.frequency * time));
        if (envelope != null) {
            value *= envelope.getValue(fraction);
        }
        return value;
	}
}
