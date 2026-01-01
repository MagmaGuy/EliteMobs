package com.magmaguy.elitemobs.config.skillbonuses;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.magmacore.config.CustomConfig;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Configuration loader for skill bonuses.
 * <p>
 * Loads all skill bonus YAML files from the skillbonuses directory.
 * Each skill type (SWORDS, AXES, etc.) has its own set of skills.
 */
public class SkillBonusesConfig extends CustomConfig {

    @Getter
    private static HashMap<String, SkillBonusConfigFields> skillBonuses = new HashMap<>();

    public SkillBonusesConfig() {
        super("skillbonuses", "com.magmaguy.elitemobs.config.skillbonuses.premade", SkillBonusConfigFields.class);
        skillBonuses = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            skillBonuses.put(key.toLowerCase(Locale.ROOT), (SkillBonusConfigFields) super.getCustomConfigFieldsHashMap().get(key));
        Logger.info("Loaded " + skillBonuses.size() + " skill bonus configurations.");
    }

    /**
     * Gets a skill bonus configuration by filename.
     *
     * @param filename The config filename (e.g., "swords_lacerate.yml")
     * @return The skill bonus config, or null if not found
     */
    public static SkillBonusConfigFields getSkillBonus(String filename) {
        String key = filename.toLowerCase(Locale.ROOT);
        if (!key.endsWith(".yml")) {
            key = key + ".yml";
        }
        return skillBonuses.get(key);
    }

    /**
     * Gets a skill bonus configuration by skill ID.
     *
     * @param skillId The skill ID (filename without .yml)
     * @return The skill bonus config, or null if not found
     */
    public static SkillBonusConfigFields getBySkillId(String skillId) {
        return getSkillBonus(skillId + ".yml");
    }

    /**
     * Gets all skill bonuses for a specific skill type.
     *
     * @param skillType The skill type to filter by
     * @return List of skill bonus configs for that type
     */
    public static List<SkillBonusConfigFields> getBySkillType(SkillType skillType) {
        List<SkillBonusConfigFields> result = new ArrayList<>();
        for (SkillBonusConfigFields config : skillBonuses.values()) {
            if (config.getSkillType() == skillType) {
                result.add(config);
            }
        }
        return result;
    }

    /**
     * Gets all enabled skill bonuses for a specific skill type.
     *
     * @param skillType The skill type to filter by
     * @return List of enabled skill bonus configs for that type
     */
    public static List<SkillBonusConfigFields> getEnabledBySkillType(SkillType skillType) {
        List<SkillBonusConfigFields> result = new ArrayList<>();
        for (SkillBonusConfigFields config : skillBonuses.values()) {
            if (config.getSkillType() == skillType && config.isEnabled()) {
                result.add(config);
            }
        }
        return result;
    }

    /**
     * Gets all skill bonuses for a specific bonus type.
     *
     * @param bonusType The bonus type to filter by
     * @return List of skill bonus configs for that type
     */
    public static List<SkillBonusConfigFields> getByBonusType(SkillBonusType bonusType) {
        List<SkillBonusConfigFields> result = new ArrayList<>();
        for (SkillBonusConfigFields config : skillBonuses.values()) {
            if (config.getBonusType() == bonusType) {
                result.add(config);
            }
        }
        return result;
    }

    /**
     * Gets all skill bonuses for a specific unlock tier.
     *
     * @param skillType  The skill type to filter by
     * @param unlockTier The unlock tier (1-4)
     * @return List of skill bonus configs at that tier
     */
    public static List<SkillBonusConfigFields> getByTier(SkillType skillType, int unlockTier) {
        List<SkillBonusConfigFields> result = new ArrayList<>();
        for (SkillBonusConfigFields config : skillBonuses.values()) {
            if (config.getSkillType() == skillType && config.getUnlockTier() == unlockTier) {
                result.add(config);
            }
        }
        return result;
    }

    /**
     * Gets all enabled skill bonus configurations.
     *
     * @return List of all enabled configs
     */
    public static List<SkillBonusConfigFields> getAllEnabled() {
        List<SkillBonusConfigFields> result = new ArrayList<>();
        for (SkillBonusConfigFields config : skillBonuses.values()) {
            if (config.isEnabled()) {
                result.add(config);
            }
        }
        return result;
    }

    /**
     * Gets the total count of skill bonuses.
     *
     * @return The number of registered skill bonuses
     */
    public static int getSkillCount() {
        return skillBonuses.size();
    }

    /**
     * Gets the count of enabled skill bonuses.
     *
     * @return The number of enabled skill bonuses
     */
    public static int getEnabledSkillCount() {
        int count = 0;
        for (SkillBonusConfigFields config : skillBonuses.values()) {
            if (config.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Clears all loaded skill bonus configurations.
     */
    public static void shutdown() {
        skillBonuses.clear();
    }
}
