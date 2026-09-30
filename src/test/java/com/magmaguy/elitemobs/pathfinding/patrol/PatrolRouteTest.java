package com.magmaguy.elitemobs.pathfinding.patrol;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatrolRouteTest {
    @Test
    void acceptsArbitrarilyLongAuthoredLegsAndIgnoresLegacyLimit() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("patrol.nodes", List.of("0,0,0", "1000,0,0"));
        configuration.set("patrol.maxLegDistance", 12D);

        PatrolRoute route = PatrolRoute.parse(configuration);

        assertNotNull(route);
        assertEquals(1000D, route.nodes().get(1).getX());
    }

    @Test
    void startNodeWaitDefaultsToNoRestAndConvertsSecondsToTicks() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("patrol.nodes", List.of("0,0,0", "10,0,0"));
        assertEquals(0, PatrolRoute.parse(configuration).startNodeWaitTicks());

        configuration.set("patrol.startNodeWaitSeconds", 2.5D);
        assertEquals(50, PatrolRoute.parse(configuration).startNodeWaitTicks());

        configuration.set("patrol.startNodeWaitSeconds", -1D);
        assertThrows(IllegalArgumentException.class, () -> PatrolRoute.parse(configuration));
    }

    @Test
    void premadeDefaultsAreReadBeforeTheirFirstSave() {
        // Premade NPC files add patrol keys as defaults; the route is parsed before they are saved.
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.addDefault("patrol.nodes", List.of("299,91,215", "286,91,213"));
        configuration.addDefault("patrol.relative", false);
        configuration.addDefault("patrol.speed", 0.5D);
        configuration.addDefault("patrol.startNodeWaitSeconds", 60D);

        PatrolRoute route = PatrolRoute.parse(configuration);

        assertFalse(route.relative());
        assertEquals(0.5D, route.speedModifier());
        assertEquals(1200, route.startNodeWaitTicks());
    }
}
