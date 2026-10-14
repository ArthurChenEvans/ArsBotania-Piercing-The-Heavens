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
- **Defence:** defaults match Terrasteel (3/8/6/3, toughness 3).

- **Tier lock:** a mixin on Ars `ArmorUpgradeRecipe.doesReagentMatch` rejects Divine pieces;
  without it Ars would accept them (any tier-0 perk holder matches) and push the tier past the slot list.
- **Beta 1 access:** creative tab only (the requirement allows creative tab or a temporary recipe).
- **Config:** `config/arsbotania-common.toml`.

### Interpretation calls (change if wrong)
- "Passive mana regen" = Botania mana into the player's mana items. Ars mana regen comes from
  the Ars attribute separately.
- Full set = any 4 Divine pieces, classes may be mixed. Full set adds a configurable extra
  Botania discount (`fullSetBotaniaToolDiscount`); everything else is per piece.
