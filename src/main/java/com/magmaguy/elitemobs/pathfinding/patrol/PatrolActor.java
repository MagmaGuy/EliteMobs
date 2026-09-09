package com.magmaguy.elitemobs.pathfinding.patrol;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

interface PatrolActor {
    UUID runtimeId();

    String canonicalIdentity();

    String displayName();

    PatrolOrigin origin();

    PatrolRoute route();

    LivingEntity body();

    boolean persistsWhileDetached();

    boolean isInCombat();

    /** A transient proximity hold, separate from combat and explicit script/admin pauses. */
    default boolean isNearPlayer(long tick) { return false; }

    /** Refreshes paused gaze, or releases it when moving/detached/shut down. Does not own movement. */
    default void tickFacing(boolean paused) { }

    /** Applies actor-specific setup before a native pathfinding handle is installed. */
    boolean prepareBody();

    String ineligibleReason();

    double currentMovementSpeed();

    void updatePersistentLocation(Location safeLocation);

    void materialize();

    void syncVisuals();

    Object owner();
}
