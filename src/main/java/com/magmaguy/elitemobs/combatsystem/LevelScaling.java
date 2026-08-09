package com.magmaguy.elitemobs.combatsystem;

import org.bukkit.Bukkit;

/**
 * Owns the live, playtested EliteMobs combat curve: exponential elite health plus the offensive
 * and defensive skill adjustments used by the damage pipelines. Experimental level-difference
 * damage modifiers intentionally do not live here.
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

    /**
     * Fallback maximum mob health allowed.
     * <p>
     * EliteMobs writes this value to spigot.yml at startup under settings.attribute.maxHealth.max.
     * Runtime health clamping should use {@link #getMinecraftMaxHealth()} so it follows the server's active value.
     */
    public static final double DEFAULT_MINECRAFT_MAX_HEALTH = 2048D;

    public static double getMinecraftMaxHealth() {
        try {
            double configuredMaxHealth = Bukkit.getServer().spigot().getConfig().getDouble(
                    "settings.attribute.maxHealth.max",
                    DEFAULT_MINECRAFT_MAX_HEALTH);
            if (Double.isFinite(configuredMaxHealth) && configuredMaxHealth > 0D) return configuredMaxHealth;
        } catch (Exception ignored) {
            // Bukkit is not initialized in pure unit tests.
        }
        return DEFAULT_MINECRAFT_MAX_HEALTH;
    }

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
     * This rate is mirrored by the offensive adjustment for a symmetric approved curve.
     */
    public static final double SKILL_SCALING_RATE = 7.5;

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
     * Note: This is defined in sword hits (1.6 attacks/sec). Other melee weapons
     * use a tuned pacing factor in {@link WeaponOffenseCalculator#getAttackSpeedFactor}
     * instead of full theoretical DPS normalization.
     */
    public static final double TARGET_HITS_TO_KILL_MOB = 3.0;

    /**
     * Reference weapon attack speed used to define {@link #TARGET_HITS_TO_KILL_MOB}.
     * <p>
     * Sword speed is 1.6 attacks/sec. Other melee weapon speeds are compared against
     * this reference by {@link WeaponOffenseCalculator#getAttackSpeedFactor}.
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
        return Math.min(health, getMinecraftMaxHealth());
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

}
