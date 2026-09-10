package com.magmaguy.elitemobs.experimentalcombat.damage;

import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.combatsystem.NaturalEliteCombatTweak;
import com.magmaguy.elitemobs.combatsystem.WeaponOffenseCalculator;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityLevelScaling;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Material;

/**
 * Canonical level-aware damage curve for [Alpha] Advanced Combat System attacks that supply their own
 * server-authoritative damage. Ability tuning and magic-weapon tuning are expressed as multiples
 * of one matched-level EliteMobs basic hit, never as multiples of vanilla attack attributes.
 */
public final class ExperimentalDamageScaling {
    private ExperimentalDamageScaling() {
    }

    public static double classAbility(
            EliteEntity target,
            int effectiveClassLevel,
            double abilityMultiplier) {
        double baseline = classAbilityUnrankedBaseline(target, effectiveClassLevel);
        return scaleClassAbilityBaseline(baseline, effectiveClassLevel, abilityMultiplier);
    }

    /**
     * Returns the matched combat baseline for a class ability before both authored ability tuning
     * and class-rank scaling are applied. Runtime probes use this seam to measure the rank curve
     * independently instead of deriving their expectation from the production implementation.
     */
    public static double classAbilityUnrankedBaseline(
            EliteEntity target,
            int effectiveClassLevel) {
        return calculate(target, effectiveClassLevel, 1D, 1D, null);
    }

    public static double magicWeapon(
            EliteEntity target,
            int skillLevel,
            double itemLevel,
            double attackMultiplier,
            Material carrierMaterial) {
        if (target == null || !target.isValid()) return 0D;
        int formulaLevel = formulaLevel(target, skillLevel);
        double itemAdjustment = WeaponOffenseCalculator.getWeaponAdjustment(itemLevel, formulaLevel);
        return calculate(target, skillLevel, itemAdjustment, attackMultiplier, carrierMaterial);
    }

    private static double calculate(
            EliteEntity target,
            int progressionLevel,
            double equipmentAdjustment,
            double outputMultiplier,
            Material carrierMaterial) {
        if (target == null || !target.isValid()
                || !Double.isFinite(equipmentAdjustment) || equipmentAdjustment < 0D
                || !Double.isFinite(outputMultiplier) || outputMultiplier <= 0D)
            return 0D;

        int boundedProgressionLevel = Math.max(1, progressionLevel);
        int formulaLevel = formulaLevel(target, boundedProgressionLevel);
        double baseDamage = NaturalEliteCombatTweak.getTweakedBaseDamageToElite(target, formulaLevel);
        double skillAdjustment = LevelScaling.calculateOffensiveSkillAdjustment(
                boundedProgressionLevel, formulaLevel);
        double scaledCombatAdjustment = scaledCombatAdjustment(target, formulaLevel);
        double bossModifier = target instanceof CustomBossEntity customBoss
                ? customBoss.getDamageModifier(carrierMaterial)
                : 1D;
        double combatMultiplier = combatMultiplier(target);
        return combine(
                baseDamage,
                skillAdjustment,
                equipmentAdjustment,
                outputMultiplier,
                scaledCombatAdjustment,
                bossModifier,
                combatMultiplier);
    }

    private static int formulaLevel(EliteEntity target, int progressionLevel) {
        return target.isScaledCombat()
                ? Math.max(1, progressionLevel)
                : Math.max(1, target.getLevel());
    }

    private static double scaledCombatAdjustment(EliteEntity target, int formulaLevel) {
        if (!target.isScaledCombat()) return 1D;
        double simulatedHealth = NaturalEliteCombatTweak.getTweakedMobHealthForLevel(
                target, formulaLevel, target.getHealthMultiplier());
        return simulatedHealth > 0D ? target.getMaxHealth() / simulatedHealth : 0D;
    }

    private static double combatMultiplier(EliteEntity target) {
        if (target.isScaledCombat())
            return MobCombatSettingsConfig.getScaledDamageToEliteMultiplier();
        if (target instanceof CustomBossEntity customBoss && customBoss.isNormalizedCombat())
            return MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier();
        return MobCombatSettingsConfig.getDamageToEliteMultiplier();
    }

    static double classRankScale(int effectiveClassLevel) {
        return AbilityLevelScaling.multiplier(effectiveClassLevel);
    }

    static double scaleClassAbilityBaseline(
            double unrankedBaseline,
            int effectiveClassLevel,
            double abilityMultiplier) {
        if (!Double.isFinite(unrankedBaseline) || unrankedBaseline <= 0D
                || !Double.isFinite(abilityMultiplier) || abilityMultiplier <= 0D) return 0D;
        double damage = unrankedBaseline * abilityMultiplier * classRankScale(effectiveClassLevel);
        return Double.isFinite(damage) && damage > 0D ? damage : 0D;
    }

    /** Pure multiplication seam retained package-private for ratio and invalid-input tests. */
    static double combine(
            double baseDamage,
            double skillAdjustment,
            double equipmentAdjustment,
            double outputMultiplier,
            double scaledCombatAdjustment,
            double bossModifier,
            double combatMultiplier) {
        double[] factors = {
                baseDamage,
                skillAdjustment,
                equipmentAdjustment,
                outputMultiplier,
                scaledCombatAdjustment,
                bossModifier,
                combatMultiplier
        };
        for (double factor : factors)
            if (!Double.isFinite(factor) || factor < 0D) return 0D;
        double damage = 1D;
        for (double factor : factors) damage *= factor;
        return Double.isFinite(damage) && damage > 0D ? damage : 0D;
    }
}
