package com.magmaguy.elitemobs.experimentalcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityRuntimeEvidenceLedgerTest {
    @Test
    void snapshotsAreCasterScopedOrderedAndDrainable() {
        AbilityRuntimeEvidenceLedger ledger = new AbilityRuntimeEvidenceLedger();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        ledger.record(observation(first, "first", 3D));
        ledger.record(observation(second, "second", 4D));
        ledger.record(observation(first, "third", 5D));

        assertEquals(java.util.List.of("first", "third"), ledger.snapshot(first).stream()
                .map(AbilityRuntimeObservation::abilityId).toList());
        assertEquals(2, ledger.drain(first).size());
        assertTrue(ledger.snapshot(first).isEmpty());
        assertEquals(1, ledger.snapshot(second).size());
    }

    @Test
    void stateEvidenceNamesOneOwnedMechanicAndItsActualTarget() {
        UUID caster = UUID.randomUUID();
        UUID ally = UUID.randomUUID();

        AbilityRuntimeObservation observation = AbilityRuntimeObservation.state(
                AbilityRuntimeObservation.Kind.STATE_ARMED,
                caster, ally, "guardian.signature", .35D, 100,
                AbilityMechanic.DAMAGE_REDIRECT);

        assertEquals(caster, observation.casterId());
        assertEquals(ally, observation.targetId());
        assertEquals(.35D, observation.amount());
        assertEquals(100, observation.durationTicks());
        assertEquals(Set.of(AbilityMechanic.DAMAGE_REDIRECT), observation.mechanics());
    }

    private static AbilityRuntimeObservation observation(UUID caster, String abilityId, double amount) {
        return new AbilityRuntimeObservation(
                AbilityRuntimeObservation.Kind.DAMAGE,
                caster, UUID.randomUUID(), abilityId, amount, 0, 1,
                Set.of(AbilityEffect.DAMAGE), Set.of());
    }
}
