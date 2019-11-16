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

    /**
     * Mod with result always in [0, divisor)
     */
    public static int posMod(int dividend, int divisor) {
        int mod = dividend % divisor;
        return mod + (mod<0?divisor:0);
    }

    /**
     * Mod with result always in [0, divisor)
     */
    public static double posMod(double dividend, double divisor) {
        double mod = dividend % divisor;
        return mod + (mod<0?divisor:0);
    }

    /**
     * Div with result always rounded in negative direction
     */
    public static int posDiv(int dividend, int divisor) {
        int mod = dividend % divisor;
        return (dividend/divisor) - (mod<0?1:0);
    }

    /**
     * Div with result always rounded in negative direction
     */
    public static double posDiv(double dividend, double divisor) {
        double mod = dividend % divisor;
        return (dividend/divisor) - (mod<0?1:0);
    }
}
