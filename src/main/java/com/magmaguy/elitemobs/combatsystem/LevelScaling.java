package com.magmaguy.elitemobs.combatsystem;

/**
 * Handles level-based combat scaling calculations.
 * <p>
 * This system ensures that level differences feel consistent across all level ranges.
 * A level 10 player fighting a level 15 mob feels the same difficulty as a level 95
 * player fighting a level 100 mob.
 * <p>
 * <h2>Core Formula</h2>
 * <pre>
 * modifier = SCALING_BASE ^ (levelDifference / LEVELS_PER_POWER_DOUBLE)
 * </pre>
 * <p>
 * Where:
 * <ul>
 *   <li><b>levelDifference</b> = targetLevel - sourceLevel (positive = target is higher)</li>
 *   <li><b>SCALING_BASE</b> = The base multiplier (default 2.0 for doubling)</li>
 *   <li><b>LEVELS_PER_POWER_DOUBLE</b> = How many levels for power to double (default 5)</li>
 * </ul>
 * <p>
 * <h2>Example Modifiers (with defaults)</h2>
 * <table border="1">
 *   <tr><th>Level Difference</th><th>Modifier</th><th>Meaning</th></tr>
 *   <tr><td>-10</td><td>0.25x</td><td>Target is 10 below you - very weak</td></tr>
 *   <tr><td>-5</td><td>0.5x</td><td>Target is 5 below you - easy fight</td></tr>
 *   <tr><td>0</td><td>1.0x</td><td>Same level - balanced combat</td></tr>
 *   <tr><td>+5</td><td>2.0x</td><td>Target is 5 above you - hard fight</td></tr>
 *   <tr><td>+10</td><td>4.0x</td><td>Target is 10 above you - very hard</td></tr>
 * </table>
 * <p>
 * <h2>How This Affects Combat</h2>
 * When fighting an enemy 5 levels above you:
 * <ul>
 *   <li>Enemy effectively has 2x health (takes twice as many hits)</li>
 *   <li>Enemy effectively deals 2x damage (you die twice as fast)</li>
 *   <li>Combined effect: fight is roughly 4x harder</li>
 * </ul>
 * <p>
 * <h2>Customization</h2>
 * Adjust these constants to change the feel:
 * <ul>
 *   <li>Increase LEVELS_PER_POWER_DOUBLE to make level differences matter less</li>
 *   <li>Decrease LEVELS_PER_POWER_DOUBLE to make level differences matter more</li>
 *   <li>Change SCALING_BASE to adjust the curve shape (2.0 = exponential doubling)</li>
 *   <li>Adjust RECOMMENDED_LEVEL_RANGE to change the "viable combat" window</li>
 * </ul>
 */
public class LevelScaling {

    // ========================================
    // CORE SCALING CONSTANTS
    // Modify these to adjust how level differences feel
    // ========================================

    /**
     * The base of the exponential scaling formula.
     * <p>
     * With a value of 2.0, power doubles every LEVELS_PER_POWER_DOUBLE levels.
     * <ul>
     *   <li>2.0 = Power doubles (default, recommended)</li>
     *   <li>1.5 = Power increases by 50%</li>
     *   <li>3.0 = Power triples (very punishing)</li>
     * </ul>
     */
    public static final double SCALING_BASE = 2.0;

    /**
     * How many levels difference results in power multiplication by SCALING_BASE.
     * <p>
     * With default values (SCALING_BASE=2.0, LEVELS_PER_POWER_DOUBLE=5):
     * <ul>
     *   <li>5 levels higher = 2x stronger</li>
     *   <li>10 levels higher = 4x stronger</li>
     *   <li>5 levels lower = 0.5x (half) strength</li>
     * </ul>
     * <p>
     * Adjusting this value:
     * <ul>
     *   <li>Lower value (e.g., 3) = Levels matter MORE, smaller viable range</li>
     *   <li>Higher value (e.g., 10) = Levels matter LESS, larger viable range</li>
     * </ul>
     */
    public static final double LEVELS_PER_POWER_DOUBLE = 5.0;

    // ========================================
    // COMBAT RANGE CONSTANTS
    // Define what level ranges are considered viable
    // ========================================

    /**
     * The recommended maximum level difference for challenging but fair combat.
     * <p>
     * Players can attempt content up to this many levels above them, though
     * it will be significantly harder. Content beyond this is possible but
     * may be frustrating or require exceptional skill/gear.
     */
    public static final int RECOMMENDED_LEVEL_RANGE = 5;

    /**
     * The absolute maximum level difference before combat becomes nearly impossible.
     * <p>
     * At this difference, the modifier is extreme (with defaults: 4x at +10 levels).
     * Beyond this, players should be warned or prevented from engaging.
     */
    public static final int EXTREME_LEVEL_RANGE = 10;

    /**
     * Level difference at which content becomes trivial.
     * <p>
     * When a player is this many levels above content, consider reducing
     * rewards or skipping combat entirely.
     */
    public static final int TRIVIAL_LEVEL_RANGE = -10;

    // ========================================
    // MODIFIER CAPS (Optional safety limits)
    // Prevent extreme values at very large level differences
    // ========================================

    /**
     * Maximum modifier value (caps how much stronger enemies can be).
     * <p>
     * Set to a high value to effectively disable the cap.
     * With defaults, this caps at +15 level difference (8x modifier).
     */
    public static final double MAX_MODIFIER = 8.0;

    /**
     * Minimum modifier value (caps how weak enemies can be).
     * <p>
     * Prevents enemies from becoming completely trivial.
     * With defaults, this caps at -15 level difference (0.125x modifier).
     */
    public static final double MIN_MODIFIER = 0.125;

    /**
     * Maximum mob health allowed.
     * <p>
     * Set to Double.MAX_VALUE to allow theoretically unlimited health.
     * The actual limit is configured in spigot.yml via settings.attribute.maxHealth.max
     */
    public static final double MINECRAFT_MAX_HEALTH = Double.MAX_VALUE;

    // ========================================
    // CALCULATION METHODS
    // ========================================
    /**
     * Base HP constant for the exponential scaling formula.
     * <p>
     * This is tuned so that level 25 mobs have approximately 70 HP.
     * Formula: HP = BASE_MOB_HP * 2^(level / 5)
     * <p>
     * At level 25: 2.1875 * 2^5 = 2.1875 * 32 = 70 HP
     */
    public static final double BASE_MOB_HP = 2.1875;

    /**
     * Target number of hits for a player to die when fighting same-level content
     * with full appropriate gear and defensive skills.
     * <p>
     * This constant is used in boss damage scaling to ensure consistent difficulty.
     * At any level, a properly geared player should survive approximately this many
     * normal hits from same-level elite mobs.
     */
    public static final double TARGET_HITS_TO_KILL_PLAYER = 5.0;

    /**
     * Target number of hits for a player to die when fighting content 10 levels above them.
     * <p>
     * Used to calculate the boss damage level scaling exponent.
     * With values of 5 hits at same level and 2 hits at +10, the scaling
     * feels challenging but not impossible for skilled players.
     */
    public static final double TARGET_HITS_AT_PLUS_10 = 2.0;

    /**
     * Controls how fast the skill-based damage adjustment scales exponentially
     * in the defensive (elite→player) damage formula.
     * <p>
     * Formula: {@code skillAdjustment = 2^((mobLevel - armorSkillLevel) / SKILL_SCALING_RATE)}
     * <ul>
     *   <li>At +7.5 levels: damage doubles</li>
     *   <li>At +15 levels: damage quadruples</li>
     *   <li>At -7.5 levels: damage halves</li>
     * </ul>
     * <p>
     * Note: This is the defensive equivalent of {@link #LEVELS_PER_BOSS_DAMAGE_DOUBLE},
     * which is used for the offensive (old level modifier) path. They happen to share the
     * same value but are conceptually separate constants.
     */
    public static final double SKILL_SCALING_RATE = 7.5;

    /**
     * Levels required for boss damage to double against players.
     * <p>
     * This is calculated from TARGET_HITS_TO_KILL_PLAYER and TARGET_HITS_AT_PLUS_10:
     * <ul>
     *   <li>At same level: 5 hits to kill</li>
     *   <li>At +10 levels: 2 hits to kill</li>
     *   <li>Damage ratio: 5/2 = 2.5x at +10 levels</li>
     *   <li>Formula: 2^(10/x) = 2.5 → x ≈ 7.57</li>
     * </ul>
     * <p>
     * Using 7.5 for cleaner math:
     * <ul>
     *   <li>+5 levels: 2^(5/7.5) = ~1.6x damage (3.1 hits)</li>
     *   <li>+10 levels: 2^(10/7.5) = ~2.5x damage (2 hits)</li>
     *   <li>+15 levels: 2^(15/7.5) = 4x damage (1.25 hits)</li>
     * </ul>
     */
    public static final double LEVELS_PER_BOSS_DAMAGE_DOUBLE = 7.5;

    // ========================================
    // OFFENSIVE FORMULA CONSTANTS (Player → Elite)
    // Mirror the defensive constants for symmetry
    // ========================================

    /**
     * Target number of sword hits to kill a standard elite mob (healthMultiplier=1.0)
     * at matched combat (weapon skill level == mob level, weapon level == mob level).
     * <p>
     * This is the core offensive balance constant. A mob with healthMultiplier=2.0
     * will take exactly 2× this many hits, at ALL levels.
     * <p>
     * Note: This is defined in sword hits (1.6 attacks/sec). Faster weapons deal
     * proportionally less per hit but the same DPS.
     */
    public static final double TARGET_HITS_TO_KILL_MOB = 3.0;

    /**
     * Reference weapon attack speed used to define {@link #TARGET_HITS_TO_KILL_MOB}.
     * <p>
     * Sword speed is 1.6 attacks/sec. All other weapon speeds are normalized
     * against this reference so that DPS remains consistent across weapon types.
     */
    public static final double REFERENCE_ATTACK_SPEED = 1.6;

    /**
     * Controls how fast the skill-based damage adjustment scales exponentially
     * in the offensive (player→elite) damage formula.
     * <p>
     * Formula: {@code skillAdjustment = 2^((weaponSkillLevel - mobLevel) / OFFENSIVE_SKILL_SCALING_RATE)}
     * <ul>
     *   <li>At +7.5 skill levels above mob: damage doubles</li>
     *   <li>At -7.5 skill levels below mob: damage halves</li>
     * </ul>
     * <p>
     * Matches the defensive {@link #SKILL_SCALING_RATE} for symmetric scaling.
     */
    public static final double OFFENSIVE_SKILL_SCALING_RATE = 7.5;

    private LevelScaling() {
        // Utility class - no instantiation
    }

    // ========================================
    // COMBAT APPLICATION METHODS
    // Convenience methods for common combat scenarios
    // ========================================

    /**
     * Calculates the level scaling modifier for combat.
     * <p>
     * This is the core method that determines how level differences affect combat.
     * Apply this modifier to damage dealt or received based on level difference.
     *
     * @param sourceLevel The level of the entity dealing damage or being evaluated
     * @param targetLevel The level of the entity receiving damage or being compared to
     * @return The scaling modifier (1.0 = no change, 2.0 = double, 0.5 = half)
     *
     * <h3>Usage Examples:</h3>
     * <pre>
     * // Player (level 50) attacking Boss (level 55)
     * double modifier = getLevelModifier(50, 55);
     * // modifier = 2.0, so the boss effectively has 2x health
     *
     * // Boss (level 55) attacking Player (level 50)
     * double modifier = getLevelModifier(55, 50);
     * // modifier = 0.5, so player takes 2x damage (inverse)
     * </pre>
     */
    public static double getLevelModifier(int sourceLevel, int targetLevel) {
        int levelDifference = targetLevel - sourceLevel;
        return getLevelModifierFromDifference(levelDifference);
    }

    /**
     * Calculates the level scaling modifier from a raw level difference.
     * <p>
     * Positive difference = target is higher level (harder)
     * Negative difference = target is lower level (easier)
     *
     * @param levelDifference The level difference (target - source)
     * @return The scaling modifier, clamped between MIN_MODIFIER and MAX_MODIFIER
     */
    public static double getLevelModifierFromDifference(int levelDifference) {
        // Core formula: modifier = base ^ (difference / levelsPerDouble)
        double rawModifier = Math.pow(SCALING_BASE, levelDifference / LEVELS_PER_POWER_DOUBLE);

        // Clamp to prevent extreme values
        return clampModifier(rawModifier);
    }

    // ========================================
    // UTILITY METHODS
    // Helper methods for combat range checks
    // ========================================

    /**
     * Clamps a modifier value between MIN_MODIFIER and MAX_MODIFIER.
     *
     * @param modifier The raw modifier value
     * @return The clamped modifier
     */
    public static double clampModifier(double modifier) {
        return Math.max(MIN_MODIFIER, Math.min(MAX_MODIFIER, modifier));
    }

    /**
     * Calculates effective damage when accounting for level difference.
     * <p>
     * When attacking a higher level target, your effective damage is reduced.
     * When attacking a lower level target, your effective damage is increased.
     *
     * @param baseDamage The base damage before level scaling
     * @param attackerLevel The level of the attacker
     * @param defenderLevel The level of the defender
     * @return The level-adjusted damage
     *
     * <h3>Example:</h3>
     * <pre>
     * // Player (50) attacks Boss (55) for 100 base damage
     * double effectiveDamage = calculateEffectiveDamage(100, 50, 55);
     * // effectiveDamage = 50 (halved because boss is 5 levels higher)
     * </pre>
     */
    public static double calculateEffectiveDamage(double baseDamage, int attackerLevel, int defenderLevel) {
        // When defender is higher level, damage is reduced (divide by modifier)
        // When defender is lower level, damage is increased (divide by smaller modifier)
        double modifier = getLevelModifier(attackerLevel, defenderLevel);
        return baseDamage / modifier;
    }

    /**
     * Calculates effective incoming damage when accounting for level difference.
     * <p>
     * When being attacked by a higher level enemy, you take more damage.
     * When being attacked by a lower level enemy, you take less damage.
     *
     * @param baseDamage The base damage before level scaling
     * @param attackerLevel The level of the attacker
     * @param defenderLevel The level of the defender
     * @return The level-adjusted incoming damage
     *
     * <h3>Example:</h3>
     * <pre>
     * // Boss (55) attacks Player (50) for 100 base damage
     * double incomingDamage = calculateIncomingDamage(100, 55, 50);
     * // incomingDamage = 200 (doubled because boss is 5 levels higher)
     * </pre>
     */
    public static double calculateIncomingDamage(double baseDamage, int attackerLevel, int defenderLevel) {
        // When attacker is higher level, damage is increased (multiply by modifier)
        double modifier = getLevelModifier(attackerLevel, defenderLevel);
        return baseDamage * modifier;
    }

    /**
     * Checks if a level difference is within the recommended combat range.
     *
     * @param playerLevel The player's level
     * @param targetLevel The target's level
     * @return true if the target is within ±RECOMMENDED_LEVEL_RANGE
     */
    public static boolean isWithinRecommendedRange(int playerLevel, int targetLevel) {
        int difference = Math.abs(targetLevel - playerLevel);
        return difference <= RECOMMENDED_LEVEL_RANGE;
    }

    /**
     * Checks if a level difference is within the extreme (but possible) combat range.
     *
     * @param playerLevel The player's level
     * @param targetLevel The target's level
     * @return true if the target is within ±EXTREME_LEVEL_RANGE
     */
    public static boolean isWithinExtremeRange(int playerLevel, int targetLevel) {
        int difference = Math.abs(targetLevel - playerLevel);
        return difference <= EXTREME_LEVEL_RANGE;
    }

    // ========================================
    // MOB HP SCALING
    // Level difficulty is now built into mob HP rather than damage modifiers
    // ========================================

    /**
     * Checks if content would be trivial for the player.
     *
     * @param playerLevel The player's level
     * @param contentLevel The content's level
     * @return true if the player is more than TRIVIAL_LEVEL_RANGE above the content
     */
    public static boolean isTrivialContent(int playerLevel, int contentLevel) {
        return (playerLevel - contentLevel) >= Math.abs(TRIVIAL_LEVEL_RANGE);
    }

    /**
     * Gets a difficulty description based on level difference.
     *
     * @param playerLevel The player's level
     * @param targetLevel The target's level
     * @return A string describing the relative difficulty
     */
    public static String getDifficultyDescription(int playerLevel, int targetLevel) {
        int difference = targetLevel - playerLevel;

        if (difference <= TRIVIAL_LEVEL_RANGE) {
            return "Trivial";
        } else if (difference < -RECOMMENDED_LEVEL_RANGE) {
            return "Very Easy";
        } else if (difference < 0) {
            return "Easy";
        } else if (difference == 0) {
            return "Balanced";
        } else if (difference <= RECOMMENDED_LEVEL_RANGE) {
            return "Challenging";
        } else if (difference <= EXTREME_LEVEL_RANGE) {
            return "Very Hard";
        } else {
            return "Extreme";
        }
    }

    /**
     * Gets the modifier value as a human-readable percentage.
     *
     * @param sourceLevel The source level
     * @param targetLevel The target level
     * @return A string like "+100%" or "-50%"
     */
    public static String getModifierAsPercentage(int sourceLevel, int targetLevel) {
        double modifier = getLevelModifier(sourceLevel, targetLevel);
        double percentage = (modifier - 1.0) * 100;

        if (percentage >= 0) {
            return String.format("+%.0f%%", percentage);
        } else {
            return String.format("%.0f%%", percentage);
        }
    }

    /**
     * Calculates mob health using pure exponential level scaling.
     * <p>
     * This replaces the old damage modifier system. Now level difficulty is
     * built directly into mob HP with a guaranteed ratio:
     * <ul>
     *   <li>+5 levels = EXACTLY 2x more HP</li>
     *   <li>-5 levels = EXACTLY 0.5x HP</li>
     *   <li>This holds true at ALL level ranges</li>
     * </ul>
     * <p>
     * The formula is purely exponential:
     * <pre>
     * HP = BASE_MOB_HP * 2^(level / 5)
     * </pre>
     *
     * @param level         The mob's level
     * @param baseEntityHP  The base HP from the entity type (unused in pure exponential, kept for API compatibility)
     * @return The calculated max health for the mob
     *
     * <h3>Example HP Values:</h3>
     * <table border="1">
     *   <tr><th>Level</th><th>HP</th><th>+5 Levels</th></tr>
     *   <tr><td>1</td><td>~2.5</td><td>→ 5 (2x)</td></tr>
     *   <tr><td>5</td><td>~4.4</td><td>→ 8.75 (2x)</td></tr>
     *   <tr><td>10</td><td>~8.75</td><td>→ 17.5 (2x)</td></tr>
     *   <tr><td>25</td><td>~70</td><td>→ 140 (2x)</td></tr>
     *   <tr><td>50</td><td>~2240</td><td>→ 4480 (2x)</td></tr>
     * </table>
     */
    public static double calculateMobHealth(int level, double baseEntityHP) {
        // Pure exponential scaling: guarantees +5 levels = exactly 2x HP at all levels
        double health = BASE_MOB_HP * Math.pow(SCALING_BASE, level / LEVELS_PER_POWER_DOUBLE);
        // Cap at Minecraft's maximum allowed health value
        return Math.min(health, MINECRAFT_MAX_HEALTH);
    }


    // ========================================
    // OFFENSIVE FORMULA METHODS (Player → Elite)
    // ========================================

    /**
     * Calculates the base damage a player should deal per sword hit to a normalized
     * elite mob (healthMultiplier=1.0) at the given mob level.
     * <p>
     * {@code baseDamage = calculateMobHealth(mobLevel, 0) / TARGET_HITS_TO_KILL_MOB}
     * <p>
     * This ensures that at matched combat, a standard mob always takes exactly
     * {@link #TARGET_HITS_TO_KILL_MOB} sword hits to kill, at ALL levels.
     *
     * @param mobLevel The elite mob's level
     * @return The base damage per sword hit
     */
    public static double calculateBaseDamageToElite(int mobLevel) {
        return calculateMobHealth(mobLevel, 0) / TARGET_HITS_TO_KILL_MOB;
    }

    /**
     * Calculates the offensive skill adjustment multiplier.
     * <p>
     * {@code skillAdjustment = 2^((weaponSkillLevel - mobLevel) / OFFENSIVE_SKILL_SCALING_RATE)}
     * <p>
     * When skill level matches mob level, returns 1.0 (baseline).
     * When skill level is higher, returns > 1.0 (bonus damage).
     * When skill level is lower, returns < 1.0 (reduced damage).
     *
     * @param weaponSkillLevel The player's weapon skill level
     * @param mobLevel         The elite mob's level
     * @return The skill adjustment multiplier
     */
    public static double calculateOffensiveSkillAdjustment(int weaponSkillLevel, int mobLevel) {
        return Math.pow(2.0, (weaponSkillLevel - mobLevel) / OFFENSIVE_SKILL_SCALING_RATE);
    }

    // ========================================
    // DEBUG / REFERENCE METHODS
    // ========================================

    /**
     * Prints a reference table of level modifiers for debugging.
     * <p>
     * Useful for visualizing how the current constants affect combat.
     */
    public static void printModifierTable() {
        com.magmaguy.magmacore.util.Logger.info("=== Level Scaling Reference Table ===");
        com.magmaguy.magmacore.util.Logger.info("Settings: SCALING_BASE=" + SCALING_BASE +
                ", LEVELS_PER_POWER_DOUBLE=" + LEVELS_PER_POWER_DOUBLE);
        com.magmaguy.magmacore.util.Logger.info(String.format("%-12s %-12s %-15s", "Level Diff", "Modifier", "Description"));
        com.magmaguy.magmacore.util.Logger.info("-".repeat(40));

        int[] differences = {-15, -10, -5, -3, -1, 0, 1, 3, 5, 10, 15};
        for (int diff : differences) {
            double modifier = getLevelModifierFromDifference(diff);
            String desc = diff < 0 ? "Easier" : (diff > 0 ? "Harder" : "Balanced");
            com.magmaguy.magmacore.util.Logger.info(String.format("%-12d %-12.3f %-15s", diff, modifier, desc));
        }
    }

    /**
     * Prints a reference table of mob HP at various levels.
     */
    public static void printHealthTable() {
        com.magmaguy.magmacore.util.Logger.info("=== Mob HP Scaling Reference Table ===");
        com.magmaguy.magmacore.util.Logger.info("Formula: HP = " + BASE_MOB_HP + " * 2^(level/5)");
        com.magmaguy.magmacore.util.Logger.info("+5 levels = exactly 2x HP at all levels");
        com.magmaguy.magmacore.util.Logger.info(String.format("%-8s %-12s %-15s", "Level", "HP", "+5 Level HP"));
        com.magmaguy.magmacore.util.Logger.info("-".repeat(40));

        int[] levels = {1, 5, 10, 15, 20, 25, 30, 50, 75, 100};
        for (int level : levels) {
            double hp = calculateMobHealth(level, 20);
            double hpPlus5 = calculateMobHealth(level + 5, 20);
            com.magmaguy.magmacore.util.Logger.info(String.format("%-8d %-12.1f %-15.1f (2x)", level, hp, hpPlus5));
        }
    }
}
