package com.arthurchenevans.arsbotania.mixin;

import com.arthurchenevans.arsbotania.core.divine.DivineTier;
import com.arthurchenevans.arsbotania.item.DivineArmorItem;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ApparatusRecipeInput;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ArmorUpgradeRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Divine is outside Ars tiers: the armor upgrade ritual must not accept it. */
@Mixin(value = ArmorUpgradeRecipe.class, remap = false)
public abstract class ArmorUpgradeRecipeMixin {
    @Inject(method = "doesReagentMatch", at = @At("HEAD"), cancellable = true)
    private void arsbotania$rejectDivine(ApparatusRecipeInput input, Level level, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (input.catalyst().getItem() instanceof DivineArmorItem && !DivineTier.acceptsUpgrade()) {
            cir.setReturnValue(false);
        }
    }
}
