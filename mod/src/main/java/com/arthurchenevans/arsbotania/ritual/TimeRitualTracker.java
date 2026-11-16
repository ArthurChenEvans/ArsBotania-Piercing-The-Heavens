package com.arthurchenevans.arsbotania.ritual;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.TimeRitual;
import com.arthurchenevans.arsbotania.crafting.DivineRunicAltarRecipe;
import com.arthurchenevans.arsbotania.mixin.RunicAltarAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs the time component's overnight craft on a Runic Altar. Mana that arrives outside
 * sunset is discarded (the craft does not start). Once started, the altar is held one mana
 * short of finishing until sunrise; a gap in mana, or the altar missing ticks (chunk
 * unloaded), fails the ritual: the mana is lost and the ingredients stay in the altar.
 */
public final class TimeRitualTracker {
    private record Tracked(TimeRitual ritual, int lastMana, long lastTick) {
    }

    private static final Map<GlobalPos, Tracked> ACTIVE = new ConcurrentHashMap<>();

    private TimeRitualTracker() {
    }

    public static TimeRitual.State stateAt(Level level, BlockPos pos) {
        Tracked t = ACTIVE.get(GlobalPos.of(level.dimension(), pos));
        return t == null ? null : t.ritual().state();
    }

    public static void tick(Level level, BlockPos pos, RunicAltarBlockEntity altar) {
        GlobalPos key = GlobalPos.of(level.dimension(), pos);
        RunicAltarAccessor access = (RunicAltarAccessor) altar;
        RecipeHolder<RunicAltarRecipe> recipe = access.arsbotania$getCurrentRecipe();
        if (recipe == null || !(recipe.value() instanceof DivineRunicAltarRecipe divine) || !divine.isTimeRitual() || altar.manaToGet <= 0) {
            ACTIVE.remove(key);
            return;
        }

        long now = level.getGameTime();
        int mana = access.arsbotania$getMana();
        Tracked tracked = ACTIVE.get(key);

        if (tracked == null) {
            if (mana <= 0) return;
            TimeRitual ritual = TimeRitual.begin(now, level.getDayTime());
            if (ritual == null) {
                access.arsbotania$setMana(0); // not sunset: the craft does not start
                return;
            }
            tracked = new Tracked(ritual, 0, now - 1);
        }

        boolean missedTicks = now - tracked.lastTick() > 1; // the altar was unloaded
        TimeRitual.State state = tracked.ritual().update(now, mana > tracked.lastMana(), !missedTicks,
                DivineConfig.settings().timeRitualMaxGapTicks());

        if (state == TimeRitual.State.FAILED) {
            access.arsbotania$setMana(0); // the mana is lost; the ingredients stay in the altar
            ACTIVE.remove(key);
            return;
        }
        int capped = Math.min(mana, TimeRitual.manaCap(state, altar.manaToGet));
        access.arsbotania$setMana(capped);
        ACTIVE.put(key, new Tracked(tracked.ritual(), capped, now));
    }
}
