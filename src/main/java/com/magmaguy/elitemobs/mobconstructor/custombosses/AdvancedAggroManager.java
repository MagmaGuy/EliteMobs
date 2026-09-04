package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.Map;
import java.util.UUID;

public final class AdvancedAggroManager {

    private static final double TARGETING_RADIUS = 35D;
    private static final double TARGETING_RADIUS_SQUARED = TARGETING_RADIUS * TARGETING_RADIUS;

    private AdvancedAggroManager() {
    }

    /**
     * Selects the nearby player with the most accumulated threat. Damage contribution is
     * intentionally not consulted here: Loud Strikes modifies the separate threat ledger so a
     * lower-damage tank can hold a boss's attention.
     */
    public static void updateTarget(EliteEntity eliteEntity) {
        if (eliteEntity == null) return;
        if (!(eliteEntity.getLivingEntity() instanceof Mob mob)) return;

        Player forcedTarget = resolveForcedTarget(eliteEntity);
        if (forcedTarget != null) {
            if (mob.getTarget() == null || !mob.getTarget().getUniqueId().equals(forcedTarget.getUniqueId()))
                mob.setTarget(forcedTarget);
            return;
        }
        if (eliteEntity.getAggro().isEmpty()) return;

        Player highestThreatPlayer = null;
        double highestThreat = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Player, Double> entry : eliteEntity.getAggro().entrySet()) {
            Player player = entry.getKey();
            Double threat = entry.getValue();
            if (!isEligibleTarget(mob, player)) continue;
            if (threat == null || !Double.isFinite(threat) || threat <= highestThreat) continue;
            highestThreatPlayer = player;
            highestThreat = threat;
        }

        if (highestThreatPlayer == null) return;

        if (mob.getTarget() == null || !mob.getTarget().getUniqueId().equals(highestThreatPlayer.getUniqueId()))
            mob.setTarget(highestThreatPlayer);
    }

    /** Rewrites ordinary AI retarget attempts while a valid taunt lease is live. */
    public static boolean enforceForcedTarget(
            EliteEntity eliteEntity,
            EntityTargetLivingEntityEvent event) {
        Player forcedTarget = resolveForcedTarget(eliteEntity);
        if (forcedTarget == null) return false;
        if (event.getTarget() == null
                || !event.getTarget().getUniqueId().equals(forcedTarget.getUniqueId()))
            event.setTarget(forcedTarget);
        return true;
    }

    private static Player resolveForcedTarget(EliteEntity eliteEntity) {
        if (!(eliteEntity.getLivingEntity() instanceof Mob mob)) return null;
        UUID targetId = eliteEntity.getForcedTargetPlayerId();
        if (targetId == null) return null;
        Player player = Bukkit.getPlayer(targetId);
        if (isEligibleTarget(mob, player)) return player;
        eliteEntity.clearForcedTarget(targetId);
        return null;
    }

    private static boolean isEligibleTarget(Mob mob, Player player) {
        if (player == null || !player.isOnline() || player.isDead() || !player.isValid()) return false;
        if (player.getGameMode() != GameMode.SURVIVAL
                && player.getGameMode() != GameMode.ADVENTURE) return false;
        if (!player.getWorld().equals(mob.getWorld())) return false;
        return player.getLocation().distanceSquared(mob.getLocation()) <= TARGETING_RADIUS_SQUARED;
    }
}
