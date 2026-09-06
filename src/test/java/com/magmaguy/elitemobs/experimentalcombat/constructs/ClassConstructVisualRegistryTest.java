package com.magmaguy.elitemobs.experimentalcombat.constructs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ClassConstructVisualRegistryTest {

    @Test
    void packetOnlyCatalogNeverContainsTheRealAbyssalPortalMaterial() {
        ClassConstructVisualRegistry registry = ClassConstructVisualRegistry.builtIns();

        assertFalse(registry.abilityIds().stream()
                .map(registry::require)
                .flatMap(plan -> plan.definition().blocks().stream())
                .map(PacketConstructDefinition.BlockVisual::serializedBlockData)
                .anyMatch(data -> data.contains("nether_portal")));
    }
}
