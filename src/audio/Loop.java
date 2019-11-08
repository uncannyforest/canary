package audio;

public class Loop extends AudioMod {
	public Loop() {
		title = "Loop";
		keys = new char[] {'\0', '\0', '\0'};
	}
	
	public AudioUnit copy() {
		return new Loop();
	}
}
