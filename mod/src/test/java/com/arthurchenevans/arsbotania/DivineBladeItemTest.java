package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.core.divine.DivineBlade;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.hollingsworth.arsnouveau.api.item.ICasterTool;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import vazkii.botania.api.mana.LensEffectItem;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.BotaniaItems;

import static org.junit.jupiter.api.Assertions.*;

/** The Divine Blade hits like the Terra Blade; its extras come from the other blades, not bigger numbers. */
class DivineBladeItemTest {

    private static ItemStack blade() {
        return new ItemStack(DivineItems.DIVINE_BLADE.get());
    }

    private static double total(ItemStack stack, Holder<Attribute> attribute) {
        return stack.getAttributeModifiers().modifiers().stream()
                .filter(e -> e.attribute().equals(attribute))
                .mapToDouble(e -> e.modifier().amount()).sum();
    }

    @Test
    void hitsExactlyAsHardAndAsFastAsTheTerraBlade() {
        ItemStack terra = new ItemStack(BotaniaItems.TERRA_BLADE);

        assertEquals(total(terra, Attributes.ATTACK_DAMAGE), total(blade(), Attributes.ATTACK_DAMAGE), 1e-9);
        assertEquals(total(terra, Attributes.ATTACK_SPEED), total(blade(), Attributes.ATTACK_SPEED), 1e-9);
    }

    @Test
    void carriesTheElementiumSwordsPixieChance() {
        assertEquals(DivineBlade.PIXIE_CHANCE, total(blade(), PixieHandler.PIXIE_SPAWN_CHANCE), 1e-9);
    }

    @Test
    void neverBreaks() {
        assertFalse(blade().isDamageableItem());
    }

    @Test
    void holdsAnArsSpellLikeTheEnchantersSword() {
        assertInstanceOf(ICasterTool.class, DivineItems.DIVINE_BLADE.get());
        assertNotNull(blade().get(DataComponentRegistry.SPELL_CASTER));
    }

    @Test
    void firesTerraBladeBursts() {
        assertInstanceOf(LensEffectItem.class, DivineItems.DIVINE_BLADE.get(), "Botania calls back into the blade's burst");
    }

    @Test
    void angelicArtShips() {
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/textures/item/divine_blade.png"));
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/models/item/divine_blade.json"));
    }
}
