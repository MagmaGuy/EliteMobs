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
        double damageDealtFlatGain,
        double damageReceivedHealthEquivalentGain,
        double damageReceivedFlatChange,
        long recoveryDelayAfterDamageTicks,
        double healingHealthEquivalentGain,
        double preventedDamageHealthEquivalentGain,
        double tauntGainPerEnemy,
        double tauntGainCap,
        NearbyRecoveryBonus nearbyRecoveryBonus,
        DamageFreeRecoveryBonus damageFreeRecoveryBonus) {

    public ClassResourceDefinition {
        type = Objects.requireNonNull(type, "type");
        requireFinitePositive(maximum, "maximum");
        requireFiniteNonNegative(initialAmount, "initialAmount");
        if (initialAmount > maximum)
            throw new IllegalArgumentException("initialAmount must not exceed maximum");
        requireFinite(inCombatTickDelta, "inCombatTickDelta");
        requireFinite(outOfCombatTickDelta, "outOfCombatTickDelta");
        requireFiniteNonNegative(damageDealtFlatGain, "damageDealtFlatGain");
        requireFiniteNonNegative(damageReceivedHealthEquivalentGain, "damageReceivedHealthEquivalentGain");
        requireFinite(damageReceivedFlatChange, "damageReceivedFlatChange");
        if (recoveryDelayAfterDamageTicks < 0L)
            throw new IllegalArgumentException("recoveryDelayAfterDamageTicks must not be negative");
        requireFiniteNonNegative(healingHealthEquivalentGain, "healingHealthEquivalentGain");
        requireFiniteNonNegative(preventedDamageHealthEquivalentGain, "preventedDamageHealthEquivalentGain");
        requireFiniteNonNegative(tauntGainPerEnemy, "tauntGainPerEnemy");
        requireFiniteNonNegative(tauntGainCap, "tauntGainCap");
        nearbyRecoveryBonus = Objects.requireNonNull(nearbyRecoveryBonus, "nearbyRecoveryBonus");
        damageFreeRecoveryBonus = Objects.requireNonNull(damageFreeRecoveryBonus, "damageFreeRecoveryBonus");
    }

    public record DamageFreeRecoveryBonus(long requiredTicks, double multiplier) {
        public static final DamageFreeRecoveryBonus NONE = new DamageFreeRecoveryBonus(0L, 1D);

        public DamageFreeRecoveryBonus {
            if (requiredTicks < 0L) throw new IllegalArgumentException("requiredTicks must not be negative");
            requireFinitePositive(multiplier, "multiplier");
        }

        public double multiplierAfter(long ticksWithoutDamage) {
            return ticksWithoutDamage > requiredTicks ? multiplier : 1D;
        }
    }

    /** Additive bonuses to passive recovery, with a spherical range and bounded entity count. */
    public record NearbyRecoveryBonus(Target target, double radius, double bonusPerEntity, int maximumEntities) {
        public enum Target { ELITES, OTHER_PLAYERS }

        public static final NearbyRecoveryBonus NONE = new NearbyRecoveryBonus(Target.ELITES, 0D, 0D, 0);

        public NearbyRecoveryBonus {
            target = Objects.requireNonNull(target, "target");
            requireFiniteNonNegative(radius, "radius");
            requireFiniteNonNegative(bonusPerEntity, "bonusPerEntity");
            if (maximumEntities < 0)
                throw new IllegalArgumentException("maximumEntities must not be negative");
            if (maximumEntities > 0) {
                requireFinitePositive(radius, "radius");
                requireFinitePositive(bonusPerEntity, "bonusPerEntity");
            }
        }

        public double multiplier(int nearbyEntities) {
            return 1D + Math.min(maximumEntities, Math.max(0, nearbyEntities)) * bonusPerEntity;
        }
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
