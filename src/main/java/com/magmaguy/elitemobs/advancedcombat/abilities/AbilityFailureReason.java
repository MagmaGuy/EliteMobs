package com.magmaguy.elitemobs.advancedcombat.abilities;

public enum AbilityFailureReason {
    NONE,
    WRONG_THREAD,
    INVALID_PLAYER,
    INVALID_LEVEL,
    ABILITY_NOT_REGISTERED,
    NO_VALID_TARGET,
    NO_CORPSE,
    PATH_BLOCKED,
    UNSAFE_DESTINATION,
    ENGINE_CLOSED,
    /** The ability only works while the caster is sprinting. */
    NOT_MOVING,
    /** The ability needs an ally, or the caster, at low health. */
    NO_LOW_HEALTH_ALLY,
    /** The ability only targets wounded foes and none were in reach. */
    NO_WOUNDED_TARGET,
    /** The ability needs an Elite in reach; Advanced Combat skills never target vanilla mobs. */
    NO_ELITE_TARGET,
    /** The ability only targets large foes and bosses. */
    NO_LARGE_TARGET,
    /** The ability only targets bosses. */
    NO_BOSS_TARGET
}
