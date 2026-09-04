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

    @Test
    void coversEveryKnownHostileProducing26_2Reason() {
        EnumSet<CreatureSpawnEvent.SpawnReason> hostileProducing = EnumSet.of(
                CreatureSpawnEvent.SpawnReason.NATURAL,
                CreatureSpawnEvent.SpawnReason.JOCKEY,
                CreatureSpawnEvent.SpawnReason.CHUNK_GEN,
                CreatureSpawnEvent.SpawnReason.SPAWNER,
                CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER,
                CreatureSpawnEvent.SpawnReason.LIGHTNING,
                CreatureSpawnEvent.SpawnReason.BUILD_WITHER,
                CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION,
                CreatureSpawnEvent.SpawnReason.BREEDING,
                CreatureSpawnEvent.SpawnReason.SLIME_SPLIT,
                CreatureSpawnEvent.SpawnReason.REINFORCEMENTS,
                CreatureSpawnEvent.SpawnReason.NETHER_PORTAL,
                CreatureSpawnEvent.SpawnReason.INFECTION,
                CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK,
                CreatureSpawnEvent.SpawnReason.MOUNT,
                CreatureSpawnEvent.SpawnReason.TRAP,
                CreatureSpawnEvent.SpawnReason.ENDER_PEARL,
                CreatureSpawnEvent.SpawnReason.DROWNED,
                CreatureSpawnEvent.SpawnReason.EXPLOSION,
                CreatureSpawnEvent.SpawnReason.RAID,
                CreatureSpawnEvent.SpawnReason.PATROL,
                CreatureSpawnEvent.SpawnReason.BEEHIVE,
                CreatureSpawnEvent.SpawnReason.PIGLIN_ZOMBIFIED,
                CreatureSpawnEvent.SpawnReason.SPELL,
                CreatureSpawnEvent.SpawnReason.FROZEN,
                CreatureSpawnEvent.SpawnReason.ENCHANTMENT,
                CreatureSpawnEvent.SpawnReason.POTION_EFFECT);

        assertEquals(
                hostileProducing,
                intersection(hostileProducing, EliteNaturalSpawnReasonPolicy.providerEligibleReasons()));
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
