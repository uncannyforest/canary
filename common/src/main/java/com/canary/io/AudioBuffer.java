package com.canary.io;

import com.canary.io.AudioSource;

/**
 * Created on 1/20/2017.
 */

public class AudioBuffer {

    AudioSource source;
    int sampleRate;

    long positionSample = 0;
    long totalSamples;

    public AudioBuffer(AudioSource source, int sampleRate) {
        this.source = source;
        this.sampleRate = sampleRate;
        this.totalSamples = (long)(source.getDuration() * sampleRate);
    }

    /**
     * populates an array with sound output
     *
     * if we go beyond the end in this iteration, the extra chuck is populated with zeroes
     *
     * if we've already reached the end, return 1 (otherwise 0)
     *
     * @param outputArray array to populate
     */
    public int play(short[] outputArray) {
        if (positionSample >= totalSamples) {
            return 1;
        }

        int samples = outputArray.length;
        for (int i = 0; i < samples; i++) {
            short value;

            if (positionSample >= totalSamples) {
                value = 0;
            } else {
                value = (short)(Short.MAX_VALUE * source.getValue(getPosition()));
                positionSample++;
            }

            outputArray[i] = value;
        }

        return 0;
    }

    public double getPosition() {
        return (double)positionSample / sampleRate;
    }

    public void setPosition(double position) {
        this.positionSample = (long)(position * sampleRate);
    }

    public void reset() {
        this.positionSample = 0;
    }

    public AudioSource getSource() {
        return source;
    }

    public int getSampleRate() {
        return sampleRate;
    }

}
