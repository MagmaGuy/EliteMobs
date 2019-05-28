package com.magmaguy.elitemobs.events.timedevents;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.events.EliteEvent;
import com.magmaguy.elitemobs.events.EventWorldFilter;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import com.magmaguy.elitemobs.mobspawning.NaturalEliteMobSpawnEventHandler;
import org.bukkit.Location;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DeadMoonEvent extends EliteEvent implements Listener {

    public DeadMoonEvent() {
        super(EventWorldFilter.getValidWorlds(WorldType.NORMAL), EventType.SURVIVAL, EntityType.ZOMBIE);
    }

    @Override
    public void activateEvent(Location location) {
        unQueue();
        super.setBossEntity(CustomBossEntity.constructCustomBoss("zombie_king.yml", location, DynamicBossLevelConstructor.findDynamicBossLevel()));
        super.setEventStartMessage(ChatColorConverter.convert(ConfigValues.eventsConfig.getString(EventsConfig.DEADMOON_ANNOUNCEMENT_MESSAGE)));
        super.sendEventStartMessage(getActiveWorld());
    }

    @Override
    public void spawnEventHandler(CreatureSpawnEvent event) {
        if (isQueued())
            if (MoonPhaseDetector.detectMoonPhase(event.getEntity().getWorld()).equals(MoonPhaseDetector.MoonPhase.NEW_MOON))
                if (event.getEntity().getType().equals(super.getEntityType()))
                    activateEvent(event.getLocation());

        if (super.getBossIsAlive())
            if (event.getEntity().getType().equals(EntityType.ZOMBIE))
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!EntityTracker.isEliteMob(event.getEntity()))
                            CustomBossEntity.constructCustomBoss("the_returned.yml", event.getLocation(),
                                    NaturalEliteMobSpawnEventHandler.getNaturalMobLevel(event.getLocation()));
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

    @Override
    public void eventWatchdog() {
        new BukkitRunnable() {
            boolean startMessageSent = false;

            @Override
            public void run() {

                if (!(getActiveWorld().getTime() > 22800 ||
                        getActiveWorld().getTime() < 13184 ||
                        MoonPhaseDetector.detectMoonPhase(getActiveWorld()) != MoonPhaseDetector.MoonPhase.NEW_MOON) &&
                        !startMessageSent)
                    return;
                cancel();
                completeEvent(getActiveWorld());
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);

    }

}
