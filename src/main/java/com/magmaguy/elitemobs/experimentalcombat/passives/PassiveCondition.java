package com.magmaguy.elitemobs.experimentalcombat.passives;

/** Code-owned condition vocabulary shared by every passive form. */
public enum PassiveCondition {
    ALWAYS,
    HEALTH_BELOW_75,
    HEALTH_BELOW_50,
    HEALTH_BELOW_25,
    MOVING,
    STANDING,
    RECENTLY_HIT,
    NOT_RECENTLY_HIT,
    GROUPED,
    SOLO,
    TARGET_WOUNDED,
    TARGET_HEALTHY,
    TARGET_BOSS,
    TARGET_ORDINARY,
    TARGET_CONTROLLED,
    TARGET_UNCONTROLLED,
    TARGET_ISOLATED,
    TARGET_GROUPED,
    CLOSE_RANGE,
    LONG_RANGE,
    CRITICAL_HIT,
    RANGED_ATTACK,
    CLASS_ABILITY_DAMAGE,
    AREA_CLASS_ABILITY_DAMAGE,
    TRAP_CLASS_ABILITY_DAMAGE,
    BLAST_CLASS_ABILITY_DAMAGE,
    SPELL_DAMAGE,
    RECENT_ELITE_KILL,
    WARD_BROKEN;

    public boolean matches(PassiveConditionContext context) {
        return switch (this) {
            case ALWAYS -> true;
            case HEALTH_BELOW_75 -> context.playerHealthFraction() < .75D;
            case HEALTH_BELOW_50 -> context.playerHealthFraction() < .50D;
            case HEALTH_BELOW_25 -> context.playerHealthFraction() < .25D;
            case MOVING -> context.moving();
            case STANDING -> !context.moving();
            case RECENTLY_HIT -> context.recentlyHit();
            case NOT_RECENTLY_HIT -> !context.recentlyHit();
            case GROUPED -> context.grouped();
            case SOLO -> !context.grouped();
            case TARGET_WOUNDED -> context.targetPresent() && context.targetHealthFraction() < .50D;
            case TARGET_HEALTHY -> context.targetPresent() && context.targetHealthFraction() >= .50D;
            case TARGET_BOSS -> context.targetPresent() && context.targetBoss();
            case TARGET_ORDINARY -> context.targetPresent() && !context.targetBoss();
            case TARGET_CONTROLLED -> context.targetPresent() && context.targetControlled();
            case TARGET_UNCONTROLLED -> context.targetPresent() && !context.targetControlled();
            case TARGET_ISOLATED -> context.targetPresent() && context.targetIsolated();
            case TARGET_GROUPED -> context.targetPresent() && context.targetGrouped();
            case CLOSE_RANGE -> context.targetPresent() && context.targetDistance() <= 6D;
            case LONG_RANGE -> context.targetPresent() && context.targetDistance() >= 18D;
            case CRITICAL_HIT -> context.criticalHit();
            case RANGED_ATTACK -> context.rangedAttack();
            case CLASS_ABILITY_DAMAGE -> context.classAbilityDamage();
            case AREA_CLASS_ABILITY_DAMAGE -> context.areaClassAbilityDamage();
            case TRAP_CLASS_ABILITY_DAMAGE -> context.trapClassAbilityDamage();
            case BLAST_CLASS_ABILITY_DAMAGE -> context.blastClassAbilityDamage();
            case SPELL_DAMAGE -> context.nonSummonClassAbilityDamage() || context.magicWeaponDamage();
            case RECENT_ELITE_KILL -> context.recentEliteKill();
            case WARD_BROKEN -> context.wardBroken();
        };
    }
}
