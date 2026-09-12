package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EliteSlimeDeathSplitTest {
    private WorldMock world;
    private JavaPlugin previousPlugin;

    @BeforeEach
    void open() {
        previousPlugin = MetadataHandler.PLUGIN;
        var server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        world = server.addSimpleWorld("slime-split");
        server.getPluginManager().registerEvents(new EliteSlimeDeathSplit(), MetadataHandler.PLUGIN);
    }

    @AfterEach
    void close() {
        MockBukkit.unmock();
        MetadataHandler.PLUGIN = previousPlugin;
    }

    @Test
    void eliteSlimeCannotSplitAfterItsActorWasUnregistered() {
        Slime slime = world.spawn(world.getSpawnLocation(), Slime.class);
        PersistentTagger.tagElite(slime, UUID.randomUUID());
        assertNull(EntityTracker.getEliteMobEntity(slime));

        assertTrue(transform(slime, EntityTransformEvent.TransformReason.SPLIT).isCancelled());
    }

    @Test
    void eliteMagmaCubeCannotSplit() {
        MagmaCube cube = world.spawn(world.getSpawnLocation(), MagmaCube.class);
        PersistentTagger.tagElite(cube, UUID.randomUUID());

        assertTrue(transform(cube, EntityTransformEvent.TransformReason.SPLIT).isCancelled());
    }

    @Test
    void ordinarySlimeStillSplits() {
        Slime slime = world.spawn(world.getSpawnLocation(), Slime.class);

        assertFalse(transform(slime, EntityTransformEvent.TransformReason.SPLIT).isCancelled());
    }

    @Test
    void otherSlimeTransformationsAreLeftToTheirExistingHandler() {
        Slime slime = world.spawn(world.getSpawnLocation(), Slime.class);
        PersistentTagger.tagElite(slime, UUID.randomUUID());

        assertFalse(transform(slime, EntityTransformEvent.TransformReason.UNKNOWN).isCancelled());
    }

    @Test
    void otherEliteEntitySplitsAreLeftToTheirExistingHandler() {
        Zombie zombie = world.spawn(world.getSpawnLocation(), Zombie.class);
        PersistentTagger.tagElite(zombie, UUID.randomUUID());

        assertFalse(transform(zombie, EntityTransformEvent.TransformReason.SPLIT).isCancelled());
    }

    private EntityTransformEvent transform(Entity entity, EntityTransformEvent.TransformReason reason) {
        var event = new EntityTransformEvent(entity, List.of(entity), reason);
        MockBukkit.getMock().getPluginManager().callEvent(event);
        return event;
    }
}
