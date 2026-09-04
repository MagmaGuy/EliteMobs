package com.magmaguy.elitemobs.experimentalcombat.constructs;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassConstructVisualRegistryTest {

    @Test
    void coversEveryAbilityWhoseFantasyPromisesAPlacedOrCarriedConstruct() {
        ClassConstructVisualRegistry registry = ClassConstructVisualRegistry.builtIns();

        assertEquals(Set.of(
                        "aegis.utility",
                        "bulwark.utility",
                        "shieldbearer.utility",
                        "marshal.signature",
                        "bannerlord.signature",
                        "artillerist.utility",
                        "pathfinder.utility",
                        "saboteur.utility",
                        "trapper.signature",
                        "cleric.utility",
                        "saint.utility",
                        "shaman.utility",
                        "lifewarden.utility",
                        "grovekeeper.signature",
                        "grovekeeper.utility"),
                registry.abilityIds());
    }

    @Test
    void paladinDefensiveConstructsAreFixedPacketOnlyWallsOrGround() {
        ClassConstructVisualRegistry registry = ClassConstructVisualRegistry.builtIns();

        for (String abilityId : Set.of(
                "aegis.utility", "bulwark.utility", "shieldbearer.utility")) {
            ClassConstructVisualRegistry.Plan plan = registry.require(abilityId);
            assertEquals(ClassConstructVisualRegistry.AnchorMode.FIXED, plan.anchorMode());
            assertFalse(plan.definition().blocks().isEmpty());
        }
    }

    @Test
    void onlyTheCarriedGrandStandardFollowsItsCaster() {
        ClassConstructVisualRegistry registry = ClassConstructVisualRegistry.builtIns();

        assertEquals(ClassConstructVisualRegistry.AnchorMode.FOLLOW_CASTER,
                registry.require("bannerlord.signature").anchorMode());
        assertTrue(registry.abilityIds().stream()
                .filter(id -> !id.equals("bannerlord.signature"))
                .map(registry::require)
                .allMatch(plan -> plan.anchorMode() == ClassConstructVisualRegistry.AnchorMode.FIXED));
    }

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
