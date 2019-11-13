package com.canary.android.prc;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.canary.android.gui.PlayActivity;
import com.canary.io.AudioSource;
import com.canary.io.WavWriterData;

import java.io.File;
import java.io.IOException;

import uk.co.labbookpages.WavFile;
import uk.co.labbookpages.WavFileException;

/**
 * Created on 1/13/2017.
 * Derived from WriteExample.java in http://www.labbookpages.co.uk/audio/javaWavFiles.html
 */

public class WavWriterTask extends AsyncTask<WavWriterData, Integer, String> {

    public static final int WRITE_BUFFER_SIZE = 10000;

    private long numFrames;
    private long frameCounter;
    private Exception exception = null;

    private File file;
    private int sampleRate;
    private int validBits;
    private AudioSource src;

    private ProgressBar bar;
    private Activity activity;

    public WavWriterTask(ProgressBar bar, Activity activity) {
        this.bar = bar;
        this.activity = activity;
    }

    private int getProgress() {
        return (int) (100 * ((float) frameCounter) / numFrames);
    }

    protected String doInBackground(WavWriterData... data) {
        this.file = data[0].file;
        this.sampleRate = data[0].sampleRate;
        this.validBits = data[0].validBits;
        this.src = data[0].src;

        try {
            saveBlocking();
        } catch (IOException | WavFileException e) {
            this.exception = e;
        }

        return file.toString();
    }

    protected void onProgressUpdate(Integer... progress) {
        bar.setProgress(progress[0]);
    }

    protected void onPostExecute(String result) {
        bar.setVisibility(View.INVISIBLE);

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        if (exception == null) {
            alertDialog.setTitle("Saved");
            alertDialog.setMessage("WAV file successfully saved to " + result);
        } else {
            alertDialog.setTitle("Error");
            alertDialog.setMessage(exception.getMessage());
        }
        alertDialog.show();
    }

    public void saveBlocking() throws IOException, WavFileException {
        double duration = src.getDuration();     // Seconds

        // Calculate the number of frames required for specified duration
        numFrames = (long)(duration * sampleRate);

        // Create a wav file with the name specified as the first argument
        WavFile wavFile = WavFile.newWavFile(file, 1, numFrames, validBits, sampleRate);

        // Create a buffer of 100 frames
        double[][] buffer = new double[1][WRITE_BUFFER_SIZE];

        // Initialise a local frame counter
        frameCounter = 0;

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
                    buffer[0][s] = src.getValue((double)frameCounter / sampleRate);
                }

                // Write the buffer
                wavFile.writeFrames(buffer, toWrite);
                publishProgress(getProgress());
            }
        } finally {
            wavFile.close();
        }
    }
}
