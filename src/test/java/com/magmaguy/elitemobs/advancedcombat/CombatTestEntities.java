package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Location;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.AllayMock;
import org.mockbukkit.mockbukkit.entity.ZombieMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.UUID;
import java.util.function.Consumer;

/** Bukkit entity state for combat tests; native pickup/despawn behavior is not simulated. */
public final class CombatTestEntities {
    private CombatTestEntities() { }

    /**
     * The default test world, except that Spiritcaller's ancestral spirit (an Allay) can spawn:
     * MockBukkit's AllayMock doesn't implement setCanPickupItems.
     */
    public static WorldMock world(String name) {
        var world = new WorldMock() {
            @Override
            public <T extends Entity> T spawn(Location location, Class<T> type, Consumer<? super T> function) {
                if (type != Allay.class) return super.spawn(location, type, function);
                var server = MockBukkit.getMock();
                var spirit = new AllayMock(server, UUID.randomUUID()) {
                    private boolean canPickupItems;

                    @Override public void setCanPickupItems(boolean value) { canPickupItems = value; }
                    @Override public boolean getCanPickupItems() { return canPickupItems; }
                };
                spirit.setLocation(location.clone());
                server.registerEntity(spirit);
                if (function != null) function.accept(type.cast(spirit));
                return type.cast(spirit);
            }
        };
        world.setName(name);
        return world;
    }

    public static EliteEntity spawnElite(Location location) {
        var server = MockBukkit.getMock();
        var body = new ZombieMock(server, UUID.randomUUID()) {
            private boolean canPickupItems;
            private boolean removeWhenFarAway;

            @Override public void setCanPickupItems(boolean value) { canPickupItems = value; }
            @Override public boolean getCanPickupItems() { return canPickupItems; }
            @Override public void setRemoveWhenFarAway(boolean value) { removeWhenFarAway = value; }
            @Override public boolean getRemoveWhenFarAway() { return removeWhenFarAway; }
        };
        body.teleport(location);
        server.registerEntity(body);
        var elite = new EliteEntity();
        elite.setLevel(1);
        elite.setLivingEntity(body, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return elite;
    }
}
