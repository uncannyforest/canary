package com.canary.android.snd;

import com.canary.io.AudioBuffer;

import org.puredata.android.io.AudioWrapper;

import java.io.IOException;

/**
 * Created on 1/20/2017.
 */

public class AudioWrapperImpl extends AudioWrapper {

    AudioBuffer buffer;

    public AudioWrapperImpl(AudioBuffer buffer, int bufferSizePerChannel) throws IOException {
        super(buffer.getSampleRate(), 0, 1, bufferSizePerChannel);
        this.buffer = buffer;
    }

    @Override
    protected int process(short[] inBuffer, short[] outBuffer) {
        return buffer.play(outBuffer);
    }

    public AudioBuffer getBuffer() {
        return buffer;
    }
}
