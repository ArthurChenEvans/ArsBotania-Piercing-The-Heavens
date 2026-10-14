package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The Divine Blade: several top-tier blades' abilities in one, each at its original strength. */
class DivineBladeTest {

    @Test
    void combinesTheFiveBlades() {
        assertEquals(5, DivineBlade.Ability.values().length);
    }

    @Test
    void terraBeamOnlyOnAFullStrengthSwingLikeTheTerraBlade() {
        assertTrue(DivineBlade.firesBeam(1.0f));
        assertFalse(DivineBlade.firesBeam(0.99f));
    }

    @Test
    void pixiesGetTheElementiumSwordsTwoExtraDamage() {
        assertEquals(4f, DivineBlade.pixieDamage(false));
        assertEquals(6f, DivineBlade.pixieDamage(true));
    }

    @Test
    void addsElementiumSwordsPixieChance() {
        assertEquals(0.05, DivineBlade.PIXIE_CHANCE, 1e-9);
    }

    @Test
    void enchantersSwordSpellsCannotCarryTheirOwnCastMethod() {
        // Ars's Enchanter's Sword rejects spells with a cast method and prepends Touch itself.
        assertFalse(DivineBlade.acceptsSpell(true));
        assertTrue(DivineBlade.acceptsSpell(false));
    }
}
