package com.canary.android.prc;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;

import com.canary.io.WavWriter;

import java.io.IOException;
import java.util.function.Consumer;

import uk.co.labbookpages.WavFileException;

public class WavWriterTask extends AsyncTask<WavWriter.Data, Integer, String> {

    private Exception exception = null;

    private ProgressBar bar;
    private Activity activity;

    public WavWriterTask(ProgressBar bar, Activity activity) {
        this.bar = bar;
        this.activity = activity;
    }

    protected String doInBackground(WavWriter.Data... data) {
        try {
            WavWriter.write(data[0], new Consumer<Double>() {
                @Override
                public void accept(Double progress) {
                    publishProgress((int)(100 * progress));
                }
            });
        } catch (IOException | WavFileException e) {
            this.exception = e;
        }
        return data[0].file.toString();
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
}
