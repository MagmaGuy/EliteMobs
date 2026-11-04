package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEventFilter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.GameClock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * Decides when Thorns hits an elite back. EliteMobs owns the whole effect against elites: vanilla Thorns
 * events on elites are cancelled, because vanilla rolls each armor piece separately (one hit could return the
 * full set's damage up to four times), and while the elite is in its damage cooldown from the player's own
 * hits, vanilla can drop a Thorns hit before EliteMobs ever sees it.
 * <p>
 * Thorns can go off when an elite lands a melee or projectile hit on a player, the hits vanilla Thorns reacts
 * to. Each armor piece rolls vanilla's 15% chance per level against its full level, and the damage counts the
 * whole set's Thorns once. After going off it rests for {@link #COOLDOWN_TICKS} for that elite and player,
 * the same window vanilla's damage cooldown uses, so multishot arrows or rapid script hits cannot stack it.
 */
public final class EliteThorns {

    /** Vanilla's Thorns chance per level, rolled for each armor piece. */
    public static final double PROC_CHANCE_PER_LEVEL = 0.15;

    /** Vanilla's damage cooldown: an entity cannot take another hit for half of its 20 invulnerable ticks. */
    public static final long COOLDOWN_TICKS = 10L;

    private static final Map<HitPair, Long> LAST_PROC_TICK = new HashMap<>();

    private EliteThorns() {
    }

    /** Called once an elite's melee or projectile hit on the player has resolved and was not cancelled. */
    public static void onEliteHitPlayer(Player player, EliteEntity eliteEntity) {
        LivingEntity body = eliteEntity.getLivingEntity();
        if (body == null || !body.isValid() || body.isDead() || !eliteEntity.isValid()) return;
        ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
        if (inventory == null) return;
        int[] levels = inventory.getArmorThornsLevels(true);
        if (totalLevel(levels) <= 0) return;

        long now = GameClock.getCurrentTick();
        // GameClock.shutdown() resets the clock to 0, so entries from the future are stale too.
        LAST_PROC_TICK.values().removeIf(tick -> now < tick || now - tick >= COOLDOWN_TICKS);
        HitPair pair = new HitPair(player.getUniqueId(), eliteEntity.getEliteUUID());
        if (LAST_PROC_TICK.containsKey(pair)) return;
        if (!rollsProc(levels, ThreadLocalRandom.current())) return;

        LAST_PROC_TICK.put(pair, now);
        EliteMobDamagedByPlayerEventFilter.applyThornsHit(player, eliteEntity);
    }

    /** Thorns levels summed across the armor pieces; every level counts. */
    public static int totalLevel(int[] levels) {
        int total = 0;
        for (int level : levels) total += Math.max(0, level);
        return total;
    }

    /** True when at least one armor piece passes its 15%-per-level roll. */
    static boolean rollsProc(int[] levels, RandomGenerator random) {
        for (int level : levels)
            if (level > 0 && random.nextDouble() < level * PROC_CHANCE_PER_LEVEL) return true;
        return false;
    }

    private record HitPair(UUID player, UUID elite) {
    }
}
