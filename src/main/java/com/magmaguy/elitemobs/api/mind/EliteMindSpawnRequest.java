package com.magmaguy.elitemobs.api.mind;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/** Inputs for spawning a native hostile Mind body. */
public record EliteMindSpawnRequest(
        Plugin owner,
        NamespacedKey programKey,
        Location location,
        int level,
        boolean persistent,
        EliteMindBodyProfile bodyProfile,
        EliteMindPowerLoadout powerLoadout) {

    /** Source-compatible default for callers that want the standard grounded body. */
    public EliteMindSpawnRequest(
            Plugin owner,
            NamespacedKey programKey,
            Location location,
            int level,
            boolean persistent) {
        this(
                owner,
                programKey,
                location,
                level,
                persistent,
                EliteMindBodyProfile.GROUNDED,
                EliteMindPowerLoadout.randomizedPowers());
    }

    /** Source-compatible body-profile constructor with the historical randomized power loadout. */
    public EliteMindSpawnRequest(
            Plugin owner,
            NamespacedKey programKey,
            Location location,
            int level,
            boolean persistent,
            EliteMindBodyProfile bodyProfile) {
        this(
                owner,
                programKey,
                location,
                level,
                persistent,
                bodyProfile,
                EliteMindPowerLoadout.randomizedPowers());
    }

    public EliteMindSpawnRequest {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(programKey, "programKey");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(bodyProfile, "bodyProfile");
        Objects.requireNonNull(powerLoadout, "powerLoadout");
        if (location.getWorld() == null) throw new IllegalArgumentException("location must have a world");
        if (level < 1) throw new IllegalArgumentException("level must be positive");
        location = location.clone();
    }

    @Override
    public Location location() {
        return location.clone();
    }
}
