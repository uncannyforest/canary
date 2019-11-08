package audio;

import jass.engine.SinkIsFullException;
import jass.engine.Source;
import jass.generators.FilterContainer;

public abstract class AudioMod extends AudioUnit {
	FilterContainer filter;
	
	AudioUnit source;
	
	public boolean isInput() {
		return false;
	}
	public boolean isOutput() {
		return false;
	}
	public boolean setSource(AudioUnit unit) {
		if (source.toSource()!=null) filter.removeSource(source.toSource());
		try {
			if (source.toSource()!=null)filter.addSource(unit.toSource());
		} catch (SinkIsFullException e) {
			e.printStackTrace();
		}
		
		source = unit;
		return true;
	}
	public Source toSource() {
		return filter;
	}
}
