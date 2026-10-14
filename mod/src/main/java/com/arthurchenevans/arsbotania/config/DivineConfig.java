package com.arthurchenevans.arsbotania.config;

import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Config file for every Divine armor number. Builds a {@link DivineArmorSettings} on load. */
public final class DivineConfig {
    public static final ModConfigSpec SPEC;

    private static final Map<ArmorSlot, ModConfigSpec.IntValue> DEFENCE = new EnumMap<>(ArmorSlot.class);
    private static final ModConfigSpec.DoubleValue TOUGHNESS;
    private static final ModConfigSpec.IntValue BOTANIA_MANA_REGEN;
    private static final ModConfigSpec.DoubleValue BOTANIA_DISCOUNT;
    private static final ModConfigSpec.DoubleValue TERRASTEEL_SET_DISCOUNT;
    private static final ModConfigSpec.DoubleValue MANAWEAVE_SET_DISCOUNT;
    private static final Map<ArmorSlot, ModConfigSpec.DoubleValue> PIXIE_CHANCE = new EnumMap<>(ArmorSlot.class);
    private static final ModConfigSpec.DoubleValue TERRASTEEL_HUNGER_REGEN;
    private static final ModConfigSpec.IntValue TERRASTEEL_HUNGER_REGEN_INTERVAL;
    private static final ModConfigSpec.IntValue TERRASTEEL_TABLET_MANA;
    private static final ModConfigSpec.IntValue TERRASTEEL_TABLET_MANA_INTERVAL;
    private static final ModConfigSpec.DoubleValue HEALTH_REGEN;
    private static final ModConfigSpec.IntValue HEALTH_REGEN_INTERVAL;
    private static final ModConfigSpec.IntValue ARS_MAX_MANA;
    private static final ModConfigSpec.DoubleValue ARS_MANA_REGEN;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> THREAD_SLOTS;

    private static volatile DivineArmorSettings current = DivineArmorSettings.defaults();

    static {
        DivineArmorSettings d = DivineArmorSettings.defaults();
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Divine armor. All values are per piece unless stated.").push("divine_armor");

        b.push("defence");
        for (ArmorSlot slot : ArmorSlot.values()) {
            DEFENCE.put(slot, b.worldRestart().defineInRange(slot.suffix(), d.defence(slot), 0, 1000));
        }
        TOUGHNESS = b.worldRestart().defineInRange("toughness", d.toughnessPerPiece(), 0, 1000);
        b.pop();

        b.push("botania");
        BOTANIA_MANA_REGEN = b.comment("Botania mana pushed into the wearer's mana items every tick")
                .defineInRange("mana_regen_per_tick", d.botaniaManaRegenPerPiece(), 0, 1_000_000);
        BOTANIA_DISCOUNT = b.comment("Fraction taken off Botania tool mana costs")
                .defineInRange("tool_discount", d.botaniaToolDiscountPerPiece(), 0, 1);
        TERRASTEEL_SET_DISCOUNT = b.comment("Terrasteel set bonus: tool discount for wearing all four Divine pieces")
                .defineInRange("terrasteel_set_tool_discount", d.terrasteelSetDiscount(), 0, 1);
        MANAWEAVE_SET_DISCOUNT = b.comment("Manaweave set bonus: tool discount for wearing all four Divine pieces")
                .defineInRange("manaweave_set_tool_discount", d.manaweaveSetDiscount(), 0, 1);
        TERRASTEEL_HUNGER_REGEN = b.comment("Terrasteel set bonus: health healed even when not full on food")
                .defineInRange("terrasteel_hunger_regen", d.terrasteelHungerRegen(), 0, 1000);
        TERRASTEEL_HUNGER_REGEN_INTERVAL = b.defineInRange("terrasteel_hunger_regen_interval_ticks", d.terrasteelHungerRegenIntervalTicks(), 1, 72000);
        TERRASTEEL_TABLET_MANA = b.comment("Terrasteel set bonus: mana generated into mana tablets")
                .defineInRange("terrasteel_tablet_mana", d.terrasteelTabletMana(), 0, 1_000_000);
        TERRASTEEL_TABLET_MANA_INTERVAL = b.defineInRange("terrasteel_tablet_mana_interval_ticks", d.terrasteelTabletManaIntervalTicks(), 1, 72000);
        b.comment("Elementium: chance per piece that taking a hit summons a pixie").push("pixie_chance");
        for (ArmorSlot slot : ArmorSlot.values()) {
            PIXIE_CHANCE.put(slot, b.worldRestart().defineInRange(slot.suffix(), d.pixieChance(slot), 0, 1));
        }
        b.pop();
        b.pop();

        b.push("health");
        HEALTH_REGEN = b.comment("Health restored per pulse")
                .defineInRange("regen_per_pulse", d.healthRegenPerPiece(), 0, 1000);
        HEALTH_REGEN_INTERVAL = b.comment("Ticks between pulses (not per piece)")
                .defineInRange("pulse_interval_ticks", d.healthRegenIntervalTicks(), 1, 72000);
        b.pop();

        b.push("ars_nouveau");
        ARS_MAX_MANA = b.worldRestart().defineInRange("max_mana", d.arsMaxManaPerPiece(), 0, 1_000_000);
        ARS_MANA_REGEN = b.worldRestart().defineInRange("mana_regen", d.arsManaRegenPerPiece(), 0, 10_000);
        THREAD_SLOTS = b.worldRestart().comment("Thread slot levels (1-3), one entry per slot")
                .defineListAllowEmpty("thread_slots", d.threadSlotLevels(), () -> 3,
                        o -> o instanceof Integer i && i >= 1 && i <= DivineArmorSettings.MAX_ARS_SLOT_LEVEL);
        b.pop();

        b.pop();
        SPEC = b.build();
    }

    private DivineConfig() {
    }

    public static DivineArmorSettings settings() {
        return current;
    }

    /** Swaps the live settings without touching the file. For GameTests only. */
    public static void overrideForTesting(DivineArmorSettings settings) {
        current = settings;
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) current = read();
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) current = read();
    }

    private static DivineArmorSettings read() {
        DivineArmorSettings.Builder s = DivineArmorSettings.defaults().toBuilder();
        DEFENCE.forEach((slot, v) -> s.defence(slot, v.get()));
        PIXIE_CHANCE.forEach((slot, v) -> s.pixieChance(slot, v.get()));
        return s.toughnessPerPiece(TOUGHNESS.get())
                .botaniaManaRegenPerPiece(BOTANIA_MANA_REGEN.get())
                .botaniaToolDiscountPerPiece(BOTANIA_DISCOUNT.get())
                .terrasteelSetDiscount(TERRASTEEL_SET_DISCOUNT.get())
                .manaweaveSetDiscount(MANAWEAVE_SET_DISCOUNT.get())
                .terrasteelHungerRegen(TERRASTEEL_HUNGER_REGEN.get().floatValue())
                .terrasteelHungerRegenIntervalTicks(TERRASTEEL_HUNGER_REGEN_INTERVAL.get())
                .terrasteelTabletMana(TERRASTEEL_TABLET_MANA.get())
                .terrasteelTabletManaIntervalTicks(TERRASTEEL_TABLET_MANA_INTERVAL.get())
                .healthRegenPerPiece(HEALTH_REGEN.get())
                .healthRegenIntervalTicks(HEALTH_REGEN_INTERVAL.get())
                .arsMaxManaPerPiece(ARS_MAX_MANA.get())
                .arsManaRegenPerPiece(ARS_MANA_REGEN.get())
                .threadSlotLevels(List.copyOf(THREAD_SLOTS.get()))
                .build();
    }
}
