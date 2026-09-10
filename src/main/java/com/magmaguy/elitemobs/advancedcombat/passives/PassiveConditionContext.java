package com.magmaguy.elitemobs.advancedcombat.passives;

/** Immutable facts available while evaluating a passive trait. */
public record PassiveConditionContext(
        double playerHealthFraction,
        boolean moving,
        boolean recentlyHit,
        boolean grouped,
        boolean targetPresent,
        double targetHealthFraction,
        boolean targetBoss,
        boolean targetControlled,
        boolean targetIsolated,
        boolean targetGrouped,
        double targetDistance,
        boolean criticalHit,
        boolean rangedAttack,
        boolean classAbilityDamage,
        boolean nonSummonClassAbilityDamage,
        boolean areaClassAbilityDamage,
        boolean trapClassAbilityDamage,
        boolean blastClassAbilityDamage,
        boolean magicWeaponDamage,
        boolean recentEliteKill,
        boolean wardBroken,
        boolean drawingRangedWeapon) {

    public PassiveConditionContext {
        playerHealthFraction = fraction(playerHealthFraction);
        targetHealthFraction = fraction(targetHealthFraction);
        if (!Double.isFinite(targetDistance) || targetDistance < 0D)
            targetDistance = Double.POSITIVE_INFINITY;
    }

    public PassiveConditionContext(
            double playerHealthFraction,
            boolean moving,
            boolean recentlyHit,
            boolean grouped,
            boolean targetPresent,
            double targetHealthFraction,
            boolean targetBoss,
            boolean targetControlled,
            boolean targetIsolated,
            boolean targetGrouped,
            double targetDistance,
            boolean criticalHit,
            boolean rangedAttack,
            boolean classAbilityDamage) {
        this(playerHealthFraction, moving, recentlyHit, grouped, targetPresent,
                targetHealthFraction, targetBoss, targetControlled, targetIsolated,
                targetGrouped, targetDistance, criticalHit, rangedAttack, classAbilityDamage,
                classAbilityDamage, false, false, false,
                false, false, false, false);
    }

    /** Compatibility constructor for callers predating typed class-ability domains. */
    public PassiveConditionContext(
            double playerHealthFraction,
            boolean moving,
            boolean recentlyHit,
            boolean grouped,
            boolean targetPresent,
            double targetHealthFraction,
            boolean targetBoss,
            boolean targetControlled,
            boolean targetIsolated,
            boolean targetGrouped,
            double targetDistance,
            boolean criticalHit,
            boolean rangedAttack,
            boolean classAbilityDamage,
            boolean magicWeaponDamage,
            boolean recentEliteKill,
            boolean wardBroken) {
        this(playerHealthFraction, moving, recentlyHit, grouped, targetPresent,
                targetHealthFraction, targetBoss, targetControlled, targetIsolated,
                targetGrouped, targetDistance, criticalHit, rangedAttack, classAbilityDamage,
                classAbilityDamage, false, false, false,
                magicWeaponDamage, recentEliteKill, wardBroken, false);
    }

    public static PassiveConditionContext playerOnly(
            double playerHealthFraction,
            boolean moving,
            boolean recentlyHit,
            boolean grouped) {
        return playerOnly(playerHealthFraction, moving, recentlyHit, grouped, false, false);
    }

    public static PassiveConditionContext playerOnly(
            double playerHealthFraction,
            boolean moving,
            boolean recentlyHit,
            boolean grouped,
            boolean recentEliteKill,
            boolean wardBroken) {
        return playerOnly(playerHealthFraction, moving, recentlyHit, grouped,
                recentEliteKill, wardBroken, false);
    }

    public static PassiveConditionContext playerOnly(
            double playerHealthFraction,
            boolean moving,
            boolean recentlyHit,
            boolean grouped,
            boolean recentEliteKill,
            boolean wardBroken,
            boolean drawingRangedWeapon) {
        return new PassiveConditionContext(
                playerHealthFraction, moving, recentlyHit, grouped,
                false, 1D, false, false, false, false,
                Double.POSITIVE_INFINITY, false, false, false,
                false, false, false, false,
                false, recentEliteKill, wardBroken, drawingRangedWeapon);
    }

    private static double fraction(double value) {
        if (!Double.isFinite(value)) return 1D;
        return Math.max(0D, Math.min(1D, value));
    }
}
