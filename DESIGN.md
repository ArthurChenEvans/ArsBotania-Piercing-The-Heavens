# ArsBotania: Piercing the Heavens — design notes

## Layout
- `core/` — game rules with no Minecraft classes. Every number is a config value
  (`DivineArmorSettings`). Covered by sociable JUnit tests: real objects, no mocks.
  Run with `./gradlew :core:test`.
- `mod/` — NeoForge 1.21.1 glue: items, config spec, events, one mixin. Thin; calls into `core`.
  - `./gradlew :mod:test` — sociable unit tests inside a bootstrapped NeoForge with Ars + Botania loaded.
  - `./gradlew :mod:runGameTestServer` — in-world GameTests on a real player.
  - `./gradlew :mod:runClient` — play it. Jar: `mod/build/libs/`.
- `libs/` — Botania 1.21.1 jar (no official 1.21.1 release yet).

## Wings (Divine robes = Flügel Tiara)
- Flight is the tiara's, numbers unchanged (`FlugelFlight`): 1200-tick bar, 35 mana/tick
  (105 once empty), sprint dash with a 40-tick cooldown, sneak glide, Eye of the Flügel works.
  Botania hard-codes its tiara, so `DivineFlightEvents` mirrors its per-tick logic on both sides
  with the robes as the item (tracked per player UUID).
- Wing style (`WingStyle`, stored in `arsbotania:wing_style`): sneak + use cycles
  Divine → none → Botania's eight craftable styles. Botania styles render through Botania's own
  `FlugelTiaraItem.Renderer`; Divine wings are Botania's Flügel wing recoloured (pearl, rainbow
  edge, full bright, with the Flügel halo). The flight bar is Botania's HUD code.

## Look
Divine has one look, no dye colours: pearl-white cloth with an animated rainbow trim, on the
same Ars Nouveau models. Textures are generated from Ars's own art:
- `tools/analyze_ars_palette.py` — finds the dyed cloth (pixels that differ across Ars's 16
  dye variants) and reports the fixed colours (gold trim, grey metal, outlines).
- `tools/divine_recolor.py` — cloth → pearl (keeps shading), gold → 16-frame rainbow strip
  with `.mcmeta`; writes armor textures, item icon layers and item models.
  Re-run after tweaking: `python3 tools/divine_recolor.py <ars textures/item> <ars models/item> mod/src/main/resources`.
- `DivineArmorRenderer` swaps the texture on Ars's renderer; GeckoLib animates it.

## References (1.21.1)
| Mod | Source |
|---|---|
| Ars Nouveau | `baileyholl/Ars-Nouveau` @ `main` (5.13.x; the `1.21.x` branch is stale 1.21.0) |
| All the Arcanist Gear | `Jarva/All-The-Arcanist-Gear` @ `main` |
| Botania | `VazkiiMods/Botania` @ `1.21.1-porting`, jar in `libs/` (needs NeoForge >= 21.1.229) |
| NeoForge docs | `neoforged/Documentation` → `versioned_docs/version-1.21.1` |

## Beta 1: how Divine armor maps onto the three mods
Pattern taken from All the Arcanist Gear's `AddonArmorItem`:
- Item extends `ArmorItem implements GeoItem`, uses Ars `ArmorPerkHolder` data component,
  attribute modifiers built from config (`ARMOR`, `ARMOR_TOUGHNESS`, Ars `PerkAttributes.MAX_MANA`,
  `PerkAttributes.MANA_REGEN_BONUS`), unbreakable, not damageable.
- **Thread slots:** `PerkRegistry.registerPerkProvider(item, List.of(slots))` — a single entry,
  so the perk holder tier is pinned to index 0 (`DivineTier.normaliseArsTier`). No tier tooltip.
- **Botania tool discount:** every piece implements Botania `ManaDiscountArmor`; Botania sums
  the discount across worn armor, so partial sets naturally get per-piece bonuses.
- **Botania mana regen:** each tick, push `botaniaManaPerTick` into the wearer's mana items via
  `ManaItemHandler.dispatchManaExact`.
- **Health regen:** `HealthRegen` pulses every `healthRegenIntervalTicks`.
- **Botania set bonuses (full Divine set, Botania's own numbers, all configurable):**
  - Terrasteel: 20% tool discount, heals 1 every 80 ticks while hungry (0 < food < 18),
    10 mana into mana tablets every 10 ticks, Ancient Wills on the hood.
  - Manaweave: 40% tool discount, rod proficiency (`ManaProficiencyArmor`).
  - Elementium: per-piece pixie chance via Botania's `pixie_spawn_chance` attribute
    (.11/.17/.15/.09), and full-set pixies apply potions.
  - Tooltip: shift shows a Botania-style "Armor Set" block reusing Botania's lines.
- **Ancient Wills:** the hood is an `AncientWillContainer`, so Botania's own will recipe works.
  Botania's will effects are hard-wired to its Terrasteel helmet, so Divine re-applies them:
  `CriticalHitEvent` (Dharok multiplier, remember target) then `LivingIncomingDamageEvent`
  (Ahrim/Torag/Karil effects, Guthan heal, Verac zeroes armor reduction).
- **Potion pixies:** Botania only adds potions for its Elementium set; an accessor mixin on
  `PixieEntity` lets Divine add one when the summoner wears the full Divine set.
- **Defence:** defaults match Terrasteel (3/8/6/3, toughness 3).

- **Tier lock:** a mixin on Ars `ArmorUpgradeRecipe.doesReagentMatch` rejects Divine pieces;
  without it Ars would accept them (any tier-0 perk holder matches) and push the tier past the slot list.
- **Beta 1 access:** creative tab only (the requirement allows creative tab or a temporary recipe).
- **Config:** `config/arsbotania-common.toml`.

### Interpretation calls (change if wrong)
- "Passive mana regen" = Botania mana into the player's mana items. Ars mana regen comes from
  the Ars attribute separately.
- Full set = any 4 Divine pieces, classes may be mixed. Set bonuses need the full set;
  partial sets get per-piece bonuses only (defence, mana, per-piece discount, pixie chance).
- Combined tool discount is capped at 50% (`max_tool_discount`). The parts (5% per piece,
  Terrasteel 20%, Manaweave 40%) would add to 80%, so a full set sits on the cap; each piece's
  report to Botania is scaled so Botania's sum is exactly the cap. The tooltip shows one
  combined line instead of Botania's separate 20% and 40% lines.
