package com.magmaguy.elitemobs.events.actionevents;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.ActionDynamicBossLevelConstructor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class TreeChoppingEvent implements Listener {

    @EventHandler
    public void onTreeFell(BlockBreakEvent event) {

        if (event.isCancelled()) return;
        if (!event.getPlayer().hasPermission("elitemobs.events.fae")) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR)
            return;
        if (!(event.getBlock().getType().equals(Material.ACACIA_WOOD)
                || event.getBlock().getType().equals(Material.BIRCH_WOOD)
                || event.getBlock().getType().equals(Material.DARK_OAK_WOOD)
                || event.getBlock().getType().equals(Material.JUNGLE_WOOD)
                || event.getBlock().getType().equals(Material.OAK_WOOD)
                || event.getBlock().getType().equals(Material.SPRUCE_WOOD)))
            return;

        if (ThreadLocalRandom.current().nextDouble() > ConfigValues.eventsConfig.getDouble(EventsConfig.FAE_CHANCE_ON_CHOP))
            return;

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {

                if (i > 20 * 3) {
                    CustomBossEntity.constructCustomBoss("lightning_fae.yml", event.getBlock().getLocation(),
                            ActionDynamicBossLevelConstructor.determineDynamicBossLevel(event.getBlock().getLocation()));
                    CustomBossEntity.constructCustomBoss("fire_fae.yml", event.getBlock().getLocation(),
                            ActionDynamicBossLevelConstructor.determineDynamicBossLevel(event.getBlock().getLocation()));
                    CustomBossEntity.constructCustomBoss("ice_fae.yml", event.getBlock().getLocation(),
                            ActionDynamicBossLevelConstructor.determineDynamicBossLevel(event.getBlock().getLocation()));
                    cancel();
                    return;
                }

                event.getBlock().getLocation().getWorld().spawnParticle(Particle.SPELL, event.getBlock().getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
                event.getBlock().getLocation().getWorld().spawnParticle(Particle.FLAME, event.getBlock().getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
                event.getBlock().getLocation().getWorld().spawnParticle(Particle.SPELL, event.getBlock().getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
                event.getBlock().getLocation().getWorld().spawnParticle(Particle.WATER_DROP, event.getBlock().getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
                i++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

}
