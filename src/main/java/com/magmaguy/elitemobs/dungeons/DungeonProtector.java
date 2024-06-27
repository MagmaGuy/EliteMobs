package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DungeonProtector implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockDamage(BlockDamageEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockBreak(BlockBreakEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockBurnDamage(BlockBurnEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockPlace(BlockCanBuildEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setBuildable(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockExplosionEvent(BlockExplodeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getBlock().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventEntityExplosionEvent(EntityExplodeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getLocation().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getLocation().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.blockList().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventTntPrimeEvent(TNTPrimeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getBlock().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockFadeEvent(BlockFadeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBonemeal(BlockFertilizeEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventLiquidFlow(BlockFromToEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockFire(BlockIgniteEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockPlace(BlockPlaceEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventLeafDecay(LeavesDecayEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventVanillaMobSpawning(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) return;
        if (!EliteMobsWorld.isEliteMobsWorld(event.getLocation().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventFriendlyFireInDungeon(EntityDamageByEntityEvent event) {
        if (DungeonsConfig.isFriendlyFireInDungeons()) return;
        if (!EliteMobsWorld.isEliteMobsWorld(event.getEntity().getLocation().getWorld().getUID())) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player)
            event.setCancelled(true);
    }


}
