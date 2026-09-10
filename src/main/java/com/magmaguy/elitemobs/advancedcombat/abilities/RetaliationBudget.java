package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;
import java.util.UUID;

/** Source-owned damage budget accumulated only from qualified taunted attackers. */
record RetaliationBudget(double amount, String abilityId, UUID lastAttackerId) {
    RetaliationBudget {
        if (!Double.isFinite(amount) || amount < 0D)
            throw new IllegalArgumentException("amount must be finite and non-negative");
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(lastAttackerId, "lastAttackerId");
    }

    static RetaliationBudget accumulate(
            RetaliationBudget current,
            double gained,
            double cap,
            String abilityId,
            UUID attackerId) {
        Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(attackerId, "attackerId");
        double boundedGain = Double.isFinite(gained) ? Math.max(0D, gained) : 0D;
        double boundedCap = Double.isFinite(cap) ? Math.max(0D, cap) : 0D;
        double existing = current != null && current.abilityId().equals(abilityId)
                ? current.amount()
                : 0D;
        return new RetaliationBudget(
                Math.min(boundedCap, existing + boundedGain), abilityId, attackerId);
    }
}
