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
    private final double fullSetBotaniaToolDiscount;
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
        this.fullSetBotaniaToolDiscount = b.fullSetBotaniaToolDiscount;
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
        b.fullSetBotaniaToolDiscount = fullSetBotaniaToolDiscount;
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

    public double fullSetBotaniaToolDiscount() {
        return fullSetBotaniaToolDiscount;
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
        private double fullSetBotaniaToolDiscount = 0.1;
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

        public Builder fullSetBotaniaToolDiscount(double v) {
            fullSetBotaniaToolDiscount = v;
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
            fraction("fullSetBotaniaToolDiscount", fullSetBotaniaToolDiscount);
            nonNegative("healthRegenPerPiece", healthRegenPerPiece);
            if (healthRegenIntervalTicks <= 0) {
                throw new IllegalArgumentException("healthRegenIntervalTicks must be positive");
            }
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

        private static void fraction(String name, double v) {
            if (v < 0 || v > 1) throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
    }
}
