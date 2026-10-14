package com.arthurchenevans.arsbotania.mixin;

import com.arthurchenevans.arsbotania.item.DivineRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.common.item.rod.ShadedMesaRodItem;
import vazkii.botania.common.item.rod.ShiftingCrustRodItem;

/**
 * Two Botania rods check for their own item on left click (Shifting Crust's block swap,
 * Shaded Mesa's throw). Let the Divine Rod through when that rod's power is selected.
 */
public final class BotaniaRodLeftClickMixin {
    private BotaniaRodLeftClickMixin() {
    }

    static boolean isRodOrDivine(ItemStack stack, Item rod) {
        return stack.is(rod) || (stack.getItem() instanceof DivineRodItem && DivineRodItem.rod(stack) == rod);
    }

    @Mixin(value = ShiftingCrustRodItem.class, remap = false)
    public abstract static class ShiftingCrust {
        @Redirect(method = "onLeftClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
        private boolean arsbotania$acceptDivineRod(ItemStack stack, Item item) {
            return isRodOrDivine(stack, item);
        }
    }

    @Mixin(value = ShadedMesaRodItem.class, remap = false)
    public abstract static class ShadedMesa {
        @Redirect(method = "leftClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
        private static boolean arsbotania$acceptDivineRod(ItemStack stack, Item item) {
            return isRodOrDivine(stack, item);
        }
    }
}
