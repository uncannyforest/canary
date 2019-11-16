package com.canary.synth.tone.mod;

import com.canary.synth.AlgUtils;

import java.util.Objects;

public interface Timbre {

    /**
     * Get value of waveform for given locationInPhase, between 0 and 1.  locationInPhase can be
     * outside this interval, but the integer part will be ignored.
     */
    double getWaveformValue(double locationInPhase);

    static Timbre create(int version, int timbreCode) {
        if (version <= 0x13) {
            return new Timbre.V10(timbreCode);
        } else {
            return new Timbre.V14(timbreCode);
        }
    }

    class V10 implements Timbre {
        private double timbreFactor;

        public V10(int timbreCode) {
          // higher timbre value makes more like square wave
          // 0 is sine wave
          timbreFactor = 256.0 / (256 - timbreCode);
        }

        @Override
        public double getWaveformValue(double locationInPhase) {
          return AlgUtils.enforceBounds(
              this.timbreFactor * Math.sin(AlgUtils.TAU * locationInPhase));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            V10 v10 = (V10) o;
            return Double.compare(v10.timbreFactor, timbreFactor) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(timbreFactor);
        }
    }

    class V14 implements Timbre {
        private double interpolationFactor;
        private enum Type {
            SINE_SQUARE, SQUARE_SAW, SAW_TICK
        };
        private Type type;

        public V14(int timbreCode) {
            // 0 is sine wave
            if (timbreCode < 128) {
                interpolationFactor = 128.0 / (128.0 - timbreCode); // within [1, 128]
                type = Type.SINE_SQUARE;
            }
            // 128 is square wave
            else if (timbreCode < 192) {
                interpolationFactor = (timbreCode - 128.0) / 64.0; // within [1/64, 1]
                type = Type.SQUARE_SAW;
            }
            // 192 is sawtooth wave
            else {
                interpolationFactor = 64.0 / (256.0 - timbreCode); // within [1, 64]
                type = Type.SAW_TICK;
            }
            // 255 is tick
        }

        @Override
        public double getWaveformValue(double locationInPhase) {
            double sawCorrection; // two lines from (0, 0) to (0.5, -1) and (0.5, 1) to (1, 0)
            switch (type) {
            case SINE_SQUARE:
                return AlgUtils.enforceBounds(interpolationFactor * Math.sin(AlgUtils.TAU * locationInPhase));
            case SQUARE_SAW:
                boolean topHalf = AlgUtils.posMod(locationInPhase, 1.0) < 0.5;
                sawCorrection = 1 - AlgUtils.posMod(locationInPhase - 0.5, 1.0) * 2;
                return (topHalf ? 1 : -1) + sawCorrection * interpolationFactor;
            case SAW_TICK:
            default:
                sawCorrection = 1 - AlgUtils.posMod(locationInPhase - 0.5, 1.0) * 2;
                double boundary = 0.5 / interpolationFactor;
                if (locationInPhase < boundary) {
                    return sawCorrection * interpolationFactor + 1.0;
                } else if (locationInPhase >= 1.0 - boundary) {
                    return sawCorrection * interpolationFactor - 1.0;
                } else {
                    return 0;
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            V14 v14 = (V14) o;
            return Double.compare(v14.interpolationFactor, interpolationFactor) == 0 &&
                    type == v14.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(interpolationFactor, type);
        }
    }
}
