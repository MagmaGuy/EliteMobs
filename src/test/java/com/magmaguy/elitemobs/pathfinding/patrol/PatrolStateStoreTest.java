package com.magmaguy.elitemobs.pathfinding.patrol;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PatrolStateStoreTest {
    @Test
    void progressSavedAgainstOtherRouteGeometryIsNotRestored(@TempDir File folder) {
        PatrolRoute absolute = route(false);
        PatrolRoute misreadAsRelative = route(true);
        PatrolStateStore store = new PatrolStateStore(folder);
        store.put("npc|combat_instructor.yml|world", new PatrolStateStore.StoredState(
                "npc|combat_instructor.yml|world", 0, 1, 1, 0D, 0.25D,
                new PatrolStateStore.StoredLocation("world", 598D, 182D, 430D, 0F, 0F),
                misreadAsRelative.geometryKey()));
        store.saveNow();

        PatrolStateStore reloaded = new PatrolStateStore(folder);

        assertTrue(reloaded.get("npc|combat_instructor.yml|world", misreadAsRelative).isPresent());
        assertTrue(reloaded.get("npc|combat_instructor.yml|world", absolute).isEmpty());
    }

    private static PatrolRoute route(boolean relative) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("patrol.nodes", List.of("299,91,215", "286,91,213"));
        configuration.set("patrol.relative", relative);
        return PatrolRoute.parse(configuration);
    }
}
