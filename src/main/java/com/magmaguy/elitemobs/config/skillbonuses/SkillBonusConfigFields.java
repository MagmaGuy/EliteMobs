package com.magmaguy.elitemobs.config.skillbonuses;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;
import java.util.Locale;

/**
 * Base configuration fields for skill bonuses.
 * <p>
 * Each skill bonus has a YAML config file defining its properties,
 * including unlock tier, bonus type, and scaling values.
 */
public class SkillBonusConfigFields extends CustomConfigFields {

    @Getter
    private String name = "Skill";

    @Getter
    private List<String> description = List.of("A skill bonus");

    @Getter
    private SkillType skillType = SkillType.SWORDS;

    @Getter
    private SkillBonusType bonusType = SkillBonusType.PASSIVE;

    @Getter
    private int unlockTier = 1;

    @Getter
    private double baseValue = 1.0;

    @Getter
    private double scalingPerLevel = 0.1;

    @Getter
    private double procChance = 0.0;

    @Getter
    private double cooldownSeconds = 0.0;

    @Getter
    private int maxStacks = 0;

    @Getter
    private double stackBonus = 0.0;

    @Getter
    private Material displayMaterial = Material.IRON_SWORD;

    @Getter
    private int customModelData = 0;

    @Getter
    private String skillId;

    @Getter
    protected List<String> loreTemplates = List.of();

    @Getter
    protected String formattedBonusTemplate = "";

    /**
     * Fallback constructor for user-created or orphaned config files.
     * Used by MagmaCore when no matching premade config class exists.
     */
    public SkillBonusConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
        this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Constructor for premade skill config files.
     */
    public SkillBonusConfigFields(String filename,
                                   boolean isEnabled,
                                   String name,
                                   List<String> description,
                                   SkillType skillType,
                                   SkillBonusType bonusType,
                                   int unlockTier,
                                   double baseValue,
                                   double scalingPerLevel,
                                   Material displayMaterial) {
        super(filename, isEnabled);
        this.name = name;
        this.description = description;
        this.skillType = skillType;
        this.bonusType = bonusType;
        this.unlockTier = unlockTier;
        this.baseValue = baseValue;
        this.scalingPerLevel = scalingPerLevel;
        this.displayMaterial = displayMaterial;
        // Generate skillId from filename
        this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Constructor for proc-type skills.
     */
    public SkillBonusConfigFields(String filename,
                                   boolean isEnabled,
                                   String name,
                                   List<String> description,
                                   SkillType skillType,
                                   int unlockTier,
                                   double baseValue,
                                   double scalingPerLevel,
                                   double procChance,
                                   Material displayMaterial) {
        super(filename, isEnabled);
        this.name = name;
        this.description = description;
        this.skillType = skillType;
        this.bonusType = SkillBonusType.PROC;
        this.unlockTier = unlockTier;
        this.baseValue = baseValue;
        this.scalingPerLevel = scalingPerLevel;
        this.procChance = procChance;
        this.displayMaterial = displayMaterial;
        this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Constructor for cooldown-type skills.
     */
    public SkillBonusConfigFields(String filename,
                                   boolean isEnabled,
                                   String name,
                                   List<String> description,
                                   SkillType skillType,
                                   int unlockTier,
                                   double baseValue,
                                   double scalingPerLevel,
                                   double cooldownSeconds,
                                   Material displayMaterial,
                                   boolean isCooldownType) {
        super(filename, isEnabled);
        this.name = name;
        this.description = description;
        this.skillType = skillType;
        this.bonusType = SkillBonusType.COOLDOWN;
        this.unlockTier = unlockTier;
        this.baseValue = baseValue;
        this.scalingPerLevel = scalingPerLevel;
        this.cooldownSeconds = cooldownSeconds;
        this.displayMaterial = displayMaterial;
        this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Constructor for skills with both proc chance and cooldown.
     * If isCooldownType is true, uses COOLDOWN type, otherwise uses PROC type.
     */
    public SkillBonusConfigFields(String filename,
                                   boolean isEnabled,
                                   String name,
                                   List<String> description,
                                   SkillType skillType,
                                   int unlockTier,
                                   double baseValue,
                                   double scalingPerLevel,
                                   double procChance,
                                   double cooldownSeconds,
                                   Material displayMaterial,
                                   boolean isCooldownType) {
        super(filename, isEnabled);
        this.name = name;
        this.description = description;
        this.skillType = skillType;
        this.bonusType = isCooldownType ? SkillBonusType.COOLDOWN : SkillBonusType.PROC;
        this.unlockTier = unlockTier;
        this.baseValue = baseValue;
        this.scalingPerLevel = scalingPerLevel;
        this.procChance = procChance;
        this.cooldownSeconds = cooldownSeconds;
        this.displayMaterial = displayMaterial;
        this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
    }

    /**
     * Constructor for stacking-type skills.
     */
    public SkillBonusConfigFields(String filename,
                                   boolean isEnabled,
                                   String name,
                                   List<String> description,
                                   SkillType skillType,
                                   int unlockTier,
                                   int maxStacks,
                                   double stackBonus,
                                   double scalingPerLevel,
                                   Material displayMaterial) {
        super(filename, isEnabled);
        this.name = name;
        this.description = description;
        this.skillType = skillType;
        this.bonusType = SkillBonusType.STACKING;
        this.unlockTier = unlockTier;
        this.maxStacks = maxStacks;
        this.stackBonus = stackBonus;
        this.scalingPerLevel = scalingPerLevel;
        this.displayMaterial = displayMaterial;
        this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.name = translatable(filename, "name", processString("name", name, "Skill", true));
        this.description = translatable(filename, "description", processStringList("description", description, description, true));

        // Process skill type
        String skillTypeStr = processString("skillType", skillType.name(), skillType.name(), true);
        try {
            this.skillType = SkillType.valueOf(skillTypeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            this.skillType = SkillType.SWORDS;
        }

        // Process bonus type
        String bonusTypeStr = processString("bonusType", bonusType.name(), bonusType.name(), true);
        try {
            this.bonusType = SkillBonusType.valueOf(bonusTypeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            this.bonusType = SkillBonusType.PASSIVE;
        }

        this.unlockTier = processInt("unlockTier", unlockTier, 1, true);
        this.baseValue = processDouble("baseValue", baseValue, 1.0, true);
        this.scalingPerLevel = processDouble("scalingPerLevel", scalingPerLevel, 0.1, true);

        // Type-specific fields
        this.procChance = processDouble("procChance", procChance, 0.0, false);
        this.cooldownSeconds = processDouble("cooldownSeconds", cooldownSeconds, 0.0, false);
        this.maxStacks = processInt("maxStacks", maxStacks, 0, false);
        this.stackBonus = processDouble("stackBonus", stackBonus, 0.0, false);

        // Display
        String materialStr = processString("displayMaterial", displayMaterial.name(), displayMaterial.name(), true);
        try {
            this.displayMaterial = Material.valueOf(materialStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            this.displayMaterial = Material.IRON_SWORD;
        }
        this.customModelData = processInt("customModelData", customModelData, 0, false);

        // Generate skill ID from filename
        this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);

        // Translatable lore and formatted bonus templates
        this.loreTemplates = translatable(filename, "loreTemplates", processStringList("loreTemplates", loreTemplates, loreTemplates, false));
        this.formattedBonusTemplate = translatable(filename, "formattedBonusTemplate", processString("formattedBonusTemplate", formattedBonusTemplate, formattedBonusTemplate, false));

        processAdditionalFields();
    }

    /**
     * Override in subclasses to process additional fields.
     */
    public void processAdditionalFields() {
    }

    /**
     * Gets the required level for this skill's unlock tier.
     *
     * @return The required level
     */
    public int getRequiredLevel() {
        return switch (unlockTier) {
            case 1 -> 10;
            case 2 -> 25;
            case 3 -> 50;
            case 4 -> 75;
            default -> 100;
        };
    }

    /**
     * Calculates the bonus value at a given skill level.
     *
     * @param skillLevel The player's skill level
     * @return The calculated bonus value
     */
    public double calculateValue(int skillLevel) {
        return Math.min(10.0, baseValue + (skillLevel * scalingPerLevel));
    }

    /**
     * Calculates the proc chance at a given skill level.
     * Proc chance increases slightly with level.
     *
     * @param skillLevel The player's skill level
     * @return The calculated proc chance (0.0 to 1.0)
     */
    public double calculateProcChance(int skillLevel) {
        // Base proc chance + 0.2% per level
        return Math.min(1.0, procChance + (skillLevel * 0.002));
    }

    /**
     * Calculates the cooldown at a given skill level.
     * Cooldown decreases slightly with level.
     *
     * @param skillLevel The player's skill level
     * @return The calculated cooldown in seconds
     */
    public double calculateCooldown(int skillLevel) {
        // Reduce cooldown by 0.5% per level, minimum 50% of base
        double reduction = 1.0 - (skillLevel * 0.005);
        return Math.max(cooldownSeconds * 0.5, cooldownSeconds * reduction);
    }
}
