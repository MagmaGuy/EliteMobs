package com.magmaguy.elitemobs.events.timedevents;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.events.EliteEvent;
import com.magmaguy.elitemobs.events.EventWorldFilter;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import org.bukkit.Location;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DeadMoonEvent extends EliteEvent implements Listener {

    public DeadMoonEvent() {
        super(EventWorldFilter.randomizeValidWorld(WorldType.NORMAL), EventType.SURVIVAL, EntityType.ZOMBIE);
    }

    @Override
    public void activateEvent(Location location) {
        super.setBossEntity(CustomBossEntity.constructCustomBoss("zombie_king.yml", location, DynamicBossLevelConstructor.findDynamicBossLevel()));
        super.setEventStartMessage(ChatColorConverter.convert(ConfigValues.eventsConfig.getString(EventsConfig.DEADMOON_ANNOUNCEMENT_MESSAGE)));
        super.sendEventStartMessage(getWorld());
        unQueue();
    }

    @Override
    public void spawnEventHandler(CreatureSpawnEvent event) {
        if (isQueued())
            if (event.getEntity().equals(super.getEntityType()))
                activateEvent(event.getLocation());

        if (super.getBossIsAlive()) {
            //do the returned spawn
        }
    }

    @Override
    public void eventWatchdog() {
        new BukkitRunnable() {
            boolean startMessageSent = false;

            @Override
            public void run() {

                if (!(getWorld().getTime() > 22800 ||
                        getWorld().getTime() < 13184 ||
                        MoonPhaseDetector.detectMoonPhase(getWorld()) != MoonPhaseDetector.moonPhase.NEW_MOON) &&
                        !startMessageSent)
                    return;
                cancel();
                completeEvent(getWorld());
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);

    }

}
