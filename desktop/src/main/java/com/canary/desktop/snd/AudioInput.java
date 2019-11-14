package com.canary.desktop.snd;

import jass.engine.Out;
import jass.engine.Source;
import jass.generators.AudioIn;

public abstract class AudioInput extends AudioUnit {
  Out input;

    public boolean isInput() {
        return true;
    }
    public boolean isOutput() {
        return false;
    }
    public boolean setSource(AudioUnit unit) {
        return false;
    }
    public Source toSource() {
        return input;
    }
}
