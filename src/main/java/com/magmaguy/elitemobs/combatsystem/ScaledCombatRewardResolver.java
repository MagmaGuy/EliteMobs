package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import org.bukkit.entity.Player;

/**
 * Resolves the reward level for a player killing an elite.
 * <p>
 * For scaled-combat elites, rewards should match the player's combat level
 * instead of the elite's stored world/spawn level.
 */
public class ScaledCombatRewardResolver {

    private ScaledCombatRewardResolver() {
    }

    public static int getRewardLevel(EliteEntity eliteEntity, Player player) {
        if (eliteEntity == null) return 1;
        if (player == null || !eliteEntity.isScaledCombat()) return Math.max(1, eliteEntity.getLevel());
        return Math.max(1, CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
    }
}
