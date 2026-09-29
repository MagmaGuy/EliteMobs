package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class ExternalInstancedBossApiTest {
    private Location location;
    private CustomBossesConfigFields config;

    @BeforeEach
    void open() {
        var server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        location = server.addSimpleWorld("external-instance").getSpawnLocation();
        config = new CustomBossesConfigFields("external_guard.yml", EntityType.ZOMBIE,
                true, "External guard", "dynamic");
    }

    @AfterEach
    void close() {
        MockBukkit.unmock();
    }

    @Test
    void externalConstructorRetainsExplicitLevelAndInstancePersistenceWithoutAMatchObject() {
        var boss = new InstancedBossEntity(config, location, 37);

        assertEquals(37, boss.getLevel());
        assertEquals(location, boss.getSpawnLocation());
        assertTrue(boss.getIsPersistent());
        assertNull(boss.getDungeonInstance());
        assertNull(boss.getLivingEntity(), "Construction must leave spawning under the caller's control");
        assertTrue(RegionalBossEntity.getRegionalBossEntities(config).isEmpty(),
                "External instances must not become permanent regional spawn definitions");

        boss.setCustomBossesConfigFields(config);
        assertEquals(37, boss.getLevel(), "A dynamic phase definition must preserve the external instance level");
    }

    @Test
    void namedFactoryReturnsAnInstancedBossWithTheRequestedLevel() {
        try (var bosses = mockStatic(CustomBossesConfig.class)) {
            bosses.when(() -> CustomBossesConfig.getCustomBoss(config.getFilename())).thenReturn(config);

            var boss = assertInstanceOf(InstancedBossEntity.class,
                    InstancedBossEntity.createInstancedBossEntity(config.getFilename(), location, 19));

            assertEquals(19, boss.getLevel());
            assertSame(config, boss.getCustomBossesConfigFields());
            assertEquals(location, boss.getSpawnLocation());
            assertTrue(boss.getIsPersistent());
            assertNull(boss.getLivingEntity());
        }
    }
}
