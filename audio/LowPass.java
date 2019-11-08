package audio;

public class LowPass extends AudioMod {
	public LowPass() {
		title = "LowP";
		keys = new char[] {'\0'};
	}

	public AudioUnit copy() {
		return new LowPass();
	}
}
