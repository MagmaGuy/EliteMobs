package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScriptConfigurationIsolationTest {
    @BeforeEach void open() {
        MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
    }

    @AfterEach void close() {
        EliteScriptBlueprint.shutdown();
        MockBukkit.unmock();
    }

    @Test void malformedConditionsDoNotDiscardOtherBossScripts() throws Exception {
        var config = new YamlConfiguration();
        config.loadFromString("""
                Before:
                  Events: [EliteMobSpawnEvent]
                  Actions: []
                Broken:
                  Events: [EliteMobSpawnEvent]
                  Conditions:
                  - isAlive: true
                  Actions: []
                After:
                  Events: [EliteMobDeathEvent]
                  Conditions:
                    isAlive: false
                  Actions: []
                """);
        var boss = new CustomBossesConfigFields("debris_guardian.yml", true);
        var scripts = assertDoesNotThrow(() -> EliteScriptBlueprint.parseBossScripts(config, boss));
        assertEquals(List.of("Before", "After"), scripts.stream().map(EliteScriptBlueprint::getScriptName).toList());
        assertEquals(false, scripts.get(1).getScriptConditionsBlueprint().getIsAlive());
    }

    @Test void malformedCooldownsAreRejectedWithoutLosingValidSibling() throws Exception {
        var config = new YamlConfiguration();
        config.loadFromString("""
                Broken:
                  Cooldowns: [10, 20]
                  Actions: []
                Valid:
                  Cooldowns:
                    local: 20
                  Actions: []
                """);
        var scripts = assertDoesNotThrow(() -> EliteScriptBlueprint.parseBossScripts(config,
                new CustomBossesConfigFields("bad_cooldown.yml", true)));
        assertEquals(List.of("Valid"), scripts.stream().map(EliteScriptBlueprint::getScriptName).toList());
    }
}
