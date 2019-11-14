package com.canary.desktop.snd;

import com.canary.synth.Synthesizer;
import jass.engine.Out;

public class SynthInput extends AudioInput {

    Synthesizer synthesizer;

    ///// CONSTRUCTOR

    public SynthInput(Synthesizer synthesizer) {
        this.synthesizer = synthesizer;

        input = new InputSource(synthesizer);

        title = "Synth";
        keys = new char[] {'\0'};
    }

    ///// INTERFACE WITH JASS

    protected class InputSource extends Out {
        Synthesizer synthesizer;

        public InputSource(Synthesizer synthesizer) {
            super(BUFFER_SIZE);
            this.synthesizer = synthesizer;
        }

        protected void computeBuffer() {
            long addition = getTime()*BUFFER_SIZE;
            for (int i=0; i<BUFFER_SIZE; i++) {
                buf[i] = (float)(synthesizer.getValue((i+addition)/AudioUnit.SRATE));
            }
        }
    }
}
