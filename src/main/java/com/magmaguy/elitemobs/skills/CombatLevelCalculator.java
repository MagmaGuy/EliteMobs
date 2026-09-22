package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Calculates a player's combat level.
 * <p>
 * Combat level is the average of:
 * - The two highest weapon skill levels
 * - The armor skill level
 */
public class CombatLevelCalculator {
    private static final AtomicLong onlineLevelRevision = new AtomicLong();
    private static OnlineLevelSnapshot onlineLevelSnapshot;

    private CombatLevelCalculator() {
        // Static utility class
    }

    /** Called by the player-data owner whenever skills or the online population change. */
    public static void invalidateOnlineCombatLevel() {
        onlineLevelRevision.incrementAndGet();
    }

    /** Both global placeholders share this result until a relevant value changes. */
    public static synchronized int highestOnlineCombatLevel() {
        long revision = onlineLevelRevision.get();
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int onlineCount = onlinePlayers.size();
        if (onlineLevelSnapshot != null && onlineLevelSnapshot.revision == revision
                && onlineLevelSnapshot.onlineCount == onlineCount) {
            return onlineLevelSnapshot.level;
        }
        int highestLevel = 0;
        for (Player player : onlinePlayers) {
            highestLevel = Math.max(highestLevel, calculateCombatLevel(player.getUniqueId()));
        }
        // Retain the starting revision: a concurrent data load must invalidate this calculation.
        onlineLevelSnapshot = new OnlineLevelSnapshot(revision, onlineCount, highestLevel);
        return highestLevel;
    }

    private record OnlineLevelSnapshot(long revision, int onlineCount, int level) {
    }

    /**
     * Calculates the combat level for a player.
     * <p>
     * Formula: (highest_weapon + second_highest_weapon + armor) / 3
     *
     * @param playerUUID The player's UUID
     * @return The calculated combat level
     */
    public static int calculateCombatLevel(UUID playerUUID) {
        int highestWeapon = 1;
        int secondHighestWeapon = 1;

        for (SkillType skillType : SkillType.values()) {
            if (skillType == SkillType.ARMOR) continue; // Skip armor, we'll add it separately

            long xp = PlayerData.getSkillXP(playerUUID, skillType);
            int level = SkillXPCalculator.levelFromTotalXP(xp);
            if (level >= highestWeapon) {
                secondHighestWeapon = highestWeapon;
                highestWeapon = level;
            } else if (level > secondHighestWeapon) {
                secondHighestWeapon = level;
            }
        }

        // Get armor level
        long armorXP = PlayerData.getSkillXP(playerUUID, SkillType.ARMOR);
        int armorLevel = SkillXPCalculator.levelFromTotalXP(armorXP);

        // Calculate average (rounded down)
        return (highestWeapon + secondHighestWeapon + armorLevel) / 3;
    }

    /**
     * Gets a formatted string for displaying combat level.
     *
     * @param playerUUID The player's UUID
     * @return Formatted combat level string
     */
    public static String getFormattedCombatLevel(UUID playerUUID) {
        int combatLevel = calculateCombatLevel(playerUUID);
        return ChatColorConverter.convert(SkillsConfig.getCombatLevelFormat().replace("$level", String.valueOf(combatLevel)));
    }
}
