package com.canary.io;

import java.io.File;

/**
 * Created on 2/19/2017.
 */

public class WavWriterData {

    public final File file;
    public final int sampleRate;
    public final int validBits;
    public final AudioSource src;

    public WavWriterData(File file, int sampleRate, int validBits, AudioSource src) {
        this.file = file;
        this.sampleRate = sampleRate;
        this.validBits = validBits;
        this.src = src;
    }
}
