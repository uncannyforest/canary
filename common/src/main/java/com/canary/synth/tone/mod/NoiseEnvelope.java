package com.canary.synth.tone.mod;

/**
 * Created on 2/8/2017.
 */

public class NoiseEnvelope {

    int code;

    public NoiseEnvelope(int code) {
        this.code = code;
    }

    public double getValue(double fraction) {
        if (code == 255) {
            return 1;
        }
        if (code == 254) {
            return 1 - fraction;
        }
        if (code == 253) {
            return (1 - fraction) * (1 - fraction);
        }
        if (code == 0) {
            return 4 * fraction * (1 - fraction);
        }
        if (code == 1) {
            return fraction;
        }
        if (code == 2) {
            return fraction * fraction;
        }
        if (code > 127) {
            return Math.pow(2, -fraction * (254 - code));
        }
        return Math.pow(2, (fraction - 1) * (code - 1));
    }
}
