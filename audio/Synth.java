package audio;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import jass.engine.Out;

public class Synth extends AudioInput {
	
	public static final int[] MAJOR_SCALE = {0, 2, 4, 5, 7, 9, 11};
	public static final double TAU = 2*Math.PI;
	
	private Score score = null;
	private Deque<Chirp> runningChirps;
	
	///// CONSTRUCTOR
	
	public Synth() {
		input = new InputSource();
		
		title = "Synth";
		keys = new char[] {'\0'};
	}
	
	///// GET/SET
	
	public void setScore(BufferedImage image) {
		if (image == null) score = null;
		else score = imageToScore(image);
	}
	
	///// COPY
	
	public AudioUnit copy() {
		return new Synth();
	}

	///// IMAGE TO SCORE
	
	private int posMod(int dividend, int divisor) {
		int mod = dividend % divisor;
		return mod + (mod<0?divisor:0);
	}
	
	private int posDiv(int dividend, int divisor) {
		int mod = dividend % divisor;
		return (dividend/divisor) - (mod<0?1:0);
	}
	
	public Score imageToScore(BufferedImage image) {
		int baseNote = 12;
		
		ArrayList<ArrayList<Tone>> scoreContents = new ArrayList<ArrayList<Tone>>(image.getWidth()-1);
		
		runningChirps = new LinkedList<Chirp>();

		// find end of percussion section
		// determines total volume and tempo
		int pixel;
		int percussionPixel = 1;
		double volume = 255;
		double pixelsPerSecond = 8;
		for (int y=0; y<image.getHeight(); y++) {
			pixel = image.getRGB(0, y);
			if ((pixel & 0x00FFFFFF) != 0) {
				percussionPixel = y;

				volume = ((pixel & 0x00FF0000) >>> 16) / 255.0;
				double tempo = 1 + (((pixel & 0x0000FF00) >> 8) / 240.0);
				double pixelsPerBeat = pixel & 0x000000FF;
				pixelsPerSecond = tempo * pixelsPerBeat;

				break;
			}
		}
		
		// find base note, indicated in image by highest non-black pixel on left edge
		// (this pixel is not an audible note - audible notes start at x=1)
		int baseNotePixel = 1;
		double transpose = 0;
		for (int y=percussionPixel+1; y<image.getHeight(); y++) {
			pixel = image.getRGB(0, y);
			if ((pixel & 0x00FFFFFF) != 0) {
				baseNotePixel = y;
				transpose = ((pixel & 0x0000FF00) >>> 8) / 21.0;
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
							pixel & 0x000000FF));
					} else {
						// convert from staff notation to note pitch in semitones
						int rawNote = baseNotePixel - y;
						int noteInScale = posMod(rawNote, 7);
						int octave = posDiv(rawNote, 7);
						int note = MAJOR_SCALE[noteInScale] + octave * 12;
						double actualNote = note + baseNote + transpose;
						
						if ((pixel & 0x00003000) == 0) { // note
							chord.add(new Note(note + baseNote + transpose,
								(pixel & 0x00FF0000) >>> 16,
								(pixel & 0x0000C000) >>> 14,
								pixel & 0x000000FF));
						} else { // chirp
							int chirpCode = (pixel & 0x00003000) >>> 12;
							Chirp chirp;
	
							switch (chirpCode) {
							case 1:
								chirp = new Chirp(actualNote,
									(pixel & 0x00FF0000) >>> 16,
									(pixel & 0x0000C000) >>> 14,
									(x-1) / pixelsPerSecond,
									pixel & 0x000000FF);
								runningChirps.add(chirp);
								break;
							case 2:
								chirp = runningChirps.removeLast();
								chirp.finalize(actualNote,
									(pixel & 0x0000C000) >>> 14,
									x / pixelsPerSecond);
								chord.add(chirp);
								break;
							case 3:
								chirp = runningChirps.removeFirst();
								chirp.finalize(actualNote,
									(pixel & 0x0000C000) >>> 14,
									x / pixelsPerSecond);
								chord.add(chirp);
								break;
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
		
	///// SCORE STRUCTURE

	public abstract class Tone {
		public double volumeFactor;
		
		protected Tone(int volume) {
			volumeFactor = volume / 255.0;
		}
		
		public abstract double getValue(double time, double fraction);
	}
	
	public class Note extends Tone {
		public double frequency;
		public double timbreFactor;
		
		/*
		 * note, in semitones above baseNote
		 * volume, [1,255]
		 * pitchCorrection, [0,3] in semitones where 2 = natural
		 * timbre, [0,255]
		 */
		public Note(double note, int volume, int pitchCorrection, int timbre) {
			super(volume);
			
			double pitch = note + pitchCorrection - 2;
			frequency = Math.pow(2, pitch/12)*440;

			// higher timbre value makes more like square wave
			// 0 is sine wave
			// I really would like to come up with a better algorithm for intermediate values,
			//		but this will have to do for now
			timbreFactor = 256.0 / (256 - timbre);
		}
		
		public double getValue(double time, double fraction) {
			return this.volumeFactor * enforceBounds(this.timbreFactor * Math.sin(TAU * this.frequency * time));
		}
	}
	
	public class Chirp extends Tone {
		public double timbreFactor;
		
		public double initialPitch;
		public double initialTime;
		
		public double chirpRate;
		public double offset;
		
		/*
		 * note, in semitones above baseNote
		 * volume, [1,255]
		 * pitchCorrection, [0,3] in semitones where 2 = natural
		 * timbre, [0,255]
		 */
		public Chirp(double note, int volume, int pitchCorrection, double initialTime, int timbre) {
			super(volume);
			
			initialPitch = note + pitchCorrection - 2;
			this.initialTime = initialTime;

			// higher timbre value makes more like square wave
			// 0 is sine wave
			// I really would like to come up with a better algorithm for intermediate values,
			//		but this will have to do for now
			timbreFactor = 256.0 / (256 - timbre);
		}
		
		public void finalize(double note, int pitchCorrection, double finalTime) {
			double finalPitch = note + pitchCorrection - 2;
			
			chirpRate = (finalPitch - initialPitch) / (finalTime - initialTime);
			offset = initialPitch - chirpRate * initialTime;
		}
		
		public double getValue(double time, double fraction) {
			double pitch = chirpRate * time + offset;
			double frequency = Math.pow(2, pitch/12)*440;
			double sineInput = frequency * 12/Math.log(2) / chirpRate;
			// the antiderivative of the frequency wrt time
			// If chirpRate == 0, this isn't the antiderivative (it's frequency * time).
			// But then you should use a Note instead.
			
			return this.volumeFactor * enforceBounds(this.timbreFactor * Math.sin(TAU * sineInput));
		}
	}
	
	public class Noise extends Tone {
		public double fractionOfDuration;
		public int envelopeCode;

		public Noise(int volume, int fractionOfDurationInput, int envelope) {
			super(volume);

			this.fractionOfDuration = fractionOfDurationInput / 255.0;
			this.envelopeCode = envelope;
		}
		
		public double getValue(double time, double fraction) {
			return (fraction <= fractionOfDuration)
					? envelope(fraction / fractionOfDuration) * this.volumeFactor * (2 * Math.random() - 1)
					: 0;
		}
		
		private double envelope(double fraction) {
			if (envelopeCode == 255) {
				return 1;
			}
			if (envelopeCode == 254) {
				return 1 - fraction;
			}
			if (envelopeCode == 253) {
				return (1 - fraction) * (1 - fraction);
			}
			if (envelopeCode == 0) {
				return 4 * fraction * (1 - fraction);
			}
			if (envelopeCode == 1) {
				return fraction;
			}
			if (envelopeCode == 1) {
				return fraction * fraction;
			}
			if (envelopeCode > 127) {
				return Math.pow(2, -fraction * (254 - envelopeCode));
			}
			return Math.pow(2, (fraction - 1) * (envelopeCode - 1));
		}
	}
	
	public class Score {
		private ArrayList<ArrayList<Tone>> notes;
		private int baseNote;
		private double notesPerSecond;
		private double volume;
		
		public Score(ArrayList<ArrayList<Tone>> notes, int baseNote, double notesPerSecond, double volume) {
			this.notes = notes;
			this.baseNote = baseNote;
			this.notesPerSecond = notesPerSecond;
			this.volume = volume;
		}
		
		public ArrayList<ArrayList<Tone>> getNotes() {
			return notes;
		}
		
		public int getBaseNote() {
			return baseNote;
		}
		
		public double getNotesPerSecond() {
			return notesPerSecond;
		}
		
		public double getVolume() {
			return volume;
		}
	}
	
	///// SCORE TO AUDIO
	
	/**
	 * Result must be between -1 and 1
	 */
	public static double enforceBounds(double input) {
		if (input > 1) return 1;
		if (input < -1) return -1;
		else return input;
	}

	public double getValue(double time) {
		double result = 0;
		
		double timeRelativeToChord = time * score.getNotesPerSecond();
		int chordIndex = (int)(timeRelativeToChord) % score.getNotes().size();
		double chordFraction = timeRelativeToChord % 1;
		
		List<Tone> chord = score.getNotes().get(chordIndex);
		for (Tone note : chord) {
			result += note.getValue(time, chordFraction);
		}
		
		return enforceBounds(score.getVolume() * result);
	}
	
	///// INTERFACE WITH JASS
	
	protected class InputSource extends Out {
		public InputSource() {
			super(BUFFER_SIZE);
		}
		
		protected void computeBuffer() {
			long addition = getTime()*BUFFER_SIZE;
			for (int i=0; i<BUFFER_SIZE; i++) {
				buf[i] = (float)getValue((i+addition)/AudioUnit.SRATE);
			}
		}
	}
}
