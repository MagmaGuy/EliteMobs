package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.ZombieMock;

import java.util.UUID;

/** Bukkit entity state for combat tests; native pickup/despawn behavior is not simulated. */
public final class CombatTestEntities {
    private CombatTestEntities() { }

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
