package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Calculates a player's combat level.
 * <p>
 * Combat level is the average of:
 * - The two highest weapon skill levels
 * - The armor skill level
 */
public class CombatLevelCalculator {

    private CombatLevelCalculator() {
        // Static utility class
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
        // Get all weapon skill levels
        List<Integer> weaponLevels = new ArrayList<>();

        for (SkillType skillType : SkillType.values()) {
            if (skillType == SkillType.ARMOR) continue; // Skip armor, we'll add it separately

            long xp = PlayerData.getSkillXP(playerUUID, skillType);
            int level = SkillXPCalculator.levelFromTotalXP(xp);
            weaponLevels.add(level);
        }

        // Sort descending to get highest first
        Collections.sort(weaponLevels, Collections.reverseOrder());

        // Get the two highest weapon levels (default to 1 if not enough weapons leveled)
        int highestWeapon = weaponLevels.size() > 0 ? weaponLevels.get(0) : 1;
        int secondHighestWeapon = weaponLevels.size() > 1 ? weaponLevels.get(1) : 1;

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
        return "\u00A76\u00A7lCombat Lv. " + combatLevel;
    }
}
