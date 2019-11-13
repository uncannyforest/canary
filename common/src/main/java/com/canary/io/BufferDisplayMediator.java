package com.canary.io;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created on 1/20/2017.
 */

public class BufferDisplayMediator {

    public static final int UPDATE_FREQUENCY_MILLIS = 20;
    // in practice it is not possible for this to be faster than SAMPLE_RATE / BUFFER_SIZE
    // 1000 / (SAMPLE_RATE / BUFFER_SIZE) is currently 5

    AudioBuffer buffer;
    PlayerDisplay display;
    double notesPerSecond = 0.0; // the length of a "sound unit", e.g. pixel, determined by the
                                 // audio source and to be used by the display
    Timer displayUpdateTimer;

    public BufferDisplayMediator(PlayerDisplay display) {
        this.display = display;
    }

    public void setBuffer(AudioBuffer buffer) {
        if (buffer != null) {
            stop();
        }

        this.buffer = buffer;
    }

    public void setNotesPerSecond(double notesPerSecond) {
        this.notesPerSecond = notesPerSecond;
    }

    public void updateBufferPositionFromDisplay() {
        if (buffer == null) {
            throw new IllegalStateException("Buffer not initialized");
        }
        if (notesPerSecond == 0.0) {
            throw new IllegalStateException("Notes per Second not initialized");
        }

        buffer.setPosition(display.getNotePosition() / notesPerSecond);
    }

    public void updateDisplayPositionFromBuffer() {
        if (buffer == null || notesPerSecond == 0.0) {
            return;
        }

        display.setNotePosition(buffer.getPosition() * notesPerSecond);
    }

    public void resetBuffer() {
        buffer.reset();
    }

    public void play() {
        if (buffer == null) {
            throw new IllegalStateException("Buffer not initialized");
        }
        if (notesPerSecond == 0.0) {
            throw new IllegalStateException("Notes per Second not initialized");
        }

        if (displayUpdateTimer != null) {
            return;
        }

        displayUpdateTimer = new Timer();
        displayUpdateTimer.schedule(new UpdateDisplayTask(), 0, UPDATE_FREQUENCY_MILLIS);
    }

    public void stop() {
        if (displayUpdateTimer != null) {
            displayUpdateTimer.cancel();
            displayUpdateTimer = null;
        }
    }

    public class UpdateDisplayTask extends TimerTask {
        boolean priorityUpdated = false;

        @Override
        public void run() {
//            if (!priorityUpdated) {
//                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
//                priorityUpdated = true;
//            }

            display.setNotePosition(buffer.getPosition() * notesPerSecond);
        }
    }

}
