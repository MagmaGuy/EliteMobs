package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class PersistentObjectWorldLifecycleTest {
    private ServerMock server;
    private World world;

    @BeforeEach
    void open() {
        server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        world = server.addSimpleWorld("external-instance");
        PersistentObjectHandler.shutdown();
        server.getPluginManager().registerEvents(new PersistentObjectHandler.PersistentObjectHandlerEvents(), MetadataHandler.PLUGIN);
    }

    @AfterEach
    void close() {
        PersistentObjectHandler.shutdown();
        EntityTracker.getEliteMobEntities().clear();
        MockBukkit.unmock();
        MetadataHandler.PLUGIN = null;
    }

    @Test
    void deferredInstancedBossCannotBeRegisteredAgainByItsWorldUnloadCallback() {
        Location spawn = new Location(world, 1600, 64, 1600);
        assertFalse(world.isChunkLoaded(100, 100));
        var config = new CustomBossesConfigFields("external_guard.yml", EntityType.ZOMBIE,
                true, "External guard", "dynamic");
        var boss = new InstancedBossEntity(config, spawn, 37);
        boss.spawn(true);
        assertNull(boss.getLivingEntity(), "An unloaded chunk must defer materialization");
        assertFalse(EntityTracker.getEliteMobEntities().containsValue(boss));

        server.getPluginManager().callEvent(new WorldUnloadEvent(world));
        assertNull(boss.getSpawnLocation().getWorld());
        server.getPluginManager().callEvent(new WorldLoadEvent(world));

        assertNull(boss.getSpawnLocation().getWorld(),
                "A removed instance boss must not reattach when a world with the same name loads");
        assertNull(boss.getLivingEntity());
    }

    @Test
    void persistentObjectsThatRemainRegisteredRestoreNormally() {
        RestorableObject object = new RestorableObject(new Location(world, 1600, 64, 1600));
        new PersistentObjectHandler(object);

        server.getPluginManager().callEvent(new WorldUnloadEvent(world));
        assertNull(object.location.getWorld());
        server.getPluginManager().callEvent(new WorldLoadEvent(world));

        assertSame(world, object.location.getWorld());
        assertEquals(1, object.loads);
        server.getPluginManager().callEvent(new WorldLoadEvent(world));
        assertEquals(1, object.loads, "A restored registration must move back to its chunk bucket");
    }

    private static final class RestorableObject implements PersistentObject {
        private final Location location;
        private final String worldName;
        private int loads;

        private RestorableObject(Location location) {
            this.location = location;
            this.worldName = location.getWorld().getName();
        }

        @Override public void chunkLoad() { }
        @Override public void chunkUnload() { }
        @Override public void worldLoad(World world) { location.setWorld(world); loads++; }
        @Override public void worldUnload() { location.setWorld(null); }
        @Override public Location getPersistentLocation() { return location; }
        @Override public String getWorldName() { return worldName; }
    }
}
