package com.magmaguy.elitemobs.pathfinding.patrol;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
