package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if (eliteEntity == null || eliteEntity.getAggro().isEmpty()) return;
        if (!(eliteEntity.getLivingEntity() instanceof Mob mob)) return;

        List<Player> nearbyPlayers = new ArrayList<>();
        for (Entity entity : mob.getNearbyEntities(TARGETING_RADIUS, TARGETING_RADIUS, TARGETING_RADIUS))
            if (entity.getType().equals(EntityType.PLAYER))
                nearbyPlayers.add((Player) entity);

        Player highestThreatPlayer = null;
        double highestThreat = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Player, Double> entry : eliteEntity.getAggro().entrySet()) {
            Player player = entry.getKey();
            Double threat = entry.getValue();
            if (!isEligibleTarget(mob, player, nearbyPlayers)) continue;
            if (threat == null || !Double.isFinite(threat) || threat <= highestThreat) continue;
            highestThreatPlayer = player;
            highestThreat = threat;
        }

        if (highestThreatPlayer == null) return;

        if (mob.getTarget() == null || !mob.getTarget().getUniqueId().equals(highestThreatPlayer.getUniqueId()))
            mob.setTarget(highestThreatPlayer);
    }

    private static boolean isEligibleTarget(Mob mob, Player player, List<Player> nearbyPlayers) {
        if (player == null || !player.isOnline() || player.isDead() || !player.isValid()) return false;
        if (!nearbyPlayers.contains(player) || !player.getWorld().equals(mob.getWorld())) return false;
        return player.getLocation().distanceSquared(mob.getLocation()) <= TARGETING_RADIUS_SQUARED;
    }
}
