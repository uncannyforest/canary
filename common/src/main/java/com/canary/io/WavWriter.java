package com.canary.io;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import uk.co.labbookpages.WavFile;
import uk.co.labbookpages.WavFileException;

/**
 * Write logic derived from WriteExample.java in
 * http://www.labbookpages.co.uk/audio/javaWavFiles.html
 */
public final class WavWriter {
    public static final int WRITE_BUFFER_SIZE = 10000;

    public static class Data {

        public final File file;
        public final int sampleRate;
        public final int validBits;
        public final AudioSource src;

        public Data(File file, int sampleRate, int validBits, AudioSource src) {
            this.file = file;
            this.sampleRate = sampleRate;
            this.validBits = validBits;
            this.src = src;
        }
    }

    public static void write(Data data) throws IOException, WavFileException {
        write(data, (progress) -> {}); // throw the updates into the void
    }

    /**
     * Writes entire song to wav file.
     *
     * @param data specs for this write task
     * @param progressUpdate function to update UI on progress.  Passes in double between 0 and 1
     */
    public static void write(Data data, Consumer<Double> progressUpdate) throws IOException, WavFileException {
        double duration = data.src.getDuration();     // Seconds

        // Calculate the number of frames required for specified duration
        long numFrames = (long)(duration * data.sampleRate);

        // Create a wav file with the name specified as the first argument
        WavFile wavFile = WavFile.newWavFile(data.file, 1, numFrames, data.validBits, data.sampleRate);

        // Create a buffer of 100 frames
        double[][] buffer = new double[1][WRITE_BUFFER_SIZE];

        // Initialise a local frame counter
        long frameCounter = 0;

        try {
            // Loop until all frames written
            while (frameCounter < numFrames) {
                // Determine how many frames to write, up to a maximum of the buffer size
                long remaining = wavFile.getFramesRemaining();
                int toWrite = (remaining > WRITE_BUFFER_SIZE) ? WRITE_BUFFER_SIZE : (int) remaining;

                // Fill the buffer, one tone per channel
                for (int s=0 ; s<toWrite ; s++, frameCounter++) {
                    buffer[0][s] = data.src.getValue((double) frameCounter / data.sampleRate);
                }

                // Write the buffer
                wavFile.writeFrames(buffer, toWrite);
                progressUpdate.accept((double) frameCounter / numFrames);
            }
        } finally {
            wavFile.close();
        }
    }
}