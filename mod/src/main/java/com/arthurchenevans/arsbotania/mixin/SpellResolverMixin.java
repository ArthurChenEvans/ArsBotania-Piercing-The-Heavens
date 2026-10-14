package com.arthurchenevans.arsbotania.mixin;

import com.arthurchenevans.arsbotania.combo.GrimoireMana;
import com.arthurchenevans.arsbotania.item.DivineGrimoireItem;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Divine Grimoire spells top up a short Ars mana bar just before Ars checks it. */
@Mixin(value = SpellResolver.class, remap = false)
public abstract class SpellResolverMixin {
    @Inject(method = "enoughMana", at = @At("HEAD"))
    private void arsbotania$topUpFromThePool(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        SpellResolver self = (SpellResolver) (Object) this;
        ItemStack tool = self.spellContext.getCasterTool();
        if (entity instanceof Player player && tool.getItem() instanceof DivineGrimoireItem) {
            GrimoireMana.topUp(player, tool, self.getResolveCost());
        }
    }
}
