package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.DivineBlade;
import com.hollingsworth.arsnouveau.api.item.ICasterTool;
import com.hollingsworth.arsnouveau.api.spell.AbstractCastMethod;
import com.hollingsworth.arsnouveau.api.spell.AbstractCaster;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.EntitySpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.IWrappedCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.LensEffectItem;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.BotaniaItemTiers;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraBladeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The Divine Blade. Terra Blade numbers; the other top blades' abilities folded in at their
 * own strength, each run by the original Botania or Ars code:
 * Terra Blade beam, Thundercaller lightning, Starcaller stars, Elementium Sword pixies and
 * the Enchanter's Sword's spell on hit.
 */
public class DivineBladeItem extends SwordItem implements ICasterTool, LensEffectItem {

    public DivineBladeItem() {
        super(BotaniaItemTiers.TERRASTEEL, new Properties()
                .fireResistant()
                .rarity(Rarity.EPIC)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(false))
                .component(DataComponentRegistry.SPELL_CASTER, new SpellCaster())
                .attributes(TerraBladeItem.createAttributes(BotaniaItemTiers.TERRASTEEL,
                                BotaniaItems.ATTACK_DAMAGE_SWORD, BotaniaItems.ATTACK_SPEED_SWORD)
                        .withModifierAdded(PixieHandler.PIXIE_SPAWN_CHANCE,
                                PixieHandler.makeModifier(ArsBotania.prefix("divine_blade"), DivineBlade.PIXIE_CHANCE),
                                EquipmentSlotGroup.MAINHAND)));
    }

    private static LensEffectItem terraBlade() {
        return (LensEffectItem) BotaniaItems.TERRA_BLADE;
    }

    /** Thundercaller's chain lightning, then the blade's own Ars spell. */
    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        BotaniaItems.THUNDERCALLER.hurtEnemy(stack, target, attacker);
        castSpell(stack, target, attacker);
        return true;
    }

    /** As Ars's Enchanter's Sword casts on hit. */
    private void castSpell(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(target.level() instanceof ServerLevel level)) return;
        AbstractCaster<?> caster = getSpellCaster(stack);
        if (caster == null || caster.getSpell().isEmpty()) return;
        IWrappedCaster wrapped = attacker instanceof Player player ? new PlayerCaster(player) : new LivingCaster(attacker);
        Spell spell = caster.modifySpellBeforeCasting(level, attacker, InteractionHand.MAIN_HAND, caster.getSpell());
        SpellContext context = new SpellContext(attacker.level(), spell, attacker, wrapped, stack);
        SpellResolver resolver = attacker instanceof Player ? new SpellResolver(context) : new EntitySpellResolver(context);
        resolver.onCastOnEntity(stack, new EntityHitResult(target).getEntity(), InteractionHand.MAIN_HAND);
    }

    /** Starcaller: stars fall where you look as you swing. */
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        BotaniaItems.STARCALLER.inventoryTick(stack, level, entity, slot, selected);
    }

    // Enchanter's Sword scribing rules: no cast method of its own; Touch is added on scribing.

    @Override
    public boolean isScribedSpellValid(AbstractCaster<?> caster, Player player, InteractionHand hand, ItemStack stack, Spell spell) {
        return DivineBlade.acceptsSpell(spell.unsafeList().stream().anyMatch(s -> s instanceof AbstractCastMethod));
    }

    @Override
    public void sendInvalidMessage(Player player) {
        PortUtil.sendMessageNoSpam(player, Component.translatable("ars_nouveau.sword.invalid"));
    }

    @Override
    public void scribeModifiedSpell(AbstractCaster<?> caster, Player player, InteractionHand hand, ItemStack stack, Spell.Mutable spell) {
        List<AbstractSpellPart> recipe = new ArrayList<>();
        recipe.add(MethodTouch.INSTANCE);
        recipe.addAll(spell.recipe);
        spell.recipe = recipe;
    }

    // Terra Blade bursts carry this blade as their lens; Botania's Terra Blade does the hitting.

    @Override
    public void apply(ItemStack stack, BurstProperties props, Level level) {
        terraBlade().apply(stack, props, level);
    }

    @Override
    public boolean collideBurst(ManaBurst burst, HitResult pos, boolean isManaBlock, boolean shouldKill, ItemStack stack) {
        return terraBlade().collideBurst(burst, pos, isManaBlock, shouldKill, stack);
    }

    @Override
    public void updateBurst(ManaBurst burst, ItemStack stack) {
        terraBlade().updateBurst(burst, stack);
    }

    @Override
    public boolean doParticles(ManaBurst burst, ItemStack stack) {
        return terraBlade().doParticles(burst, stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arsbotania.divine").withStyle(ChatFormatting.GOLD));
        for (DivineBlade.Ability ability : DivineBlade.Ability.values()) {
            tooltip.add(Component.translatable("tooltip.arsbotania.divine_blade." + ability.name().toLowerCase()).withStyle(ChatFormatting.GRAY));
        }
        getInformation(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.arsbotania.divine_blade.lore").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
