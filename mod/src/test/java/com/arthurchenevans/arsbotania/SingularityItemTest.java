package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.item.SingularityItem;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import vazkii.botania.api.item.SequentialBreaker;
import vazkii.botania.api.item.SpecialBlockBreakingHandler;

import static org.junit.jupiter.api.Assertions.*;

/** Block tags are not loaded in unit tests; tool coverage is checked in SingularityGameTests. */
class SingularityItemTest {

    private static ItemStack singularity() {
        return new ItemStack(DivineItems.SINGULARITY.get());
    }

    @Test
    void neverBreaks() {
        assertFalse(singularity().isDamageableItem());
    }

    @Test
    void plugsIntoBotaniasBreakingHooks() {
        assertInstanceOf(SpecialBlockBreakingHandler.class, DivineItems.SINGULARITY.get());
        assertInstanceOf(SequentialBreaker.class, DivineItems.SINGULARITY.get(), "Ring of Loki support");
    }

    @Test
    void areaModeStartsOnAndToggles() {
        ItemStack s = singularity();
        assertTrue(SingularityItem.isAreaMode(s));
        SingularityItem.setAreaMode(s, false);
        assertFalse(SingularityItem.isAreaMode(s));
    }

    @Test
    void blackHoleArtShipsAnimated() {
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/textures/item/singularity.png"));
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/textures/item/singularity.png.mcmeta"));
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/models/item/singularity.json"));
    }
}
