package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.magmacore.util.ChatColorConverter;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for skill-based bonuses.
 * <p>
 * Skill bonuses provide stat increases or special abilities based on skill levels.
 * Players can select up to 3 active skills per weapon type.
 * Skills unlock at different level tiers (10, 25, 50, 75).
 */
public abstract class SkillBonus {

    /**
     * Sends a skill trigger action bar message to the player.
     *
     * @param player The player to send the message to
     * @param skill  The skill that triggered
     */
    public static void sendSkillActionBar(Player player, SkillBonus skill) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColorConverter.convert(
                        DungeonsConfig.getSkillActivationFormat()
                                .replace("$skillName", skill.getBonusName()))));
    }

    @Getter
    protected final SkillType skillType;

    @Getter
    protected final int requiredLevel;

    @Getter
    protected final String bonusName;

    @Getter
    protected final String description;

    @Getter
    protected final SkillBonusType bonusType;

    @Getter
    protected final int unlockTier;

    @Getter
    protected final String skillId;

    @Getter
    @Setter
    protected boolean enabled = true;

    @Getter
    protected SkillBonusConfigFields configFields;

    // Proc tracking for testing - tracks how many times this skill procced per player
    private final Map<UUID, Integer> procCounts = new ConcurrentHashMap<>();

    /**
     * Increments the proc count for a player. Call this when the skill activates/procs.
     *
     * @param player The player who triggered the proc
     */
    public void incrementProcCount(Player player) {
        procCounts.merge(player.getUniqueId(), 1, Integer::sum);
    }

    /**
     * Gets the proc count for a player.
     *
     * @param player The player to check
     * @return The number of times this skill has procced for the player
     */
    public int getProcCount(Player player) {
        return procCounts.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Resets the proc count for a player.
     *
     * @param player The player to reset
     */
    public void resetProcCount(Player player) {
        procCounts.remove(player.getUniqueId());
    }

    /**
     * Resets all proc counts. Used during shutdown or test cleanup.
     */
    public void resetAllProcCounts() {
        procCounts.clear();
    }

    /**
     * Creates a new skill bonus.
     *
     * @param skillType     The skill type this bonus applies to
     * @param requiredLevel Minimum level required to unlock this skill
     * @param bonusName     Display name for the bonus
     * @param description   Description of what the bonus does
     * @param bonusType     The type of skill (PROC, PASSIVE, etc.)
     * @param unlockTier    The tier at which this skill unlocks (1=Lv10, 2=Lv25, 3=Lv50, 4=Lv75)
     * @param skillId       Unique identifier for database storage
     */
    protected SkillBonus(SkillType skillType, int requiredLevel, String bonusName, String description,
                         SkillBonusType bonusType, int unlockTier, String skillId) {
        this.skillType = skillType;
        this.requiredLevel = requiredLevel;
        this.bonusName = bonusName;
        this.description = description;
        this.bonusType = bonusType;
        this.unlockTier = unlockTier;
        this.skillId = skillId;
    }

    /**
     * Creates a new skill bonus from config fields.
     *
     * @param config The configuration fields for this skill
     */
    protected SkillBonus(SkillBonusConfigFields config) {
        this.configFields = config;
        this.skillType = config.getSkillType();
        this.requiredLevel = config.getRequiredLevel();
        this.bonusName = config.getName();
        this.description = String.join(" ", config.getDescription());
        this.bonusType = config.getBonusType();
        this.unlockTier = config.getUnlockTier();
        this.skillId = config.getSkillId();
        this.enabled = config.isEnabled();
    }

    /**
     * Sets the configuration fields for this skill.
     *
     * @param configFields The config fields loaded from YAML
     */
    public void setConfigFields(SkillBonusConfigFields configFields) {
        this.configFields = configFields;
        if (configFields != null) {
            this.enabled = configFields.isEnabled();
        }
    }

    /**
     * Gets the required level for a given unlock tier.
     *
     * @param tier The unlock tier (1-4)
     * @return The required level
     */
    public static int getLevelForTier(int tier) {
        return switch (tier) {
            case 1 -> 10;
            case 2 -> 25;
            case 3 -> 50;
            case 4 -> 75;
            default -> 100;
        };
    }

    /**
     * Sends a stacking skill action bar message showing current stack count.
     *
     * @param player        The player to send the message to
     * @param skill         The stacking skill
     * @param currentStacks Current number of stacks
     * @param maxStacks     Maximum number of stacks
     */
    public static void sendStackingSkillActionBar(Player player, SkillBonus skill, int currentStacks, int maxStacks) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColorConverter.convert(
                        DungeonsConfig.getSkillStackFormat()
                                .replace("$skillName", skill.getBonusName())
                                .replace("$current", String.valueOf(currentStacks))
                                .replace("$max", String.valueOf(maxStacks)))));
    }

    /**
     * Applies placeholder replacements to lore templates from config.
     * Subclasses call this to build their lore from translatable templates.
     *
     * @param replacements Key-value pairs where keys are placeholder names (without $) and values are their replacements
     * @return The processed lore lines, or empty list if no templates configured
     */
    protected List<String> applyLoreTemplates(Map<String, String> replacements) {
        if (configFields == null || configFields.getLoreTemplates().isEmpty()) return List.of();
        List<String> result = new ArrayList<>();
        for (String template : configFields.getLoreTemplates()) {
            String line = template;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                line = line.replace("$" + entry.getKey(), entry.getValue());
            }
            result.add(line);
        }
        return result;
    }

    /**
     * Applies this bonus to a player based on their skill level.
     * Called when bonuses need to be recalculated (e.g., on level up, login).
     *
     * @param player     The player to apply the bonus to
     * @param skillLevel The player's current skill level
     */
    public abstract void applyBonus(Player player, int skillLevel);

    /**
     * Removes this bonus from a player.
     * Called when the bonus needs to be cleared (e.g., on logout, skill deselection).
     *
     * @param player The player to remove the bonus from
     */
    public abstract void removeBonus(Player player);

    /**
     * Called when a player activates (selects) this skill.
     *
     * @param player The player activating the skill
     */
    public abstract void onActivate(Player player);

    /**
     * Called when a player deactivates (deselects) this skill.
     *
     * @param player The player deactivating the skill
     */
    public abstract void onDeactivate(Player player);

    /**
     * Checks if this skill is currently active for a player.
     *
     * @param player The player to check
     * @return true if the skill is active
     */
    public abstract boolean isActive(Player player);

    /**
     * Gets the lore description for the skill menu display.
     *
     * @param skillLevel The player's skill level (for scaling display)
     * @return List of lore lines
     */
    public abstract List<String> getLoreDescription(int skillLevel);

    /**
     * Gets the numeric value of the bonus at a given skill level.
     * Used for display purposes and calculations.
     *
     * @param skillLevel The skill level to calculate for
     * @return The bonus value at that level
     */
    public abstract double getBonusValue(int skillLevel);

    /**
     * Gets a formatted string describing the bonus at a given level.
     * Used for tooltips and UI display.
     *
     * @param skillLevel The skill level to format for
     * @return A formatted description (e.g., "+5.0 Damage")
     */
    public abstract String getFormattedBonus(int skillLevel);

    /**
     * Checks if a player meets the level requirement for this bonus.
     *
     * @param skillLevel The player's skill level
     * @return true if the player meets the minimum level requirement
     */
    public boolean meetsLevelRequirement(int skillLevel) {
        return skillLevel >= requiredLevel;
    }

    /**
     * Checks if this skill can be unlocked at the given level.
     *
     * @param skillLevel The player's skill level
     * @return true if the skill is unlockable
     */
    public boolean canUnlock(int skillLevel) {
        return skillLevel >= getLevelForTier(unlockTier);
    }

    /**
     * Applies placeholder replacements to the formatted bonus template from config.
     *
     * @param replacements Key-value pairs where keys are placeholder names (without $) and values are their replacements
     * @return The processed formatted bonus string, or empty string if no template configured
     */
    protected String applyFormattedBonusTemplate(Map<String, String> replacements) {
        if (configFields == null || configFields.getFormattedBonusTemplate().isEmpty()) return "";
        String result = configFields.getFormattedBonusTemplate();
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("$" + entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Returns the test strategy for verifying this skill in automated testing.
     * Override in subclasses that need special testing approaches.
     *
     * @return The test strategy (default: PROC_COUNT)
     */
    public TestStrategy getTestStrategy() {
        return TestStrategy.PROC_COUNT;
    }

    /**
     * Called on plugin shutdown. Override to clean up static resources.
     */
    public void shutdown() {
        procCounts.clear();
        // Override in subclasses to clean up additional static resources
    }

    /**
     * Gets the scaled bonus value based on player's skill level.
     * Uses the config's baseValue and scalingPerLevel.
     *
     * @param player The player
     * @return The scaled bonus value
     */
    protected double getScaledValue(Player player) {
        if (configFields == null) return 1.0;
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, skillType);
        return configFields.calculateValue(skillLevel);
    }

    /**
     * Gets the scaled bonus value for a specific skill level.
     *
     * @param skillLevel The skill level
     * @return The scaled bonus value
     */
    protected double getScaledValue(int skillLevel) {
        if (configFields == null) return 1.0;
        return configFields.calculateValue(skillLevel);
    }

    /**
     * Gets the proc chance based on player's skill level.
     *
     * @param player The player
     * @return The proc chance (0.0 to 1.0)
     */
    protected double getScaledProcChance(Player player) {
        if (configFields == null) return 0.1;
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, skillType);
        return configFields.calculateProcChance(skillLevel);
    }

    /**
     * Gets the cooldown based on player's skill level.
     *
     * @param player The player
     * @return The cooldown in seconds
     */
    protected double getScaledCooldown(Player player) {
        if (configFields == null) return 30.0;
        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, skillType);
        return configFields.calculateCooldown(skillLevel);
    }

    /**
     * Returns whether this skill affects damage when used offensively.
     * <p>
     * By default, returns true. Override this method in utility skills
     * (like movement speed, knockback reduction) to return false, so they
     * don't incorrectly apply damage multipliers.
     *
     * @return true if this skill modifies damage, false if it's a utility skill
     */
    public boolean affectsDamage() {
        return true;
    }

    /**
     * Defines how the test system should verify this skill works correctly.
     * <ul>
     *   <li>PROC_COUNT - Default. Checks if incrementProcCount was called during combat simulation.</li>
     *   <li>ATTRIBUTE_CHECK - For passive utility skills (speed, knockback resist, reach).
     *       Verifies player attributes changed instead of checking proc counts.</li>
     *   <li>CONDITION_SETUP - For conditional skills that need special test state (draw time, standing still).
     *       The test system sets up the required conditions before testing.</li>
     * </ul>
     */
    public enum TestStrategy {
        PROC_COUNT,
        ATTRIBUTE_CHECK,
        CONDITION_SETUP
    }
}
