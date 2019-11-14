package com.canary.synth;

public class AlgUtils {
    public static final double TAU = 2*Math.PI;

    /**
     * Result must be between -1 and 1
     */
    public static double enforceBounds(double input) {
        if (input > 1) return 1;
        if (input < -1) return -1;
        else return input;
    }
}
