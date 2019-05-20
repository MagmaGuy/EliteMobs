package com.magmaguy.elitemobs.events.timedevents;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.events.EliteEvent;
import com.magmaguy.elitemobs.events.EventWorldFilter;
import org.bukkit.Location;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class SmallTreasureGoblinEvent extends EliteEvent implements Listener {

    public SmallTreasureGoblinEvent() {
        super(EventWorldFilter.randomizeValidWorld(WorldType.NORMAL), EventType.KILL_BOSS, EntityType.ZOMBIE);
    }

    @Override
    public void activateEvent(Location location) {
        unQueue();
    }

    @Override
    public void spawnEventHandler(CreatureSpawnEvent event) {
        if (!isQueued()) return;
        if (event.getEntity().equals(super.getEntityType()))
            activateEvent(event.getLocation());
    }

    @Override
    public void bossDeathEventHandler(EliteMobDeathEvent event) {
        endEvent();
    }

    @Override
    public void endEvent() {
        this.completeEvent(getWorld());
    }

    @Override
    public void eventWatchdog() {
    }

}
