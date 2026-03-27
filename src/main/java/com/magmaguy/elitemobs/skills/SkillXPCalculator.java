package com.magmaguy.elitemobs.skills;

/**
 * Utility class for calculating skill XP values.
 * <p>
 * XP Progression System:
 * - Quadratic mob XP rewards higher-level mobs proportionally
 * - Cubic level requirements create linear kills-per-level scaling
 * - Boss multipliers reward players for fighting tougher enemies
 * - Soft cap beyond level 100 makes progression exponentially harder
 */
public class SkillXPCalculator {

    private SkillXPCalculator() {
        // Static utility class
    }

    /**
     * Calculates the XP earned from killing a mob of a given level.
     * Formula: mob_xp(mob_level) = mob_level^2
     * <p>
     * Examples:
     * - Level 1 mob: 1 XP
     * - Level 50 mob: 2,500 XP
     * - Level 100 mob: 10,000 XP
     *
     * @param mobLevel The level of the killed mob (must be positive)
     * @return The XP earned, or 0 if mobLevel is <= 0
     */
    public static long calculateMobXP(int mobLevel) {
        if (mobLevel <= 0) return 0;
        return (long) mobLevel * mobLevel;
    }

    /**
     * Calculates the XP required to advance from a given level to the next level.
     * <p>
     * For levels 1-100: xp_to_next_level(L) = 8.5 * L^3
     * For levels > 100: xp_to_next_level(L) = 8.5 * L^3 * (1 + ((L - 100)^2 / 100))
     * <p>
     * The soft cap formula ensures:
     * - Continuous at level 100 (multiplier = 1.0)
     * - Quadratic growth beyond 100, making progress increasingly difficult
     * - At level 150, multiplier is 26x on top of the base, making progress nearly impossible
     *
     * @param currentLevel The player's current level (must be >= 1)
     * @return The XP required to reach the next level
     */
    public static long xpToNextLevel(int currentLevel) {
        if (currentLevel < 1) currentLevel = 1;

        long baseXP = (long) (8.5 * (long) currentLevel * currentLevel * currentLevel);

        if (currentLevel <= 100) {
            return baseXP;
        }

        // Soft cap multiplier for levels beyond 100
        double softCapMultiplier = 1.0 + (Math.pow(currentLevel - 100, 2) / 100.0);
        return (long) (baseXP * softCapMultiplier);
    }

    /**
     * Calculates the total cumulative XP required to reach a target level from level 1.
     * This is the sum of all xpToNextLevel values from level 1 to (targetLevel - 1).
     *
     * @param targetLevel The level to reach (must be >= 1)
     * @return The total XP required to reach that level from level 1
     */
    public static long totalXPForLevel(int targetLevel) {
        if (targetLevel <= 1) return 0;

        long total = 0;
        for (int level = 1; level < targetLevel; level++) {
            total += xpToNextLevel(level);
        }
        return total;
    }

    /**
     * Determines the current level based on total accumulated XP.
     * This is the inverse of totalXPForLevel.
     *
     * @param totalXP The total XP accumulated
     * @return The current level (minimum 1)
     */
    public static int levelFromTotalXP(long totalXP) {
        if (totalXP <= 0) return 1;

        int level = 1;
        long accumulatedXP = 0;

        while (accumulatedXP + xpToNextLevel(level) <= totalXP) {
            accumulatedXP += xpToNextLevel(level);
            level++;
        }

        return level;
    }

    /**
     * Calculates the XP progress within the current level.
     * This is the amount of XP earned toward the next level.
     *
     * @param totalXP The total XP accumulated
     * @return The XP progress within the current level
     */
    public static long xpProgressInCurrentLevel(long totalXP) {
        if (totalXP <= 0) return 0;

        int currentLevel = levelFromTotalXP(totalXP);
        long xpForCurrentLevel = totalXPForLevel(currentLevel);
        return totalXP - xpForCurrentLevel;
    }

    /**
     * Calculates the percentage progress toward the next level (0.0 to 1.0).
     *
     * @param totalXP The total XP accumulated
     * @return The progress as a decimal (0.0 = just leveled, 1.0 = about to level)
     */
    public static double levelProgress(long totalXP) {
        int currentLevel = levelFromTotalXP(totalXP);
        long progressXP = xpProgressInCurrentLevel(totalXP);
        long requiredXP = xpToNextLevel(currentLevel);

        if (requiredXP == 0) return 0;
        return (double) progressXP / requiredXP;
    }

    /**
     * Calculates how many kills of a specific mob level are needed to reach the next level.
     *
     * @param currentLevel The player's current skill level
     * @param mobLevel     The level of mobs being killed
     * @return The number of kills needed (approximate, as it's integer division)
     */
    public static long killsToNextLevel(int currentLevel, int mobLevel) {
        long xpNeeded = xpToNextLevel(currentLevel);
        long xpPerKill = calculateMobXP(mobLevel);

        if (xpPerKill == 0) return Long.MAX_VALUE;
        return (xpNeeded + xpPerKill - 1) / xpPerKill; // Ceiling division
    }

    /**
     * Applies the armor XP multiplier (1/3 rate).
     * Armor levels more slowly than weapons to balance progression.
     *
     * @param baseXP The base XP from killing a mob
     * @return The adjusted XP for armor skill
     */
    public static long calculateArmorXP(long baseXP) {
        return baseXP / 3;
    }

    /**
     * Applies a skill type's XP multiplier to the base mob XP.
     *
     * @param skillType The skill type
     * @param baseXP    The base XP from killing a mob
     * @return The adjusted XP for that skill
     */
    public static long applySkillMultiplier(SkillType skillType, long baseXP) {
        return (long) (baseXP * skillType.getXpMultiplier());
    }

    /**
     * Calculates the XP multiplier for a boss based on its health and damage multipliers.
     * <p>
     * HP component uses a slightly super-linear exponent (1.15) to reward the compounding
     * difficulty of longer fights. Damage component is dampened since even small increases
     * in damage significantly increase difficulty.
     * <p>
     * Formula: hpMultiplier^1.15 * (1 + (dmgMultiplier - 1) * 0.5)
     *
     * @param healthMultiplier The mob's health multiplier (e.g. 1.0 for normal, 10.0 for boss)
     * @param damageMultiplier The mob's damage multiplier (e.g. 0.8 for trash, 1.5 for boss)
     * @return The XP multiplier to apply (minimum 0.01 to avoid zero XP)
     */
    public static double calculateBossXPMultiplier(double healthMultiplier, double damageMultiplier) {
        double hpComponent = Math.pow(Math.max(healthMultiplier, 0.01), 1.15);
        double dmgComponent = 1.0 + (damageMultiplier - 1.0) * 0.5;
        if (dmgComponent < 0.1) dmgComponent = 0.1;
        return hpComponent * dmgComponent;
    }
}
