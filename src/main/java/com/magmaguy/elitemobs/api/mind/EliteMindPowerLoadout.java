package com.magmaguy.elitemobs.api.mind;

import org.bukkit.NamespacedKey;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/** Power selection resolved atomically while a native Mind actor is prepared. */
public record EliteMindPowerLoadout(boolean randomized, List<NamespacedKey> powerKeys) {

    private static final EliteMindPowerLoadout RANDOMIZED =
            new EliteMindPowerLoadout(true, List.of());

    public EliteMindPowerLoadout {
        Objects.requireNonNull(powerKeys, "powerKeys");
        powerKeys = List.copyOf(powerKeys);
        if (randomized && !powerKeys.isEmpty()) {
            throw new IllegalArgumentException("A randomized power loadout cannot contain exact keys");
        }
        if (new LinkedHashSet<>(powerKeys).size() != powerKeys.size()) {
            throw new IllegalArgumentException("An exact power loadout cannot contain duplicate keys");
        }
    }

    public static EliteMindPowerLoadout randomizedPowers() {
        return RANDOMIZED;
    }

    public static EliteMindPowerLoadout exact(List<NamespacedKey> powerKeys) {
        return new EliteMindPowerLoadout(false, powerKeys);
    }
}
