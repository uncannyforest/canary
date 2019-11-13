package com.canary.android.gui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.canary.R;
import com.canary.android.PlayManager;
import com.canary.android.prc.ImageImpl;
import com.canary.android.prc.WavWriterTask;
import com.canary.io.WavWriterData;
import com.canary.synth.Synthesizer;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PlayActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 60;

    private View mainView;
    private ImageView imageView;
    private View controlsView;
    private HorizontalScrollViewOptionalFling scrollView;
    private View menu;

    private boolean visible;
    private boolean playing = false;
    private Uri imageUri;

    private PlayManager playManager;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play);

        visible = true;
        mainView = findViewById(R.id.main);
        controlsView = findViewById(R.id.fullscreen_content_controls);
        menu = findViewById(R.id.fullscreen_content_controls_menu);
        imageView = (ImageView) findViewById(R.id.image_view);
        scrollView = (HorizontalScrollViewOptionalFling) findViewById(R.id.fullscreen_content);
        playManager = new PlayManager(scrollView);

        hideMenu();

        // Set up the user interaction to manually show or hide the system UI.
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        mainView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int leftWas, int topWas, int rightWas, int bottomWas) {
                // of course, scaleImage causes a layout change!
                // so we need to make sure to only call it when dimensions themselves change
                // to prevent infinite calling
                if ((left != leftWas) || (top != topWas) || (right != rightWas) || (bottom != bottomWas)) {
                    scaleImage((BitmapDrawable) imageView.getDrawable());
                    playManager.updateDisplayPositionFromBuffer();
                }
            }
        });
        findViewById(R.id.pane).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                hideMenu();
                return false;
            }
        });
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            boolean paused = false;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        playManager.updateBufferPositionFromDisplay();
                        if (paused) {
                            paused = false;
                            playManager.play(PlayActivity.this);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!paused && playing) {
                            playManager.stop();
                            paused = true;
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                    default:
                        break;
                }
                return false;
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.button_load).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.button_reload).setOnTouchListener(mDelayHideTouchListener);
        findViewById(R.id.button_play).setOnTouchListener(mDelayHideTouchListener);

        if (imageUri == null) {
            pickImage();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (resultCode == RESULT_OK) {
            imageUri = imageReturnedIntent.getData();
            loadImage();
        }
    }

    // MY EVENTS

    public void buttonLoad(View view) {
        pickImage();
        hideMenu();
    }

    public void buttonExport(View view) {
        saveAudio();
        hideMenu();
    }

    public void buttonHelp(View view) {
        help();
        hideMenu();
    }

    public void buttonMenu(View view) {
        showMenu();
    }

    public void buttonReload(View view) {
        loadImage();
        hideMenu();
    }

    public void buttonBeginning(View view) {
        beginning();
        hideMenu();
    }

    public void buttonPlay(View view) {
        playToggle();
        hideMenu();
    }

    // MY METHODS

    private void pickImage() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 0);
    }

    private void saveAudio() {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        try {
            File file = getExportFile();
            WavWriterData wavWriterData = new WavWriterData(file, 48000, 16, playManager.getSource());
            new WavWriterTask(progressBar, PlayActivity.this).execute(wavWriterData);
        } catch (IOException e) {
            e.printStackTrace();
            alertError(e.getMessage());
        }

    }

    private void beginning() {
        scrollView.smoothScrollTo(0, 0);
        playManager.resetBuffer();
    }

    private void playToggle() {
        if (playing) {
            playing = false;
            ((Button) findViewById(R.id.button_play)).setText("Play");
            scrollView.setFlingEnabled(true);
            playManager.stop();
        } else {
            playing = true;
            ((Button) findViewById(R.id.button_play)).setText("Stop");
            scrollView.setFlingEnabled(false);
            playManager.play(this);
            hide();
        }
    }

    private void loadImage() {
        if (imageUri == null) {
            return;
        }

        try {
            BitmapDrawable drawable = new BitmapDrawableWithNoDensity(getContentResolver().openInputStream(imageUri));
            drawable.setAntiAlias(false);
            drawable.setFilterBitmap(false);

            scaleImage(drawable);
            imageView.setImageDrawable(drawable);
            playManager.setSource(new Synthesizer(new ImageImpl(drawable.getBitmap())));

        } catch (IOException e) {
            e.printStackTrace();
            alertError(e.getMessage());
        }
    }

    private void scaleImage(BitmapDrawable drawable) {
        if (drawable == null) {
            return;
        }

        Bitmap bitmap = drawable.getBitmap();
        Point screen = new Point();
        getWindowManager().getDefaultDisplay().getSize(screen);
        int pixelSize = Math.max(screen.y / bitmap.getHeight(), 1);

        int paddingLeft = screen.x / 2 - pixelSize;
        int paddingRight = screen.x / 2;
        imageView.setPadding(paddingLeft, 0, paddingRight, 0);

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = bitmap.getWidth() * pixelSize;
        layoutParams.height = bitmap.getHeight() * pixelSize;
        imageView.setLayoutParams(layoutParams);

        playManager.setPixelSize(pixelSize);
    }


    private void help() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://uncannyforest.com/docs/canary"));
        startActivity(browserIntent);
    }

    private AlertDialog alert(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(PlayActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
        return alertDialog;
    }

    private void alertError(String error) {
        alert("Error", error);
    }

    private File getExportFile() throws IOException {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), "Canary");
        File file = new File(dir, buildExportFileName());
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return file;
    }

    private String buildExportFileName() {
        File imgFile = new File(imageUri.toString());
        String img = imgFile.getName();
        int nameLength = img.lastIndexOf('.');
        String coreName = (nameLength > 0) ? img.substring(0, nameLength) : img;
        return coreName + ".wav";
    }

    // OTHER METHODS

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        controlsView.setVisibility(View.GONE);
        visible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        imageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        visible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void hideMenu() {
        menu.setVisibility(View.GONE);
    }

    private void showMenu() {
        menu.setVisibility(View.VISIBLE);
        menu.requestFocus();
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            controlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Play Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
