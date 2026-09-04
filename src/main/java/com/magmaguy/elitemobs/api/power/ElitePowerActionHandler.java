package com.magmaguy.elitemobs.api.power;

/** Owner-supplied handler for one semantic action admitted by an attached Lua power. */
@FunctionalInterface
public interface ElitePowerActionHandler {
    ElitePowerActionResult handle(ElitePowerActionRequest request);
}
