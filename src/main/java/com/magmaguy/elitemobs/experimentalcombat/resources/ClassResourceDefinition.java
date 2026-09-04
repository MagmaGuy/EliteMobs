package com.magmaguy.elitemobs.experimentalcombat.resources;

import com.magmaguy.elitemobs.experimentalcombat.classes.ClassResourceType;

import java.util.Objects;

/** Complete immutable economy for one root class resource. */
public record ClassResourceDefinition(
        ClassResourceType type,
        double maximum,
        double initialAmount,
        double inCombatTickDelta,
        double outOfCombatTickDelta,
        double damageDealtHealthEquivalentGain,
        double damageReceivedHealthEquivalentGain,
        double damageReceivedFlatChange,
        long recoveryDelayAfterDamageTicks,
        double healingHealthEquivalentGain,
        double preventedDamageHealthEquivalentGain,
        double tauntGainPerEnemy,
        double tauntGainCap) {

    public ClassResourceDefinition {
        type = Objects.requireNonNull(type, "type");
        requireFinitePositive(maximum, "maximum");
        requireFiniteNonNegative(initialAmount, "initialAmount");
        if (initialAmount > maximum)
            throw new IllegalArgumentException("initialAmount must not exceed maximum");
        requireFinite(inCombatTickDelta, "inCombatTickDelta");
        requireFinite(outOfCombatTickDelta, "outOfCombatTickDelta");
        requireFiniteNonNegative(damageDealtHealthEquivalentGain, "damageDealtHealthEquivalentGain");
        requireFiniteNonNegative(damageReceivedHealthEquivalentGain, "damageReceivedHealthEquivalentGain");
        requireFinite(damageReceivedFlatChange, "damageReceivedFlatChange");
        if (recoveryDelayAfterDamageTicks < 0L)
            throw new IllegalArgumentException("recoveryDelayAfterDamageTicks must not be negative");
        requireFiniteNonNegative(healingHealthEquivalentGain, "healingHealthEquivalentGain");
        requireFiniteNonNegative(preventedDamageHealthEquivalentGain, "preventedDamageHealthEquivalentGain");
        requireFiniteNonNegative(tauntGainPerEnemy, "tauntGainPerEnemy");
        requireFiniteNonNegative(tauntGainCap, "tauntGainCap");
    }

    private static void requireFinitePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0D)
            throw new IllegalArgumentException(name + " must be finite and positive");
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0D)
            throw new IllegalArgumentException(name + " must be finite and non-negative");
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
    }
}
