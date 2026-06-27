package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlock;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBossBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhaseBossEntityListenerTest {

    @AfterEach
    void tearDownMockBukkit() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void phaseResetRunsAfterTransitiveBlockCleanupOnRemove() throws NoSuchMethodException {
        Method resetMethod = PhaseBossEntity.PhaseBossEntityListener.class
                .getDeclaredMethod("onEliteRemove", EliteMobRemoveEvent.class);
        Method cleanupMethod = TransitiveBossBlock.class
                .getDeclaredMethod("onBossRemove", EliteMobRemoveEvent.class);

        EventHandler resetHandler = resetMethod.getAnnotation(EventHandler.class);
        EventHandler cleanupHandler = cleanupMethod.getAnnotation(EventHandler.class);

        assertNotNull(resetHandler);
        assertNotNull(cleanupHandler);
        assertEquals(EventPriority.MONITOR, resetHandler.priority());
        assertEquals(EventPriority.NORMAL, cleanupHandler.priority());
        assertTrue(cleanupHandler.priority().ordinal() < resetHandler.priority().ordinal(),
                "Transitive block cleanup must see the phase that died before phase state resets.");
    }

    @Test
    void phaseResetDoesNotRunDuringDeathEvent() {
        boolean listensToDeathEvent = Arrays.stream(PhaseBossEntity.PhaseBossEntityListener.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(EventHandler.class))
                .flatMap(method -> Arrays.stream(method.getParameterTypes()))
                .anyMatch(EliteMobDeathEvent.class::equals);

        assertFalse(listensToDeathEvent,
                "Death-event cleanup must run before phase reset changes the active custom boss config.");
    }

    @Test
    void removeEventClearsConfiguredTransitiveBlocksInMockWorld() throws Exception {
        ServerMock server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        World world = server.addSimpleWorld("transitive_blocks");
        world.loadChunk(0, 0);

        Location spawnLocation = new Location(world, 0, 64, 0);
        Location barrierLocation = spawnLocation.clone().add(1, 0, 0);
        barrierLocation.getBlock().setType(Material.BARRIER);

        RegionalBossEntity boss = allocateRegionalBoss(spawnLocation, List.of(
                new TransitiveBlock(Material.AIR.createBlockData(), new Vector(1, 0, 0))));

        server.getPluginManager().registerEvents(new TransitiveBossBlock(), plugin);
        server.getPluginManager().callEvent(new EliteMobRemoveEvent(boss, RemovalReason.DEATH));

        assertEquals(Material.AIR, barrierLocation.getBlock().getType());
    }

    private static RegionalBossEntity allocateRegionalBoss(Location spawnLocation, List<TransitiveBlock> removeBlocks) throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        RegionalBossEntity boss = (RegionalBossEntity) unsafe.allocateInstance(RegionalBossEntity.class);
        boss.setOnRemoveTransitiveBlocks(removeBlocks);
        setField(EliteEntity.class, boss, "spawnLocation", spawnLocation);
        return boss;
    }

    private static void setField(Class<?> ownerClass, Object target, String fieldName, Object value) throws Exception {
        Field field = ownerClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
