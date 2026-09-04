package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityControlEvidenceTest {
    private final FixedAbilityRegistry registry = BuiltInClassContent.abilityRegistry();

    @Test
    void configuredButUnappliedControlsCanNeverAppearInEvidence() {
        FixedAbilitySpec warmonger = registry.require("warmonger.utility");
        UUID caster = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        List<AbilityRuntimeObservation> observations = AbilityControlEvidence.applied(
                caster, target, warmonger,
                Set.of(AbilityEffect.KNOCKBACK), 1.2D, 80);

        assertEquals(1, observations.size());
        AbilityRuntimeObservation knockback = observations.getFirst();
        assertEquals(AbilityRuntimeObservation.Kind.CONTROL, knockback.kind());
        assertEquals(Set.of(AbilityEffect.KNOCKBACK), knockback.effects());
        assertFalse(knockback.effects().contains(AbilityEffect.FEAR));
        assertThrows(IllegalArgumentException.class, () -> AbilityControlEvidence.applied(
                caster, target, warmonger, Set.of(AbilityEffect.ROOT), 1D, 40));
    }

    @Test
    void tauntExtensionEvidenceExistsOnlyForAnActuallyExtendedOwnedLease() {
        UUID existing = UUID.randomUUID();
        UUID fresh = UUID.randomUUID();
        UUID expired = UUID.randomUUID();
        Map<UUID, Long> leases = new java.util.LinkedHashMap<>();
        leases.put(existing, 120L);
        leases.put(expired, 90L);

        Set<UUID> extended = TimedStatusLeasePolicy.apply(
                leases, List.of(existing, fresh, expired), 100L, 40L, true);

        assertEquals(Set.of(existing), extended);
        assertEquals(160L, leases.get(existing));
        assertEquals(140L, leases.get(fresh));
        assertEquals(140L, leases.get(expired));

        FixedAbilitySpec champion = registry.require("champion.signature");
        AbilityRuntimeObservation evidence = AbilityControlEvidence.mechanicTriggered(
                UUID.randomUUID(), existing, champion,
                AbilityMechanic.EXTEND_TAUNT, AbilityEffect.TAUNT, 25D, 60);
        assertEquals(AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED, evidence.kind());
        assertEquals(Set.of(AbilityMechanic.EXTEND_TAUNT), evidence.mechanics());
        assertEquals(Set.of(AbilityEffect.TAUNT), evidence.effects());
    }

    @Test
    void replacingRatherThanExtendingAStatusNeverClaimsExtension() {
        Map<String, Long> leases = new java.util.LinkedHashMap<>();
        leases.put("target", 120L);

        assertTrue(TimedStatusLeasePolicy.apply(
                leases, List.of("target"), 100L, 40L, false).isEmpty());
        assertEquals(140L, leases.get("target"));
    }
}
