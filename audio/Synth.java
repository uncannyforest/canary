package audio;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import jass.engine.Out;

public class Synth extends AudioInput {
	
	public static final int[] MAJOR_SCALE = {0, 2, 4, 5, 7, 9, 11};
	public static final double TAU = 2*Math.PI;
	
	private Score score = null;
	
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
		int baseNote = 24;
		
		ArrayList<ArrayList<Note>> scoreContents = new ArrayList<ArrayList<Note>>(image.getWidth()-1);

		int pixel;

		// find base note, indicated in image by highest non-black pixel on left edge
		// (this pixel is not an audible note - audible notes start at x=1)
		int baseNotePixel = 0;
		for (int y=0; y<image.getHeight(); y++) {
			pixel = image.getRGB(0, y);
			if ((pixel & 0x00FFFFFF) != 0) {
				baseNotePixel = y;
				break;
			}
		}
		
		ArrayList<Note> chord;
		for (int x=1; x<image.getWidth(); x++) {
			chord = new ArrayList<Note>();
			for (int y=0; y<image.getHeight(); y++) {
				pixel = image.getRGB(x, y);
				
				if ((pixel & 0x00FF0000) != 0) {// non-zero volume
					
					// convert from staff notation to note pitch in semitones
					int rawNote = baseNotePixel-y;
					int noteInScale = posMod(rawNote, 7);
					int octave = posDiv(rawNote, 7);
					int note = MAJOR_SCALE[noteInScale] + octave;
					
					// add note
					chord.add(new Note(
							note + baseNote,
							(pixel & 0x00FF0000) >>> 16,
							(pixel & 0x0000FF00) >>> 8,
							pixel & 0x000000FF));
				}
			}
			scoreContents.add(chord);
		}
		
		return new Score(scoreContents, baseNote, 4);
	}
		
	///// SCORE STRUCTURE

	public class Note {
		public double frequency;
		public double volumeFactor;
		public double timbreFactor;
		
		/*
		 * note, in semitones above baseNote
		 * volume, [1,255]
		 * pitchCorrection, [0,254] (255 permitted without error)
		 * timbre, [0,255]
		 */
		public Note(int note, int volume, int pitchCorrection, int timbre) {
			double pitch = ((note*85) + (pitchCorrection-127)) / 85.0;
			frequency = Math.pow(2, pitch/12)*440;
			
			volumeFactor = volume / 255.0;

			// higher timbre value makes more like square wave
			// 0 is sine wave
			// I really would like to come up with a better algorithm for intermediate values,
			//		but this will have to do for now
			timbreFactor = 256.0 / (256 - timbre);
		}
	}
	
	public class Score {
		private ArrayList<ArrayList<Note>> notes;
		private int baseNote;
		private int notesPerSecond;
		
		public Score(ArrayList<ArrayList<Note>> notes, int baseNote, int notesPerSecond) {
			this.notes = notes;
			this.baseNote = baseNote;
			this.notesPerSecond = notesPerSecond;
		}
		
		public ArrayList<ArrayList<Note>> getNotes() {
			return notes;
		}
		
		public int getBaseNote() {
			return baseNote;
		}
		
		public int getNotesPerSecond() {
			return notesPerSecond;
		}
	}
	
	///// SCORE TO AUDIO
	
	private double enforceBounds(double input) {
		if (input > 1) return 1;
		if (input < -1) return -1;
		else return input;
	}

	public double getValue(double time) {
		double result = 0;
		
		int chordIndex = (int)(time * score.getNotesPerSecond()) % score.getNotes().size();
		
		List<Note> chord = score.getNotes().get(chordIndex);
		for (Note note : chord) {
			result += note.volumeFactor * enforceBounds(note.timbreFactor * Math.sin(TAU * note.frequency * time));
		}
		
		return enforceBounds(result);
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