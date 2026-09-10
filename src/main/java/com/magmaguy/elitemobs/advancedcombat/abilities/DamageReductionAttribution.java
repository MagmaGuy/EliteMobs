package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;
import java.util.UUID;

/**
 * Carries percentage-reduction attribution from the damage-modifier phase to the authoritative
 * end of the Elite-to-player event.
 *
 * <p>The reduction is applied before the player's later defensive listeners. Those listeners may
 * reduce or amplify the already-weakened hit, so credit must undergo the same downstream ratio.
 * A cancelled or fully erased hit produces no mitigation credit.</p>
 */
record DamageReductionAttribution(
        UUID sourceId,
        UUID targetId,
        String abilityId,
        double damageAfterReduction,
        double preventedBeforeDownstream) {

    DamageReductionAttribution {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(abilityId, "abilityId");
        if (!Double.isFinite(damageAfterReduction) || damageAfterReduction < 0D)
            throw new IllegalArgumentException("damageAfterReduction must be finite and non-negative");
        if (!Double.isFinite(preventedBeforeDownstream) || preventedBeforeDownstream < 0D)
            throw new IllegalArgumentException("preventedBeforeDownstream must be finite and non-negative");
    }

    double survivingPreventedDamage(double finalDamage, boolean cancelled) {
        if (cancelled || !Double.isFinite(finalDamage) || finalDamage <= 0D
                || damageAfterReduction <= 0D || preventedBeforeDownstream <= 0D)
            return 0D;
        double downstreamRatio = finalDamage / damageAfterReduction;
        double surviving = preventedBeforeDownstream * downstreamRatio;
        return Double.isFinite(surviving) && surviving > 0D ? surviving : 0D;
    }
}
