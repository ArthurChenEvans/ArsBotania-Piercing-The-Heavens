package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.combo.DivineCombos;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.hollingsworth.arsnouveau.api.spell.SpellTier;
import com.hollingsworth.arsnouveau.client.renderer.item.SpellBookRenderer;
import com.hollingsworth.arsnouveau.common.items.SpellBook;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

import java.util.List;
import java.util.function.Consumer;

/**
 * The Divine Grimoire: Ars Nouveau's Archmage's spellbook (all glyph tiers, 10 spell slots),
 * which can also top up a short Ars mana bar from nearby Source jars and Botania mana
 * (see {@code SpellResolverMixin}). Held with the Divine Rod, it combos left hand first.
 */
public class DivineGrimoireItem extends SpellBook {
    public static final ResourceLocation TEXTURE = ArsBotania.prefix("textures/item/divine_grimoire.png");

    public DivineGrimoireItem() {
        super(new Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC)
                .component(DataComponents.BASE_COLOR, DyeColor.WHITE)
                .component(DataComponentRegistry.SPELL_CASTER, new SpellCaster(10)), SpellTier.THREE);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (DivineCombos.tryCombo(player, hand, null)) {
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arsbotania.divine").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.arsbotania.grimoire.pool").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.arsbotania.combo").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /** Ars's spellbook model with the Divine texture. */
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new SpellBookRenderer() {
                        @Override
                        public ResourceLocation getTextureLocation(SpellBook book) {
                            return TEXTURE;
                        }
                    };
                }
                return renderer;
            }
        });
    }
}
