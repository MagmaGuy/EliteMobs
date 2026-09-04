package com.magmaguy.elitemobs.api.power;

/** Immediate disposition of a semantic action requested by an EliteMobs Mind actor. */
public enum ElitePowerActionResult {
    /** The handler performed the action or took durable ownership of it. */
    ACCEPTED,
    /** The handler took no ownership; the Mind may retry later. */
    DEFERRED,
    /** The unchanged action is not valid for this actor or attached power. */
    REJECTED
}
