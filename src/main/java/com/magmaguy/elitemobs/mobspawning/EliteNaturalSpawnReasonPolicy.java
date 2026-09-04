package com.magmaguy.elitemobs.mobspawning;

import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Defines which Bukkit creature spawns an external game mode may claim through EliteMobs.
 *
 * <p>Only reasons with an explicit external owner are exempt. Every other reason is offered to
 * registered providers before EliteMobs' optional global conversion, including vanilla
 * transformations, raids, patrols, reinforcements, portals, and spawners. Providers still decide
 * whether the carrier belongs to them.</p>
 */
final class EliteNaturalSpawnReasonPolicy {
    private static final EnumSet<CreatureSpawnEvent.SpawnReason> EXTERNALLY_OWNED = EnumSet.of(
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
            CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
            CreatureSpawnEvent.SpawnReason.COMMAND,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            CreatureSpawnEvent.SpawnReason.DEFAULT);
    private static final EnumSet<CreatureSpawnEvent.SpawnReason> PROVIDER_ELIGIBLE =
            EnumSet.complementOf(EXTERNALLY_OWNED);

    private EliteNaturalSpawnReasonPolicy() {
    }

    static boolean isProviderEligible(CreatureSpawnEvent.SpawnReason reason) {
        return PROVIDER_ELIGIBLE.contains(Objects.requireNonNull(reason, "reason"));
    }

    static EnumSet<CreatureSpawnEvent.SpawnReason> externallyOwnedReasons() {
        return EnumSet.copyOf(EXTERNALLY_OWNED);
    }

    static EnumSet<CreatureSpawnEvent.SpawnReason> providerEligibleReasons() {
        return EnumSet.copyOf(PROVIDER_ELIGIBLE);
    }
}
