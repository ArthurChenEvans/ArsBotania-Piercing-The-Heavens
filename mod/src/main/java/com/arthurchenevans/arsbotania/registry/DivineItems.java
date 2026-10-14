package com.arthurchenevans.arsbotania.registry;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineLoadout;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.DivineTier;
import com.arthurchenevans.arsbotania.item.DivineArmorItem;
import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DivineItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArsBotania.MODID);
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, ArsBotania.MODID);

    /** Stats come from attribute modifiers driven by config, so the material itself carries none. */
    public static final Holder<ArmorMaterial> DIVINE_MATERIAL = MATERIALS.register("divine", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                for (ArmorItem.Type t : ArmorItem.Type.values()) m.put(t, 0);
            }),
            30, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(ArsBotania.prefix("divine"))), 0, 0));

    private static final Map<DivinePiece, DeferredHolder<Item, DivineArmorItem>> PIECES = new LinkedHashMap<>();

    static {
        for (DivinePiece piece : DivinePiece.all()) {
            PIECES.put(piece, ITEMS.register(piece.registryName(), () -> new DivineArmorItem(piece, DIVINE_MATERIAL)));
        }
    }

    private DivineItems() {
    }

    public static void register(IEventBus modBus) {
        MATERIALS.register(modBus);
        ITEMS.register(modBus);
    }

    public static DivineArmorItem get(DivinePiece piece) {
        return PIECES.get(piece).get();
    }

    public static List<DivineArmorItem> all() {
        List<DivineArmorItem> items = new ArrayList<>();
        PIECES.values().forEach(h -> items.add(h.get()));
        return Collections.unmodifiableList(items);
    }

    /**
     * Ars reads thread slots by tier index. Divine has one slot list; it is repeated for every
     * index Ars could store so a stray tier can never index past the list.
     */
    public static void registerThreadSlots() {
        List<PerkSlot> slots = new ArrayList<>();
        for (int level : DivineTier.threadSlots(DivineConfig.settings())) {
            slots.add(switch (level) {
                case 1 -> PerkSlot.ONE;
                case 2 -> PerkSlot.TWO;
                default -> PerkSlot.THREE;
            });
        }
        List<List<PerkSlot>> perTier = Collections.nCopies(8, List.copyOf(slots));
        for (DivineArmorItem item : all()) {
            PerkRegistry.registerPerkProvider(item, perTier);
        }
    }

    public static DivineLoadout loadoutOf(LivingEntity entity) {
        DivineLoadout.Builder b = DivineLoadout.builder();
        for (ArmorSlot slot : ArmorSlot.values()) {
            if (entity.getItemBySlot(toEquipmentSlot(slot)).getItem() instanceof DivineArmorItem item
                    && item.piece().slot() == slot) {
                b.wear(item.piece());
            }
        }
        return b.build();
    }

    public static EquipmentSlot toEquipmentSlot(ArmorSlot slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
        };
    }
}
