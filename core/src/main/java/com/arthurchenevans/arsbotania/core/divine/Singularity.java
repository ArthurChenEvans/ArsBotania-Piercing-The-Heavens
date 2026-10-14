package com.arthurchenevans.arsbotania.core.divine;

import java.util.List;

/** The Singularity's mining: a Terra Truncator on tree trunks, an S-rank Terra Shatterer otherwise. */
public final class Singularity {
    /** Vanilla block tags it mines as the right tool: pickaxe, axe, shovel and hoe. */
    public static final List<String> MINEABLE_TAGS = List.of(
            "minecraft:mineable/pickaxe", "minecraft:mineable/axe", "minecraft:mineable/shovel", "minecraft:mineable/hoe");

    public enum Action { FELL_TREE, AREA, SINGLE }

    private Singularity() {
    }

    /**
     * Truncator rules for trees (sneak or Temperance break a single log); otherwise the
     * Shatterer's area, if area mode is on.
     */
    public static Action actionFor(boolean trunk, boolean sneaking, boolean temperance, boolean areaMode) {
        if (trunk && !sneaking && !temperance) return Action.FELL_TREE;
        return areaMode ? Action.AREA : Action.SINGLE;
    }
}
