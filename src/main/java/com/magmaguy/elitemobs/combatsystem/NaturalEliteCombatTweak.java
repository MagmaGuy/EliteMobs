package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;

/**
 * Optional compatibility tweak for natural elite combat numbers.
 * <p>
 * This does not replace the recommended EliteMobs 10 combat formulas. Instead,
 * it side-loads lower natural-elite HP and outgoing-to-elite damage numbers when
 * {@code useRecommendedHealthScaling} is disabled for servers that need values
 * closer to legacy EliteMobs 9 integrations.
 * <p>
 * Legacy-compatible values are intentionally simple:
 * <pre>
 * baseDamage = level * 2
 * mobHealth  = baseDamage * TARGET_HITS_TO_KILL_MOB
 * </pre>
 * This keeps the matched hits-to-kill target aligned with the existing combat
 * formula while producing much smaller displayed HP and damage values.
 */
public class NaturalEliteCombatTweak {

    public static final double LEGACY_DAMAGE_PER_LEVEL = 2.0;

    private NaturalEliteCombatTweak() {
    }

    public static boolean shouldApply(EliteEntity eliteEntity) {
        return eliteEntity != null
                && eliteEntity.isNaturalEntity()
                && !MobCombatSettingsConfig.isUseRecommendedHealthScaling();
    }

    public static double getTweakedMobHealth(EliteEntity eliteEntity, int level, double recommendedHealth) {
        if (!shouldApply(eliteEntity)) return recommendedHealth;
        return getLegacyMobHealth(level);
    }

    public static double getTweakedBaseDamageToElite(EliteEntity eliteEntity, int level) {
        if (!shouldApply(eliteEntity)) return LevelScaling.calculateBaseDamageToElite(level);
        return getLegacyBaseDamage(level);
    }

    public static double getTweakedMobHealthForLevel(EliteEntity eliteEntity, int level, double healthMultiplier) {
        double baseHealth = shouldApply(eliteEntity)
                ? getLegacyMobHealth(level)
                : LevelScaling.calculateMobHealth(level, 0);
        return baseHealth * healthMultiplier;
    }

    private static double getLegacyBaseDamage(int level) {
        return Math.max(1, level) * LEGACY_DAMAGE_PER_LEVEL;
    }

    private static double getLegacyMobHealth(int level) {
        return getLegacyBaseDamage(level) * LevelScaling.TARGET_HITS_TO_KILL_MOB;
    }
}
