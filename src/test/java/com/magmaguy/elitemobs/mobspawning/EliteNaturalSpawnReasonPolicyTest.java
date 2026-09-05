package com.magmaguy.elitemobs.mobspawning;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EliteNaturalSpawnReasonPolicyTest {

    @Test
    void classifiesEveryExact26_2ReasonWithoutLeavingAProviderBypass() {
        EnumSet<CreatureSpawnEvent.SpawnReason> externallyOwned = EnumSet.of(
                CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
                CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
                CreatureSpawnEvent.SpawnReason.COMMAND,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                CreatureSpawnEvent.SpawnReason.DEFAULT);
        EnumSet<CreatureSpawnEvent.SpawnReason> environmentOwned =
                EnumSet.allOf(CreatureSpawnEvent.SpawnReason.class);
        environmentOwned.removeAll(externallyOwned);

        assertEquals(externallyOwned, EliteNaturalSpawnReasonPolicy.externallyOwnedReasons());
        assertEquals(environmentOwned, EliteNaturalSpawnReasonPolicy.providerEligibleReasons());
        assertEquals(
                EnumSet.allOf(CreatureSpawnEvent.SpawnReason.class),
                union(externallyOwned, environmentOwned));
    }

    private static EnumSet<CreatureSpawnEvent.SpawnReason> union(
            EnumSet<CreatureSpawnEvent.SpawnReason> left,
            EnumSet<CreatureSpawnEvent.SpawnReason> right) {
        EnumSet<CreatureSpawnEvent.SpawnReason> union = EnumSet.copyOf(left);
        union.addAll(right);
        return union;
    }

    private static EnumSet<CreatureSpawnEvent.SpawnReason> intersection(
            EnumSet<CreatureSpawnEvent.SpawnReason> left,
            EnumSet<CreatureSpawnEvent.SpawnReason> right) {
        EnumSet<CreatureSpawnEvent.SpawnReason> intersection = EnumSet.copyOf(left);
        intersection.retainAll(right);
        return intersection;
    }
}
