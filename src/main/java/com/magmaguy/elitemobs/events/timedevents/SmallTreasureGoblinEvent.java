package com.magmaguy.elitemobs.events.timedevents;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.events.EliteEvent;
import com.magmaguy.elitemobs.events.EventWorldFilter;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import org.bukkit.Location;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class SmallTreasureGoblinEvent extends EliteEvent implements Listener {

    public SmallTreasureGoblinEvent() {
        super(EventWorldFilter.getValidWorlds(WorldType.NORMAL), EventType.KILL_BOSS, EntityType.ZOMBIE);
    }

    @Override
    public void activateEvent(Location location) {
        unQueue();
        super.setBossEntity(CustomBossEntity.constructCustomBoss("treasure_goblin.yml", location, DynamicBossLevelConstructor.findDynamicBossLevel()));
    }

    @Override
    public void spawnEventHandler(CreatureSpawnEvent event) {
        if (!isQueued()) return;
        if (event.getEntity().getType().equals(super.getEntityType()))
            activateEvent(event.getLocation());
    }


    @Override
    public void bossDeathEventHandler(EliteMobDeathEvent event) {
        endEvent();
    }

    @Override
    public void endEvent() {
        this.completeEvent(getActiveWorld());
    }

    @Override
    public void eventWatchdog() {
    }

}
