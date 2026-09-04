package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.CustomConfigInheritancePolicy;

import java.util.Set;

/** Consumer-owned exclusions for sparse boss/NPC inheritance. */
public final class EliteMobsConfigInheritance {
    public static final CustomConfigInheritancePolicy POLICY =
            CustomConfigInheritancePolicy.excludingRoots(Set.of(
                    "spawnLocation",
                    "spawnLocations",
                    "isEnabled",
                    "leashRadius",
                    "powers",
                    "eliteScript"));

    private EliteMobsConfigInheritance() {
    }
}
