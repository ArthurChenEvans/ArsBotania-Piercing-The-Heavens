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

## Beta 2: the Singularity (so far)
- One tool that mines as pickaxe, axe, shovel and hoe at netherite level, speed from config
  (default 16; Botania Terrasteel is 9). No durability.
- Terra Shatterer at rank S (`ShattererArea`, Botania's rules): 7x7 area, sneak + use toggles it,
  Ring of Thor adds a rank and the third axis, Stone of Temperance caps it at 3x3.
- Terra Truncator on tree trunks: handed to Botania's own `TERRA_TRUNCATOR.breakOtherBlock`, so
  trees fall exactly as Botania cuts them; sneaking breaks a single log.
- Hooks Botania's `SpecialBlockBreakingHandler` and `SequentialBreaker` (Ring of Loki works).
- Texture: `tools/singularity_texture.py` draws an animated black hole (32 frames).
- The Singularity is a tool, not a weapon (design call: a black hole that hits would one-shot).
- Not yet: hoe use, Source self-repair, stored Ars spell, Divine Well, the full-set death rule.

## Beta 2: Divine Grimoire, Divine Rod and combos (replaces the Divine Well)
No infinite mana: QoL only, everything is paid.
- **Divine Grimoire**: Ars `SpellBook` at tier THREE, 10 slots (the Archmage's book). Shared pool:
  `SpellResolverMixin` tops up a short Ars mana bar just before Ars's mana check, buying the gap
  from Source jars within `jar_range` first (`source_per_ars_mana`), then Botania mana from the
  player's mana items (`botania_mana_per_ars_mana`, default 50). Never past max mana.
- **Divine Rod**: all 13 Botania rods (`RodPower`), one selected at a time; sneak + use in the air
  cycles. Each power runs the Botania rod's own code with the Divine Rod's stack.
  `BotaniaRodLeftClickMixin` lets the Shifting Crust swap and Shaded Mesa throw (which check for
  their own item) accept the Divine Rod.
- **Combos** (`ComboOrder`, `DivineCombos`): hold one in each hand, left acts first. Book left /
  rod right: the spell is cast, and when it lands (`SpellResolveEvent.Post`) the rod acts there.
  Rod left / book right: the rod acts on the looked-at block, then the spell resolves straight onto
  that block (mana checked and spent). Left/right follows the player's handedness setting.
- Not yet: Ars mana paying for rods (one-way pool for now).

## Beta 2: the Divine Blade
The weapon. Terra Blade damage and speed (no stat boost); the other top blades' abilities
folded in, each run by the original code: Terra Blade beam (`TerraBladeItem.getBurst`, the blade
is the burst's lens and delegates to the Terra Blade; air swings use a small client packet),
Thundercaller chain lightning (`THUNDERCALLER.hurtEnemy`), Starcaller falling stars
(`STARCALLER.inventoryTick`), Elementium Sword pixies (+5% chance attribute, +2 pixie damage via
the pixie accessor), and the Enchanter's Sword's spell on hit (Ars `ICasterTool`, Touch added on
scribing). Unbreakable. Look: Terra Blade recoloured pearl, rainbow edge, gold hilt. Lore: spacetime
will not bind to a weapon.
- GameTests use `TestPlayers.survivalServerPlayer`: a server player not joined to the player list,
  because Curios' join sync fails on the helper's mock connection.

## Wings (Divine robes = Flügel Tiara)
- Flight is the tiara's, numbers unchanged (`FlugelFlight`): 1200-tick bar, 35 mana/tick
  (105 once empty), sprint dash with a 40-tick cooldown, sneak glide, Eye of the Flügel works.
  Botania hard-codes its tiara, so `DivineFlightEvents` mirrors its per-tick logic on both sides
  with the robes as the item (tracked per player UUID).
- Wing style (`WingStyle`, stored in `arsbotania:wing_style`): sneak + use cycles
  Divine → none → Botania's eight craftable styles. Botania styles render through Botania's own
  `FlugelTiaraItem.Renderer`; Divine wings are one pair of Botania's Flügel wing recoloured
  (pearl, rainbow edge, full bright), scale 2.3 vs Botania's 1.5, tilted -20 so the tips sweep
  diagonally up (Botania's -60 lies flat), under a rainbow-cycling Flügel halo. Calibrated from
  screenshots: `./gradlew :mod:runVisualCheck -PwingSweep=scale:height:tilt;...` (needs a world
  named `visualcheck` in `mod/run/client/saves`; runs headless under `xvfb-run`). The flight bar is Botania's HUD code.

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
