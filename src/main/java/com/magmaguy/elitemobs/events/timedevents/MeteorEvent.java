package com.magmaguy.elitemobs.events.timedevents;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.config.events.premade.MeteorEventConfig;
import com.magmaguy.elitemobs.events.EliteEvent;
import com.magmaguy.elitemobs.events.EventWorldFilter;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MeteorEvent extends EliteEvent implements Listener {

    public MeteorEvent() {
        super(EventWorldFilter.getValidWorlds(Arrays.asList(WorldType.NORMAL, WorldType.AMPLIFIED)), EventType.KILL_BOSS, EntityType.BLAZE);
    }

    @Override
    public void activateEvent(Location location) {
        if (location.getWorld().getEnvironment().equals(World.Environment.NETHER))
            return;
        unQueue();
        eventWatchdog();
        FallingBlock meteor = createMeteorite(location);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (!meteor.getLocation().subtract(new Vector(0, 1, 0)).getBlock().getType().equals(Material.AIR) &&
                        !meteor.getLocation().subtract(new Vector(0, 1, 0)).getBlock().getType().equals(Material.VOID_AIR)) {
                    Location landingLocation = meteor.getLocation().clone();
                    cancel();
                    setBossEntity(CustomBossEntity.constructCustomBoss("blayyze.yml", landingLocation.clone().add(new Vector(0, 10, 0)), DynamicBossLevelConstructor.findDynamicBossLevel()));
                    for (int i = 0; i < 5; i++)
                        CustomBossEntity.constructCustomBoss("ember.yml", landingLocation.add(new Vector(0, 10, 0)), DynamicBossLevelConstructor.findDynamicBossLevel());
                    return;
                }
                counter++;
                if (counter > 60 * 20 * 2)
                    cancel();
                meteor.getWorld().spawnParticle(Particle.SMOKE_LARGE, meteor.getLocation(), 10, 0, 0, 0);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

        super.setEventStartMessage(MeteorEventConfig.eventAnnouncementMessage.replace("$location", meteor.getLocation().getBlockX() + "," + meteor.getLocation().getBlockZ()));
        super.sendEventStartMessage(getActiveWorld());
    }

    private FallingBlock createMeteorite(Location location) {
        Location meteorLocation = location.clone();
        meteorLocation.setY(255);
        FallingBlock fallingBlock = null;

        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                for (int z = 0; z < 3; z++) {
                    Location blockLocation = meteorLocation.clone().add(new Vector(-1, 0, -1));
                    blockLocation = blockLocation.add(new Vector(x, y, z));
                    BlockData blockData = null;
                    if (ThreadLocalRandom.current().nextDouble() < 0.1)
                        blockData = Bukkit.createBlockData(Material.DIAMOND_ORE);
                    else if (ThreadLocalRandom.current().nextDouble() < 0.2)
                        blockData = Bukkit.createBlockData(Material.IRON_ORE);
                    else if (ThreadLocalRandom.current().nextDouble() < 0.3)
                        blockData = Bukkit.createBlockData(Material.COAL_ORE);
                    else
                        blockData = Bukkit.createBlockData(Material.MAGMA_BLOCK);
                    fallingBlock = location.getWorld().spawnFallingBlock(blockLocation, blockData);
                }

        return fallingBlock;
    }

    @Override
    public void spawnEventHandler(CreatureSpawnEvent event) {
        if (!isQueued()) return;
        activateEvent(event.getLocation());
    }

    @Override
    public void eventWatchdog() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getBossEntity() == null || !getBossEntity().advancedGetEntity().isDead()) return;
                cancel();
                silentCompleteEvent();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 60, 20);
    }

    @Override
    public void endEvent() {
        this.completeEvent(getActiveWorld());
    }

    @Override
    public void bossDeathEventHandler(EliteMobDeathEvent event) {
        endEvent();
    }

}
