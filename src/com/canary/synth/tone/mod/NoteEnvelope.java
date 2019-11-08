package com.canary.synth.tone.mod;

/**
 * Created on 2/8/2017.
 */

public class NoteEnvelope {

    int code;

    public NoteEnvelope(int code) {
        this.code = code;
    }

    public double getValue(double fraction) {
        if (code == 0 || ((code == 7 && fraction >= 0.5) || (code == 15 && fraction < 0.5))) {
            return 1;
        } else if ((code == 7) || (code == 8) || (code == 15)) {
            return 16 * fraction * fraction * (1 - fraction) * (1 - fraction);
        } else if (code < 7) {
            return Math.pow(2, -fraction * code);
        } else {
            return Math.pow(2, (fraction-1) * (15-code));
        }
    }

    public boolean isNoop() {
        return code == 0;
    }
}
