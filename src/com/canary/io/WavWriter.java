package com.canary.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import uk.co.labbookpages.WavFile;
import uk.co.labbookpages.WavFileException;

/**
 * Created on 1/13/2017.
 * Derived from http://www.labbookpages.co.uk/audio/javaWavFiles.html
 */

public class WavWriter {

    public static final int WRITE_BUFFER_SIZE = 1000;

    public static void save(File file, int sampleRate, int validBits, AudioSource src) throws IOException, WavFileException {
        double duration = src.getDuration();     // Seconds

        // Calculate the number of frames required for specified duration
        long numFrames = (long)(duration * sampleRate);

        // Create a wav file with the name specified as the first argument
        WavFile wavFile = WavFile.newWavFile(file, 1, numFrames, validBits, sampleRate);

        // Create a buffer of 100 frames
        double[][] buffer = new double[1][WRITE_BUFFER_SIZE];

        // Initialise a local frame counter
        long frameCounter = 0;

        try {
            // Loop until all frames written
            while (frameCounter < numFrames)
            {
                // Determine how many frames to write, up to a maximum of the buffer size
                long remaining = wavFile.getFramesRemaining();
                int toWrite = (remaining > WRITE_BUFFER_SIZE) ? WRITE_BUFFER_SIZE : (int) remaining;

                // Fill the buffer, one tone per channel
                for (int s=0 ; s<toWrite ; s++, frameCounter++)
                {
                    buffer[0][s] = src.getValue(frameCounter / sampleRate);
                }

                // Write the buffer
                wavFile.writeFrames(buffer, toWrite);
            }
        } finally {
            wavFile.close();
        }
    }
}
