package com.canary.synth.tone;

import com.canary.synth.tone.mod.NoiseEnvelope;

public class Noise extends Tone {
	public double fractionOfDuration;
	public NoiseEnvelope envelope;

	public Noise(int volume, int fractionOfDurationInput, NoiseEnvelope envelope) {
		super(volume);

		this.fractionOfDuration = fractionOfDurationInput / 255.0;
		this.envelope = envelope;
	}
	
	public double getValue(double time, double fraction) {
		return (fraction <= fractionOfDuration)
				? envelope.getValue(fraction / fractionOfDuration) * this.volumeFactor * (2 * Math.random() - 1)
				: 0;
	}
}
