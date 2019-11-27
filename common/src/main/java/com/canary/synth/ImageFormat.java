package com.canary.synth;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import com.canary.io.Image;
import com.canary.synth.tone.Chirp;
import com.canary.synth.tone.Noise;
import com.canary.synth.tone.Note;
import com.canary.synth.tone.PartialChirp;
import com.canary.synth.tone.Tone;
import com.canary.synth.tone.mod.NoiseEnvelope;
import com.canary.synth.tone.mod.NoteEnvelope;
import com.canary.synth.tone.mod.Timbre;

public class ImageFormat {

    public static final int[] MAJOR_SCALE = {0, 2, 4, 5, 7, 9, 11};

    public static int getYBoundary(Image image) {
        int pixel;
        for (int y=0; y<image.getHeight(); y++) {
            pixel = image.getRGB(0, y);
            if ((pixel & 0x00FFFFFF) != 0) {
                return y;
            }
        }
        throw new IllegalArgumentException("Format missing non-black pixels in leftmost row");
    }

    public static int getFormatVersion(Image image, int yBoundary) {
        int pixel = image.getRGB(0, yBoundary);
        return ((pixel & 0x00FF0000) >>> 16);
    }

    public static int getFormatVersion(Image image) {
        return getFormatVersion(image, getYBoundary(image));
    }

    public static Score load(Image image) {
        Deque<Chirp> runningChirps;

        // Why not 0?
        int baseNote = 12;

        ArrayList<ArrayList<Tone>> scoreContents = new ArrayList<ArrayList<Tone>>(image.getWidth()-1);

        runningChirps = new LinkedList<Chirp>();

        // find end of percussion section
        // determines total volume and tempo
        int pixel;
        int percussionPixel = getYBoundary(image);
        double volume = 255;
        double pixelsPerSecond = 8;
        int version = 0x14; // (latest version) - must be initialized for Java compile

        // Process first format pixel
        pixel = image.getRGB(0, percussionPixel);
        int claimedVersion = ((pixel & 0x00FF0000) >>> 16);
        if (claimedVersion >= 0x13) {
            version = claimedVersion;
        } else {
            version = 0x13; // oldest supported version
        }
        double tempo = 1 + (((pixel & 0x0000FF00) >> 8) / 240.0);
        double pixelsPerBeat = pixel & 0x000000FF;
        pixelsPerSecond = tempo * pixelsPerBeat;

        // find base note, indicated in image by highest non-black pixel on left edge
        // (this pixel is not an audible note - audible notes start at x=1)
        int baseNotePixel = 1;
        double transpose = 0;
        for (int y=percussionPixel+1; y<image.getHeight(); y++) {
            pixel = image.getRGB(0, y);
            if ((pixel & 0x00FFFFFF) != 0) {
                baseNotePixel = y;
                volume = ((pixel & 0x00FF0000) >>> 16) / 255.0;
                transpose = ((pixel & 0x0000FF00) >>> 8) / 20.0;
                break;
            }
        }

        ArrayList<Tone> chord;
        for (int x=1; x<image.getWidth(); x++) {
            chord = new ArrayList<Tone>();
            for (int y=0; y<image.getHeight(); y++) {
                pixel = image.getRGB(x, y);

                if ((pixel & 0x00FF0000) != 0) {// non-zero volume

                    if (y <= percussionPixel) { // noise
                        chord.add(new Noise((pixel & 0x00FF0000) >>> 16,
                            (pixel & 0x0000FF00) >>> 8,
                            new NoiseEnvelope(pixel & 0x000000FF)));
                    } else {
                        // convert from staff notation to note pitch in semitones
                        int rawNote = baseNotePixel - y;
                        int noteInScale = AlgUtils.posMod(rawNote, 7);
                        int octave = AlgUtils.posDiv(rawNote, 7);
                        int note = MAJOR_SCALE[noteInScale] + octave * 12;
                        double actualNote = note + baseNote + transpose;

                        if ((pixel & 0x00001000) == 0) { // note
                            chord.add(new Note(note + baseNote + transpose,
                                (pixel & 0x00FF0000) >>> 16,
                                (pixel & 0x0000C000) >>> 14,
                                Timbre.create(version, pixel & 0x000000FF),
                                new NoteEnvelope((pixel & 0x00000F00) >>> 8)));
                        } else { // chirp
                            int chirpCode = (pixel & 0x00000F00) >>> 8;

                            Chirp chirp;
                            boolean endChirp = (chirpCode & 0x8) == 0x8;

                            if (endChirp) {
                                boolean reverse = (chirpCode & 0x4) == 0x4;
                                boolean filo = (chirpCode & 0x2) == 0x2;

                                chirp = filo ? runningChirps.removeLast() : runningChirps.removeFirst();
                                chirp.finalize(actualNote,
                                        (pixel & 0x0000C000) >>> 14,
                                        x / pixelsPerSecond,
                                        reverse);
                                chord.add(chirp);
                            } else {
                                if ((chirpCode & 0x7) == 0x0) {
                                    chirp = new Chirp(actualNote,
                                            (pixel & 0x00FF0000) >>> 16,
                                            (pixel & 0x0000C000) >>> 14,
                                            (x - 1) / pixelsPerSecond,
                                            Timbre.create(version, pixel & 0x000000FF));
                                } else {
                                    chirp = new PartialChirp(actualNote,
                                            (pixel & 0x00FF0000) >>> 16,
                                            (pixel & 0x0000C000) >>> 14,
                                            (x - 1) / pixelsPerSecond,
                                            Timbre.create(version, pixel & 0x000000FF),
                                            chirpCode & 0x7);
                                }
                                runningChirps.add(chirp);
                            }
                        }
                    }
                }
            }
            for (Chirp chirp : runningChirps) {
                chord.add(chirp);
            }
            scoreContents.add(chord);
        }

        return new Score(scoreContents, baseNote, pixelsPerSecond, volume);
    }

}
