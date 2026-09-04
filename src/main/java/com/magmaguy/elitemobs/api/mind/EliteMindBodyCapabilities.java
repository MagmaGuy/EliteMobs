package com.magmaguy.elitemobs.api.mind;

import java.util.Objects;
import java.util.Set;

/** Classloader-safe view of physical body support provided by the active native adapter. */
public record EliteMindBodyCapabilities(
        Set<EliteMindBodyLocomotion> supportedLocomotions,
        Set<EliteMindBodyLocomotion> pathfindingLocomotions,
        boolean uniformScale,
        double minimumUniformScale,
        double maximumUniformScale,
        boolean independentDimensions,
        boolean entityCollisionToggle,
        boolean blockCollisionToggle) {

    public EliteMindBodyCapabilities {
        supportedLocomotions = Set.copyOf(Objects.requireNonNull(
                supportedLocomotions, "supportedLocomotions"));
        pathfindingLocomotions = Set.copyOf(Objects.requireNonNull(
                pathfindingLocomotions, "pathfindingLocomotions"));
        if (supportedLocomotions.isEmpty()) {
            throw new IllegalArgumentException("At least one locomotion profile must be supported");
        }
        if (!supportedLocomotions.containsAll(pathfindingLocomotions)) {
            throw new IllegalArgumentException("Pathfinding locomotion must be supported locomotion");
        }
        if (!Double.isFinite(minimumUniformScale)
                || !Double.isFinite(maximumUniformScale)
                || minimumUniformScale <= 0.0D
                || maximumUniformScale < minimumUniformScale) {
            throw new IllegalArgumentException("Uniform scale bounds are invalid");
        }
        if (!uniformScale
                && (Double.compare(minimumUniformScale, 1.0D) != 0
                || Double.compare(maximumUniformScale, 1.0D) != 0)) {
            throw new IllegalArgumentException("Unsupported uniform scaling must use fixed 1.0 bounds");
        }
    }

    /** Validates a request before EliteMobs creates a physical body. */
    public void validate(EliteMindBodyProfile profile) {
        Objects.requireNonNull(profile, "profile");
        if (!supportedLocomotions.contains(profile.locomotion())) {
            throw new IllegalArgumentException("Unsupported Mind body locomotion: " + profile.locomotion());
        }
        if ((!uniformScale && Double.compare(profile.uniformScale(), 1.0D) != 0)
                || profile.uniformScale() < minimumUniformScale
                || profile.uniformScale() > maximumUniformScale) {
            throw new IllegalArgumentException(
                    "Mind body scale " + profile.uniformScale() + " is outside supported bounds "
                            + minimumUniformScale + ".." + maximumUniformScale);
        }
        if (!entityCollisionToggle && !profile.entityCollidable()) {
            throw new IllegalArgumentException("The active native adapter cannot disable entity collision");
        }
    }

    public boolean canPathfind(EliteMindBodyLocomotion locomotion) {
        return pathfindingLocomotions.contains(Objects.requireNonNull(locomotion, "locomotion"));
    }
}
