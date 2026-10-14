package com.arthurchenevans.arsbotania.core.divine;

/**
 * Botania's Terra Shatterer area rules: the box of blocks broken around the target, as offsets.
 * Divine has no ranks, so the Singularity always mines at rank S.
 */
public record ShattererArea(int[] begin, int[] end) {
    /** Botania's rank S (D=0 ... SS=5). */
    public static final int S_RANK = 4;

    public enum Axis { X, Y, Z }

    /**
     * @param face     axis of the block face that was hit
     * @param level    shatterer rank (0..5)
     * @param thorRing Botania's Ring of Thor: one rank higher and all three axes
     */
    public static ShattererArea forFace(Axis face, int level, boolean thorRing) {
        boolean doX = thorRing || face != Axis.X;
        boolean doY = thorRing || face != Axis.Y;
        boolean doZ = thorRing || face != Axis.Z;
        int lvl = level + (thorRing ? 1 : 0);
        int range = lvl - 1;
        int rangeY = Math.max(1, range);
        if (range <= 0) return new ShattererArea(new int[]{0, 0, 0}, new int[]{0, 0, 0});
        return new ShattererArea(
                new int[]{doX ? -range : 0, doY ? -1 : 0, doZ ? -range : 0},
                new int[]{doX ? range : 0, doY ? rangeY * 2 - 1 : 0, doZ ? range : 0});
    }

    /** Botania's Stone of Temperance caps the shatterer at rank B. */
    public static int temperance(int level) {
        return Math.min(level, 2);
    }

    public int blockCount() {
        return (end[0] - begin[0] + 1) * (end[1] - begin[1] + 1) * (end[2] - begin[2] + 1);
    }
}
