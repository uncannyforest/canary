package com.canary.synth.tone.mod;

import com.canary.synth.AlgUtils;

import java.util.Objects;

public interface Timbre {

    /**
     * Get value of waveform for given locationInPhase, between 0 and 1.  locationInPhase can be
     * outside this interval, but the integer part will be ignored.
     */
    public double getWaveformValue(double locationInPhase);

    public static class V10 implements Timbre {
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
}
