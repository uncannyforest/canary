package com.canary.android;

import android.content.Context;
import android.widget.HorizontalScrollView;

import com.canary.android.gui.PlayerDisplayImpl;
import com.canary.android.snd.AudioWrapperImpl;
import com.canary.io.AudioBuffer;
import com.canary.io.AudioSource;
import com.canary.io.BufferDisplayMediator;
import com.canary.synth.Synthesizer;

import java.io.IOException;
import java.nio.Buffer;

/**
 * I wanted all the playback logic to be in a separate file - PlayActivity was getting too bloated.
 *
 * Created on 1/21/2017.
 */

public class PlayManager {
    // hardcoded these values, very bad

    // got this value by calling (from a Context)
    // ((AudioManager)(this.getSystemService(Context.AUDIO_SERVICE))).getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
    public static final int SAMPLE_RATE = 48000;

    // got this value by calling (from a Context)
    // ((AudioManager)(this.getSystemService(Context.AUDIO_SERVICE))).getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
    final int BUFFER_SIZE = 240;

    AudioWrapperImpl audio = null;
    PlayerDisplayImpl display;
    BufferDisplayMediator mediator;

    public PlayManager(HorizontalScrollView scrollView) {
        this.display = new PlayerDisplayImpl(scrollView);
        this.mediator = new BufferDisplayMediator(display);
    }

    public AudioSource getSource() {
        if (audio == null) {
            throw new IllegalStateException("Source not initialized");
        }

        return audio.getBuffer().getSource();
    }

    public void setSource(Synthesizer source) throws IOException {
        AudioBuffer buffer = new AudioBuffer(source, SAMPLE_RATE);
        audio = new AudioWrapperImpl(buffer, BUFFER_SIZE);
        mediator.setBuffer(buffer);
        mediator.setNotesPerSecond(source.getNotesPerSecond());
    }

    public void setPixelSize(int pixelSize) {
        display.setPixelSize(pixelSize);
    }

    public void play(Context context) {
        if (audio == null) {
            throw new IllegalStateException("Source not initialized");
        }

        audio.start(context);
        mediator.play();
    }

    public void stop() {
        if (audio == null) {
            throw new IllegalStateException("Source not initialized");
        }

        audio.stop();
        mediator.stop();
    }

    public void updateBufferPositionFromDisplay() {
        mediator.updateBufferPositionFromDisplay();
    }

    public void updateDisplayPositionFromBuffer() {
        mediator.updateDisplayPositionFromBuffer();
    }

    public void resetBuffer() {
        mediator.resetBuffer();
    }
}
