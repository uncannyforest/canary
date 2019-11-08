package audio;

import audio.Synth.InputSource;
import jass.engine.Out;

public class EmptyInput extends AudioInput {
	public EmptyInput() {
		input = new InputSource();
		
		title = "Synth";
		keys = new char[] {'\0'};
	}
	
	public AudioUnit copy() {
		return new EmptyInput();
	}
	
	public float getValue(double time) {
		return 0;
	}
	
	protected class InputSource extends Out {
		public InputSource() {
			super(BUFFER_SIZE);
		}
		
		protected void computeBuffer() {
			buf[0] = getValue(getTime()/AudioUnit.SRATE);
		}
	}
}
