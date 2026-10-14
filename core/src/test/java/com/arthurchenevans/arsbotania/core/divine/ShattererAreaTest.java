package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The Singularity mines like Botania's Terra Shatterer at rank S, with Botania's own area rules. */
class ShattererAreaTest {

    @Test
    void sRankIsLevelFour() {
        assertEquals(4, ShattererArea.S_RANK);
    }

    @Test
    void miningDownwardClearsASevenBySevenLayer() {
        ShattererArea a = ShattererArea.forFace(ShattererArea.Axis.Y, ShattererArea.S_RANK, false);

        assertArrayEquals(new int[]{-3, 0, -3}, a.begin());
        assertArrayEquals(new int[]{3, 0, 3}, a.end());
        assertEquals(49, a.blockCount());
    }

    @Test
    void miningAWallClearsSevenWideSevenTallFromOneBelowTheTarget() {
        ShattererArea a = ShattererArea.forFace(ShattererArea.Axis.Z, ShattererArea.S_RANK, false);

        assertArrayEquals(new int[]{-3, -1, 0}, a.begin());
        assertArrayEquals(new int[]{3, 5, 0}, a.end());
        assertEquals(49, a.blockCount());
    }

    @Test
    void ringOfThorAddsARankAndDigsAllThreeAxes() {
        ShattererArea a = ShattererArea.forFace(ShattererArea.Axis.Z, ShattererArea.S_RANK, true);

        assertArrayEquals(new int[]{-4, -1, -4}, a.begin());
        assertArrayEquals(new int[]{4, 7, 4}, a.end());
    }

    @Test
    void stoneOfTemperanceCapsItAtThreeByThree() {
        ShattererArea a = ShattererArea.forFace(ShattererArea.Axis.Y, ShattererArea.temperance(ShattererArea.S_RANK), false);

        assertEquals(9, a.blockCount());
    }
}
