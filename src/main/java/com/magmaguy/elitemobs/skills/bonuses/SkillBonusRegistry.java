package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Registry for managing skill bonuses.
 * <p>
 * This class handles:
 * - Registration of bonus types for each skill
 * - Lookup of skills by ID or type
 * - Application of all bonuses to players
 * - Calculation of total bonuses for display
 */
public class SkillBonusRegistry {

    private static final Map<SkillType, List<SkillBonus>> bonusesBySkill = new EnumMap<>(SkillType.class);
    private static final Map<String, SkillBonus> bonusesById = new HashMap<>();

    private SkillBonusRegistry() {
        // Static utility class
    }

    /**
     * Registers a bonus for a specific skill type.
     *
     * @param bonus The bonus to register
     */
    public static void registerBonus(SkillBonus bonus) {
        bonusesBySkill
                .computeIfAbsent(bonus.getSkillType(), k -> new ArrayList<>())
                .add(bonus);
        bonusesById.put(bonus.getSkillId(), bonus);
    }

    /**
     * Unregisters a bonus.
     *
     * @param bonus The bonus to unregister
     */
    public static void unregisterBonus(SkillBonus bonus) {
        List<SkillBonus> bonuses = bonusesBySkill.get(bonus.getSkillType());
        if (bonuses != null) {
            bonuses.remove(bonus);
        }
        bonusesById.remove(bonus.getSkillId());
    }

    /**
     * Gets a skill bonus by its unique ID.
     *
     * @param skillId The skill ID
     * @return The skill bonus, or null if not found
     */
    public static SkillBonus getSkillById(String skillId) {
        return bonusesById.get(skillId);
    }

    /**
     * Gets all bonuses registered for a skill type.
     *
     * @param skillType The skill type
     * @return List of bonuses (empty if none registered)
     */
    public static List<SkillBonus> getBonuses(SkillType skillType) {
        return bonusesBySkill.getOrDefault(skillType, Collections.emptyList());
    }

    /**
     * Gets all enabled bonuses for a skill type.
     *
     * @param skillType The skill type
     * @return List of enabled bonuses
     */
    public static List<SkillBonus> getEnabledBonuses(SkillType skillType) {
        return getBonuses(skillType).stream()
                .filter(SkillBonus::isEnabled)
                .toList();
    }

    /**
     * Gets all bonuses across all skill types.
     *
     * @return List of all registered bonuses
     */
    public static List<SkillBonus> getAllBonuses() {
        List<SkillBonus> all = new ArrayList<>();
        for (List<SkillBonus> bonusList : bonusesBySkill.values()) {
            all.addAll(bonusList);
        }
        return all;
    }

    /**
     * Gets all enabled bonuses across all skill types.
     *
     * @return List of all enabled bonuses
     */
    public static List<SkillBonus> getAllEnabledBonuses() {
        return getAllBonuses().stream()
                .filter(SkillBonus::isEnabled)
                .toList();
    }

    /**
     * Gets bonuses for a skill type filtered by unlock tier.
     *
     * @param skillType  The skill type
     * @param unlockTier The unlock tier (1-4)
     * @return List of bonuses at that tier
     */
    public static List<SkillBonus> getBonusesByTier(SkillType skillType, int unlockTier) {
        return getBonuses(skillType).stream()
                .filter(b -> b.getUnlockTier() == unlockTier)
                .toList();
    }

    /**
     * Gets all unlockable bonuses for a player's skill level.
     *
     * @param skillType  The skill type
     * @param skillLevel The player's skill level
     * @return List of unlockable bonuses
     */
    public static List<SkillBonus> getUnlockableBonuses(SkillType skillType, int skillLevel) {
        return getEnabledBonuses(skillType).stream()
                .filter(b -> b.canUnlock(skillLevel))
                .toList();
    }

    /**
     * Gets bonuses sorted by unlock tier.
     *
     * @param skillType The skill type
     * @return List of bonuses sorted by tier
     */
    public static List<SkillBonus> getBonusesSortedByTier(SkillType skillType) {
        return getBonuses(skillType).stream()
                .sorted(Comparator.comparingInt(SkillBonus::getUnlockTier))
                .toList();
    }

    /**
     * Applies all applicable bonuses for a specific skill to a player.
     * Only applies bonuses that are active (selected by the player).
     *
     * @param player    The player
     * @param skillType The skill type
     */
    public static void applyBonuses(Player player, SkillType skillType) {
        int level = getPlayerSkillLevel(player, skillType);
        List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), skillType);

        for (String skillId : activeSkillIds) {
            SkillBonus bonus = getSkillById(skillId);
            if (bonus != null && bonus.isEnabled() && bonus.meetsLevelRequirement(level)) {
                bonus.applyBonus(player, level);
            }
        }
    }

    /**
     * Applies all bonuses from all skills to a player.
     * Called on player login or when refreshing all bonuses.
     *
     * @param player The player
     */
    public static void applyAllBonuses(Player player) {
        for (SkillType skillType : SkillType.values()) {
            applyBonuses(player, skillType);
        }
    }

    /**
     * Removes all bonuses from a player.
     * Called on player logout or skill reset.
     *
     * @param player The player
     */
    public static void removeAllBonuses(Player player) {
        for (SkillBonus bonus : getAllBonuses()) {
            if (bonus.isActive(player)) {
                bonus.removeBonus(player);
            }
        }
    }

    /**
     * Gets the total bonus value from a specific skill for a player.
     *
     * @param player    The player
     * @param skillType The skill type
     * @return Map of bonus names to their values
     */
    public static Map<String, Double> getTotalBonuses(Player player, SkillType skillType) {
        Map<String, Double> totals = new LinkedHashMap<>();
        int level = getPlayerSkillLevel(player, skillType);
        List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), skillType);

        for (String skillId : activeSkillIds) {
            SkillBonus bonus = getSkillById(skillId);
            if (bonus != null && bonus.isEnabled() && bonus.meetsLevelRequirement(level)) {
                totals.put(bonus.getBonusName(), bonus.getBonusValue(level));
            }
        }

        return totals;
    }

    /**
     * Gets a formatted list of all active bonuses for a player's skill.
     *
     * @param player    The player
     * @param skillType The skill type
     * @return List of formatted bonus strings
     */
    public static List<String> getFormattedBonuses(Player player, SkillType skillType) {
        List<String> formatted = new ArrayList<>();
        int level = getPlayerSkillLevel(player, skillType);
        List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), skillType);

        for (String skillId : activeSkillIds) {
            SkillBonus bonus = getSkillById(skillId);
            if (bonus != null && bonus.isEnabled() && bonus.meetsLevelRequirement(level)) {
                formatted.add(bonus.getFormattedBonus(level));
            }
        }

        return formatted;
    }

    /**
     * Gets the count of registered skills for a skill type.
     *
     * @param skillType The skill type
     * @return The number of registered skills
     */
    public static int getSkillCount(SkillType skillType) {
        return getBonuses(skillType).size();
    }

    /**
     * Gets the count of enabled skills for a skill type.
     *
     * @param skillType The skill type
     * @return The number of enabled skills
     */
    public static int getEnabledSkillCount(SkillType skillType) {
        return getEnabledBonuses(skillType).size();
    }

    /**
     * Helper method to get a player's skill level.
     */
    public static int getPlayerSkillLevel(Player player, SkillType skillType) {
        long xp = PlayerData.getSkillXP(player.getUniqueId(), skillType);
        return SkillXPCalculator.levelFromTotalXP(xp);
    }

    /**
     * Clears all registered bonuses.
     * Called on plugin shutdown.
     */
    public static void shutdown() {
        // Shutdown each bonus
        for (SkillBonus bonus : getAllBonuses()) {
            bonus.shutdown();
        }
        bonusesBySkill.clear();
        bonusesById.clear();
    }

    /**
     * Initializes and registers all skill bonuses.
     * Called on plugin startup after config is loaded.
     */
    public static void initializeSkillBonuses() {
        // This will be called after SkillBonusesConfig loads all skill configs
        // Individual skills register themselves via registerBonus()
    }
}
