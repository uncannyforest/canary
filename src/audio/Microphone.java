package audio;

import jass.engine.Out;
import jass.generators.AudioIn;

public class Microphone extends AudioInput {
  public final int BUFFER_SIZE_JAVASOUND = BUFFER_SIZE;
  public final String INMIXER = "Microphone (2- Realtek High Def";

	public Microphone() {
		input = new AudioIn(SRATE, BUFFER_SIZE, BUFFER_SIZE_JAVASOUND, INMIXER, "javasound", NUM_BUFFERS_RTAUDIO);
		
		title = "Mike";
		keys = new char[] {'\0'};
	}
	
	public Microphone(Out input) {
		this.input = input;
		
		title = "Mike";
		keys = new char[] {'\0'};
	}
	
	public AudioUnit copy() {
		return new Microphone(input);
	}
}
