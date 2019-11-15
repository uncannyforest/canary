package com.canary.synth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlgUtilsTest {
    @Test
    public void enforceBounds_lessThanNegOne_setsToNegOne() {
        assertEquals(-1.0, AlgUtils.enforceBounds(-10.0), 0.0001);
        assertEquals(-1.0, AlgUtils.enforceBounds(-1.1), 0.0001);
    }

    @Test
    public void enforceBounds_inRange_leaves() {
        assertEquals(-1.0, AlgUtils.enforceBounds(-1.0), 0.0001);
        assertEquals(-0.5, AlgUtils.enforceBounds(-0.5), 0.0001);
        assertEquals(0.0, AlgUtils.enforceBounds(0.0), 0.0001);
        assertEquals(0.5, AlgUtils.enforceBounds(0.5), 0.0001);
        assertEquals(1.0, AlgUtils.enforceBounds(1.0), 0.0001);
    }

    @Test
    public void enforceBounds_moreThanOne_setsToOne() {
        assertEquals(1.0, AlgUtils.enforceBounds(1.1), 0.0001);
        assertEquals(1.0, AlgUtils.enforceBounds(10.0), 0.0001);
    }

    @Test
    public void posMod_pos_worksNormally() {
        assertEquals(4, AlgUtils.posMod(14, 5));
    }

    @Test
    public void posMod_neg_rotatesToPositive() {
        assertEquals(1, AlgUtils.posMod(-14, 5));
    }

    @Test
    public void posMod_negNoRemainder_makesZero() {
        assertEquals(0, AlgUtils.posMod(-15, 5));
    }

    @Test
    public void posDiv_pos_worksNormally() {
        assertEquals(2, AlgUtils.posDiv(14, 5));
    }

    @Test
    public void posDiv_neg_roundsToNegative() {
        assertEquals(-3, AlgUtils.posDiv(-14, 5));
    }

    @Test
    public void posDiv_negNoRemainder_isQuotient() {
        assertEquals(-3, AlgUtils.posDiv(-15, 5));
    }
}
