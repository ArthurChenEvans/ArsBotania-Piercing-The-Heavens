package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The Singularity: a Terra Truncator on trees, a Terra Shatterer on everything else. */
class SingularityTest {

    @Test
    void hittingATrunkFellsTheWholeTree() {
        assertEquals(Singularity.Action.FELL_TREE, Singularity.actionFor(true, false, false, true));
    }

    @Test
    void sneakingOnATrunkBreaksJustThatLogLikeTheTruncator() {
        assertEquals(Singularity.Action.AREA, Singularity.actionFor(true, true, false, true));
    }

    @Test
    void temperanceStopsTreeFelling() {
        assertEquals(Singularity.Action.AREA, Singularity.actionFor(true, false, true, true));
    }

    @Test
    void anythingElseIsAreaMinedWhenAreaModeIsOn() {
        assertEquals(Singularity.Action.AREA, Singularity.actionFor(false, false, false, true));
        assertEquals(Singularity.Action.SINGLE, Singularity.actionFor(false, false, false, false));
    }

    @Test
    void areaModeOffStillFellsTrees() {
        assertEquals(Singularity.Action.FELL_TREE, Singularity.actionFor(true, false, false, false));
    }

    @Test
    void mineableEverywhereAShatterTruncatorShovelOrHoeIs() {
        assertEquals(4, Singularity.MINEABLE_TAGS.size());
    }
}
