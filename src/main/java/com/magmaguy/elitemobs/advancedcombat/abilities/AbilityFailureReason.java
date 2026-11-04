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
    /** The ability only works while the caster is moving. */
    NOT_MOVING,
    /** The ability needs an ally, or the caster, at low health. */
    NO_LOW_HEALTH_ALLY,
    /** The ability only targets wounded foes and none were in reach. */
    NO_WOUNDED_TARGET
}
