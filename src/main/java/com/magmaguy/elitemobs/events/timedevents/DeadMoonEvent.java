package com.magmaguy.elitemobs.events.timedevents;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.config.events.premade.DeadMoonEventConfig;
import com.magmaguy.elitemobs.events.EliteEvent;
import com.magmaguy.elitemobs.events.EventWorldFilter;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import com.magmaguy.elitemobs.utils.PlayerScanner;
import org.bukkit.Location;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class DeadMoonEvent extends EliteEvent implements Listener {

    public DeadMoonEvent() {
        super(EventWorldFilter.getValidWorlds(Arrays.asList(WorldType.NORMAL, WorldType.AMPLIFIED)), EventType.SURVIVAL, EntityType.ZOMBIE);
    }

    @Override
    public void activateEvent(Location location) {
        unQueue();
        super.setBossEntity(CustomBossEntity.constructCustomBoss("zombie_king.yml", location, DynamicBossLevelConstructor.findDynamicBossLevel()));
        super.setEventStartMessage(DeadMoonEventConfig.eventAnnouncementMessage);
        super.sendEventStartMessage(getActiveWorld());
        DeadMoonEvent deadMoonEvent = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!EliteEvent.getActiveEvents().containsValue(deadMoonEvent)) {
                    cancel();
                    return;
                }
                if (deadMoonEvent.getActiveWorld().getTime() > 24000)
                    completeEvent(deadMoonEvent.getActiveWorld());
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
        super.isActive = true;
        eventWatchdog();
    }

    @Override
    public void spawnEventHandler(CreatureSpawnEvent event) {
        if (isQueued())
            if (MoonPhaseDetector.detectMoonPhase(event.getEntity().getWorld()).equals(MoonPhaseDetector.MoonPhase.NEW_MOON))
                if (event.getEntity().getType().equals(super.getEntityType()))
                    activateEvent(event.getLocation());

        if (super.isActive)
            if (event.getEntity().getType().equals(EntityType.ZOMBIE))
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!EntityTracker.isEliteMob(event.getEntity()))
                            CustomBossEntity.constructCustomBoss("the_returned.yml", event.getLocation(),
                                    NaturalMobSpawnEventHandler.getNaturalMobLevel(event.getLocation(), PlayerScanner.getNearbyPlayers(event.getLocation())));
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 1);
    }

    @Override
    public void eventWatchdog() {

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!(getActiveWorld().getTime() > 22800 ||
                        getActiveWorld().getTime() < 13184 ||
                        MoonPhaseDetector.detectMoonPhase(getActiveWorld()) != MoonPhaseDetector.MoonPhase.NEW_MOON) &&
                        DeadMoonEvent.super.isActive)
                    return;
                cancel();
                completeEvent(getActiveWorld());
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);

    }

    @Override
    public void bossDeathEventHandler(EliteMobDeathEvent event) {
        super.completeEvent(super.getActiveWorld());
    }

}
