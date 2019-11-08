package com.canary.synth.tone;

import com.canary.synth.AlgUtils;

public class Chirp extends Tone {
	public double timbreFactor;
	
	public double initialPitch;
	public double initialTime;

	public double finalPitch;
    public double finalTime;
	
	public double chirpRate;
	public double yOffset;
	
	/*
	 * note, in semitones above baseNote
	 * volume, [1,255]
	 * pitchCorrection, [0,3] in semitones where 2 = natural
	 * timbre, [0,255]
	 */
	public Chirp(double note, int volume, int pitchCorrection, double initialTime, int timbre) {
		super(volume);
		
		initialPitch = note + pitchCorrection - 2;
		this.initialTime = initialTime;

		// higher timbre value makes more like square wave
		// 0 is sine wave
		// I really would like to come up with a better algorithm for intermediate values,
		//		but this will have to do for now
		timbreFactor = 256.0 / (256 - timbre);
	}
	
	public void finalize(double note, int pitchCorrection, double finalTime, boolean reverse) {
		this.finalPitch = note + pitchCorrection - 2;
        this.finalTime = finalTime;
		
		chirpRate = (finalPitch - initialPitch) / (finalTime - initialTime) * (reverse ? -1 : 1);
		yOffset = (reverse ? finalPitch : initialPitch) - chirpRate * initialTime;
	}
	
	public double getValue(double time, double fraction) {
		double pitch = chirpRate * time + yOffset;
		double frequency = Math.pow(2, pitch/12)*440;
		double sineInput = frequency * 12/Math.log(2) / chirpRate;
		// the antiderivative of the frequency wrt time
		// If chirpRate == 0, this isn't the antiderivative (it's frequency * time).
		// But then you should use a Note instead.
		
		return this.volumeFactor * AlgUtils.enforceBounds(this.timbreFactor * Math.sin(AlgUtils.TAU * sineInput));
	}
}
