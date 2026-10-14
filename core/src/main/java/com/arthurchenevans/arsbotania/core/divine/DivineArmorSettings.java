package com.arthurchenevans.arsbotania.core.divine;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Every tunable number for Divine armor. The mod's config file fills one of these;
 * the defaults here are what a fresh config starts with.
 */
public final class DivineArmorSettings {
    /** Ars Nouveau ships thread slot levels 1 to 3. */
    public static final int MAX_ARS_SLOT_LEVEL = 3;

    private final Map<ArmorSlot, Integer> defence;
    private final double toughnessPerPiece;
    private final int botaniaManaRegenPerPiece;
    private final double botaniaToolDiscountPerPiece;
    private final double terrasteelSetDiscount;
    private final double manaweaveSetDiscount;
    private final Map<ArmorSlot, Double> pixieChance;
    private final float terrasteelHungerRegen;
    private final int terrasteelHungerRegenIntervalTicks;
    private final int terrasteelTabletMana;
    private final int terrasteelTabletManaIntervalTicks;
    private final double healthRegenPerPiece;
    private final int healthRegenIntervalTicks;
    private final int arsMaxManaPerPiece;
    private final double arsManaRegenPerPiece;
    private final List<Integer> threadSlotLevels;

    private DivineArmorSettings(Builder b) {
        this.defence = Map.copyOf(b.defence);
        this.toughnessPerPiece = b.toughnessPerPiece;
        this.botaniaManaRegenPerPiece = b.botaniaManaRegenPerPiece;
        this.botaniaToolDiscountPerPiece = b.botaniaToolDiscountPerPiece;
        this.terrasteelSetDiscount = b.terrasteelSetDiscount;
        this.manaweaveSetDiscount = b.manaweaveSetDiscount;
        this.pixieChance = Map.copyOf(b.pixieChance);
        this.terrasteelHungerRegen = b.terrasteelHungerRegen;
        this.terrasteelHungerRegenIntervalTicks = b.terrasteelHungerRegenIntervalTicks;
        this.terrasteelTabletMana = b.terrasteelTabletMana;
        this.terrasteelTabletManaIntervalTicks = b.terrasteelTabletManaIntervalTicks;
        this.healthRegenPerPiece = b.healthRegenPerPiece;
        this.healthRegenIntervalTicks = b.healthRegenIntervalTicks;
        this.arsMaxManaPerPiece = b.arsMaxManaPerPiece;
        this.arsManaRegenPerPiece = b.arsManaRegenPerPiece;
        this.threadSlotLevels = List.copyOf(b.threadSlotLevels);
    }

    public static DivineArmorSettings defaults() {
        return new Builder().build();
    }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.defence.putAll(defence);
        b.toughnessPerPiece = toughnessPerPiece;
        b.botaniaManaRegenPerPiece = botaniaManaRegenPerPiece;
        b.botaniaToolDiscountPerPiece = botaniaToolDiscountPerPiece;
        b.terrasteelSetDiscount = terrasteelSetDiscount;
        b.manaweaveSetDiscount = manaweaveSetDiscount;
        b.pixieChance.putAll(pixieChance);
        b.terrasteelHungerRegen = terrasteelHungerRegen;
        b.terrasteelHungerRegenIntervalTicks = terrasteelHungerRegenIntervalTicks;
        b.terrasteelTabletMana = terrasteelTabletMana;
        b.terrasteelTabletManaIntervalTicks = terrasteelTabletManaIntervalTicks;
        b.healthRegenPerPiece = healthRegenPerPiece;
        b.healthRegenIntervalTicks = healthRegenIntervalTicks;
        b.arsMaxManaPerPiece = arsMaxManaPerPiece;
        b.arsManaRegenPerPiece = arsManaRegenPerPiece;
        b.threadSlotLevels = threadSlotLevels;
        return b;
    }

    public int defence(ArmorSlot slot) {
        return defence.get(slot);
    }

    public double toughnessPerPiece() {
        return toughnessPerPiece;
    }

    public int botaniaManaRegenPerPiece() {
        return botaniaManaRegenPerPiece;
    }

    public double botaniaToolDiscountPerPiece() {
        return botaniaToolDiscountPerPiece;
    }

    /** Terrasteel's full-set tool discount. */
    public double terrasteelSetDiscount() {
        return terrasteelSetDiscount;
    }

    /** Manaweave's full-set tool discount. */
    public double manaweaveSetDiscount() {
        return manaweaveSetDiscount;
    }

    /** Elementium's chance, per piece, that taking a hit summons a pixie. */
    public double pixieChance(ArmorSlot slot) {
        return pixieChance.get(slot);
    }

    public float terrasteelHungerRegen() {
        return terrasteelHungerRegen;
    }

    public int terrasteelHungerRegenIntervalTicks() {
        return terrasteelHungerRegenIntervalTicks;
    }

    public int terrasteelTabletMana() {
        return terrasteelTabletMana;
    }

    public int terrasteelTabletManaIntervalTicks() {
        return terrasteelTabletManaIntervalTicks;
    }

    public double healthRegenPerPiece() {
        return healthRegenPerPiece;
    }

    public int healthRegenIntervalTicks() {
        return healthRegenIntervalTicks;
    }

    public int arsMaxManaPerPiece() {
        return arsMaxManaPerPiece;
    }

    public double arsManaRegenPerPiece() {
        return arsManaRegenPerPiece;
    }

    public List<Integer> threadSlotLevels() {
        return threadSlotLevels;
    }

    public static final class Builder {
        // Terrasteel values (Botania 1.21.1 BotaniaArmorMaterials.TERRASTEEL)
        private final Map<ArmorSlot, Integer> defence = new EnumMap<>(Map.of(
                ArmorSlot.HEAD, 3, ArmorSlot.CHEST, 8, ArmorSlot.LEGS, 6, ArmorSlot.FEET, 3));
        private double toughnessPerPiece = 3;
        private int botaniaManaRegenPerPiece = 5;
        private double botaniaToolDiscountPerPiece = 0.05;
        // Botania full-set values: Terrasteel 20%, Manaweave 40%
        private double terrasteelSetDiscount = 0.2;
        private double manaweaveSetDiscount = 0.4;
        // Botania Elementium per-piece pixie chances
        private final Map<ArmorSlot, Double> pixieChance = new EnumMap<>(Map.of(
                ArmorSlot.HEAD, 0.11, ArmorSlot.CHEST, 0.17, ArmorSlot.LEGS, 0.15, ArmorSlot.FEET, 0.09));
        // Botania Terrasteel helmet: 1 health every 80 ticks, 10 tablet mana every 10 ticks
        private float terrasteelHungerRegen = 1f;
        private int terrasteelHungerRegenIntervalTicks = 80;
        private int terrasteelTabletMana = 10;
        private int terrasteelTabletManaIntervalTicks = 10;
        private double healthRegenPerPiece = 0.5;
        private int healthRegenIntervalTicks = 40;
        private int arsMaxManaPerPiece = 200;
        private double arsManaRegenPerPiece = 4;
        private List<Integer> threadSlotLevels = List.of(3, 3, 3);

        private Builder() {
        }

        public Builder defence(ArmorSlot slot, int value) {
            defence.put(slot, value);
            return this;
        }

        public Builder toughnessPerPiece(double v) {
            toughnessPerPiece = v;
            return this;
        }

        public Builder botaniaManaRegenPerPiece(int v) {
            botaniaManaRegenPerPiece = v;
            return this;
        }

        public Builder botaniaToolDiscountPerPiece(double v) {
            botaniaToolDiscountPerPiece = v;
            return this;
        }

        public Builder terrasteelSetDiscount(double v) {
            terrasteelSetDiscount = v;
            return this;
        }

        public Builder manaweaveSetDiscount(double v) {
            manaweaveSetDiscount = v;
            return this;
        }

        public Builder pixieChance(ArmorSlot slot, double v) {
            pixieChance.put(slot, v);
            return this;
        }

        public Builder terrasteelHungerRegen(float v) {
            terrasteelHungerRegen = v;
            return this;
        }

        public Builder terrasteelHungerRegenIntervalTicks(int v) {
            terrasteelHungerRegenIntervalTicks = v;
            return this;
        }

        public Builder terrasteelTabletMana(int v) {
            terrasteelTabletMana = v;
            return this;
        }

        public Builder terrasteelTabletManaIntervalTicks(int v) {
            terrasteelTabletManaIntervalTicks = v;
            return this;
        }

        public Builder healthRegenPerPiece(double v) {
            healthRegenPerPiece = v;
            return this;
        }

        public Builder healthRegenIntervalTicks(int v) {
            healthRegenIntervalTicks = v;
            return this;
        }

        public Builder arsMaxManaPerPiece(int v) {
            arsMaxManaPerPiece = v;
            return this;
        }

        public Builder arsManaRegenPerPiece(double v) {
            arsManaRegenPerPiece = v;
            return this;
        }

        public Builder threadSlotLevels(List<Integer> v) {
            threadSlotLevels = List.copyOf(v);
            return this;
        }

        public DivineArmorSettings build() {
            defence.forEach((slot, v) -> nonNegative("defence." + slot, v));
            nonNegative("toughnessPerPiece", toughnessPerPiece);
            nonNegative("botaniaManaRegenPerPiece", botaniaManaRegenPerPiece);
            fraction("botaniaToolDiscountPerPiece", botaniaToolDiscountPerPiece);
            fraction("terrasteelSetDiscount", terrasteelSetDiscount);
            fraction("manaweaveSetDiscount", manaweaveSetDiscount);
            pixieChance.forEach((slot, v) -> fraction("pixieChance." + slot, v));
            nonNegative("terrasteelHungerRegen", terrasteelHungerRegen);
            positive("terrasteelHungerRegenIntervalTicks", terrasteelHungerRegenIntervalTicks);
            nonNegative("terrasteelTabletMana", terrasteelTabletMana);
            positive("terrasteelTabletManaIntervalTicks", terrasteelTabletManaIntervalTicks);
            nonNegative("healthRegenPerPiece", healthRegenPerPiece);
            positive("healthRegenIntervalTicks", healthRegenIntervalTicks);
            nonNegative("arsMaxManaPerPiece", arsMaxManaPerPiece);
            nonNegative("arsManaRegenPerPiece", arsManaRegenPerPiece);
            for (int level : threadSlotLevels) {
                if (level < 1 || level > MAX_ARS_SLOT_LEVEL) {
                    throw new IllegalArgumentException("thread slot level must be 1-" + MAX_ARS_SLOT_LEVEL + ", got " + level);
                }
            }
            return new DivineArmorSettings(this);
        }

        private static void nonNegative(String name, double v) {
            if (v < 0) throw new IllegalArgumentException(name + " must not be negative");
        }

        private static void positive(String name, int v) {
            if (v <= 0) throw new IllegalArgumentException(name + " must be positive");
        }

        private static void fraction(String name, double v) {
            if (v < 0 || v > 1) throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }
}
