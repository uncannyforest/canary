package audio;

import jass.engine.Out;
import jass.engine.Source;

public abstract class AudioUnit {
  public static final int BUFFER_SIZE = 4096;
  public static final int NUM_BUFFERS_RTAUDIO = 0;
  public static final float SRATE = 44100;
  
  public static final AudioUnit NULL_UNIT = new NullUnit();
	
	String title;
	char[] keys;
	
	public String getTitle() {
		return title;
	}
	
	public char[] getKeys(){
		return keys;
	}
	
	public abstract boolean isInput();
	public abstract boolean isOutput();
	
	public abstract AudioUnit copy();
	public abstract boolean setSource(AudioUnit unit);
	public abstract Source toSource();
	
	protected static class NullUnit extends AudioUnit {
		public boolean isInput() {
			return false;
		}
		public boolean isOutput() {
			return false;
		}
		public AudioUnit copy() {
			return null;
		}
		public boolean setSource(AudioUnit unit) {
			return false;
		}
		public Source toSource() {
			return null;
		}
	}
}
