package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.client.DivineArmorRenderer;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.BotaniaSetBonuses;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivineBonuses;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.DivineTier;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.api.perk.PerkInstance;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.client.renderer.tile.GenericModel;
import com.hollingsworth.arsnouveau.common.armor.AnimatedMagicArmor;
import com.hollingsworth.arsnouveau.common.items.data.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.item.ManaProficiencyArmor;
import vazkii.botania.api.mana.ManaDiscountArmor;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.proxy.Proxy;

import java.util.List;
import java.util.function.Consumer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * A Divine armor piece. Built on Ars Nouveau's armor so threads, dyeing and the
 * GeckoLib model all behave like the Ars piece it comes from; stats come from config.
 */
public class DivineArmorItem extends AnimatedMagicArmor implements ManaDiscountArmor, ManaProficiencyArmor {
    private final DivinePiece piece;

    public DivineArmorItem(DivinePiece piece, Holder<ArmorMaterial> material) {
        super(material, toArmorType(piece.slot()),
                ItemsRegistry.defaultItemProperties()
                        .stacksTo(1)
                        .fireResistant()
                        .rarity(Rarity.EPIC)
                        .component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())
                        .component(DataComponents.BASE_COLOR, DyeColor.PURPLE),
                arsModel(piece.arcanistClass()));
        this.piece = piece;
    }

    public DivinePiece piece() {
        return piece;
    }

    private static ArmorItem.Type toArmorType(ArmorSlot slot) {
        return switch (slot) {
            case HEAD -> ArmorItem.Type.HELMET;
            case CHEST -> ArmorItem.Type.CHESTPLATE;
            case LEGS -> ArmorItem.Type.LEGGINGS;
            case FEET -> ArmorItem.Type.BOOTS;
        };
    }

    /** Ars's name for the armor model each class uses. */
    public static String arsArmorName(ArcanistClass cls) {
        return switch (cls) {
            case SORCERER -> "light_armor";
            case ARCANIST -> "medium_armor";
            case BATTLEMAGE -> "heavy_armor";
        };
    }

    /** The animated Divine armor texture for a class (pearl cloth, rainbow trim). */
    public static ResourceLocation textureFor(ArcanistClass cls) {
        return ArsBotania.prefix("textures/armor/divine_" + arsArmorName(cls) + ".png");
    }

    /** Same GeckoLib model Ars uses for the base class; the texture is swapped by the renderer. */
    private static GenericModel<AnimatedMagicArmor> arsModel(ArcanistClass cls) {
        String name = arsArmorName(cls);
        return new GenericModel<AnimatedMagicArmor>(name, "item/" + name).withEmptyAnim();
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T entity, ItemStack stack, @Nullable EquipmentSlot slot, @Nullable HumanoidModel<T> original) {
                if (renderer == null) renderer = new DivineArmorRenderer(getArmorModel(), piece.arcanistClass());
                return renderer;
            }
        });
    }

    /** The vanilla armor layer path; GeckoLib draws the armor, but this keeps texture lookups pointed at Divine. */
    @Override
    public @Nullable ResourceLocation getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EquipmentSlot slot, ArmorMaterial.@NotNull Layer layer, boolean innerModel) {
        return textureFor(piece.arcanistClass());
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public void verifyComponentsAfterLoad(@NotNull ItemStack stack) {
        ArmorPerkHolder holder = PerkUtil.getPerkHolder(stack);
        if (holder != null && holder.getTier() != DivineTier.ARS_TIER_INDEX) {
            stack.set(DataComponentRegistry.ARMOR_PERKS, holder.setTier(DivineTier.normaliseArsTier(holder.getTier())));
        }
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        DivineArmorSettings s = DivineConfig.settings();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(getEquipmentSlot());
        ResourceLocation id = ArsBotania.prefix("divine_armor." + piece.slot().suffix());

        ItemAttributeModifiers mods = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, s.defence(piece.slot()), AttributeModifier.Operation.ADD_VALUE), group)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, s.toughnessPerPiece(), AttributeModifier.Operation.ADD_VALUE), group)
                .add(PerkAttributes.MAX_MANA, new AttributeModifier(id, s.arsMaxManaPerPiece(), AttributeModifier.Operation.ADD_VALUE), group)
                .add(PerkAttributes.MANA_REGEN_BONUS, new AttributeModifier(id, s.arsManaRegenPerPiece(), AttributeModifier.Operation.ADD_VALUE), group)
                // Elementium: Botania spawns pixies from this attribute when the wearer is hurt
                .add(PixieHandler.PIXIE_SPAWN_CHANCE, PixieHandler.makeModifier(id, s.pixieChance(piece.slot())), group)
                .build();

        ArmorPerkHolder holder = PerkUtil.getPerkHolder(stack);
        if (holder != null) {
            for (PerkInstance instance : holder.getPerkInstances(stack)) {
                mods = instance.getPerk().applyAttributeModifiers(mods, stack, instance.getSlot().value(), group);
            }
        }
        return mods;
    }

    @Override
    public float getDiscount(ItemStack stack, int slot, Player player, @Nullable ItemStack tool) {
        return (float) DivineBonuses.discountReportedBy(piece.slot(), DivineItems.loadoutOf(player), DivineConfig.settings());
    }

    /** Manaweave set bonus: proficiency with Botania rods. */
    @Override
    public boolean shouldGiveProficiency(ItemStack stack, EquipmentSlot slot, Player player, ItemStack rod) {
        return new BotaniaSetBonuses(DivineConfig.settings()).givesRodProficiency(DivineItems.loadoutOf(player));
    }

    /**
     * Ars prints "Tier N" here; Divine has no tier, so only the threads are listed.
     * The Botania-style set bonus list shows on shift, like Botania's own armor.
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arsbotania.divine").withStyle(ChatFormatting.GOLD));
        ArmorPerkHolder holder = PerkUtil.getPerkHolder(stack);
        if (holder != null) holder.appendPerkTooltip(tooltip, stack);
        TooltipHandler.addOnShift(tooltip, flag, () -> {
            Player player = Proxy.INSTANCE.getClientPlayer();
            int worn = player == null ? 0 : DivineItems.loadoutOf(player).count();
            tooltip.addAll(DivineSetTooltip.lines(stack, worn));
        });
    }
}
