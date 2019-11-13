package com.canary.desktop.snd;

import jass.engine.SinkIsFullException;
import jass.engine.Source;
import jass.generators.AudioIn;
import jass.render.SourcePlayer;

public abstract class AudioOutput extends AudioUnit {
	PlayerContainer player = new PlayerContainer();
	
	AudioUnit source;
	
	public boolean isInput() {
		return false;
	}
	public boolean isOutput() {
		return true;
	}
	public boolean setSource(AudioUnit unit) {
		if (source!=null && source.toSource()!=null) player.content.removeSource(source.toSource());
		try {
			Source source = unit.toSource();
			if (source != null) player.content.addSource(source);
		} catch (SinkIsFullException e) {
			e.printStackTrace();
		}
		player.restart();
		
		source = unit;
		return true;
	}
	public Source toSource() {
		return null;
	}
	protected abstract void setupPlayer();
	
	protected class PlayerContainer {
		SourcePlayer content = null;
		
		public void restart() {
			SourcePlayer oldPlayer = content;
			oldPlayer.stopPlaying();
			setupPlayer();
			SourcePlayer newPlayer = content;
			for (Source src: oldPlayer.getSources()) {
				try {
					newPlayer.addSource(src);
				} catch (SinkIsFullException e) {
					e.printStackTrace();
				}
			}
			newPlayer.start();
		}
	}
}
