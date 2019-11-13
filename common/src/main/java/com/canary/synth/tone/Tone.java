package com.canary.synth.tone;

public abstract class Tone {
	public double volumeFactor;
	
	protected Tone(int volume) {
		volumeFactor = volume / 255.0;
	}
	protected Tone(double volumeFactor) {
        this.volumeFactor = volumeFactor;
    }
	
	public abstract double getValue(double time, double fraction);
}