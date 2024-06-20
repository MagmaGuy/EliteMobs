package com.magmaguy.elitemobs.dungeons;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DungeonProtector implements Listener {

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockDamage(BlockDamageEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockBreak(BlockBreakEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockBurnDamage(BlockBurnEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventPlayerBlockPlace(BlockCanBuildEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setBuildable(false);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockExplosionEvent(BlockExplodeEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getBlock().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventEntityExplosionEvent(EntityExplodeEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getLocation().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getLocation().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.blockList().clear();
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventTntPrimeEvent(TNTPrimeEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        EliteMobsWorld eliteMobsWorld = EliteMobsWorld.getEliteMobsWorld(event.getBlock().getWorld().getUID());
        if (!eliteMobsWorld.isAllowExplosions())
            event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockFadeEvent(BlockFadeEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBonemeal(BlockFertilizeEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventLiquidFlow(BlockFromToEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockFire(BlockIgniteEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventBlockPlace(BlockPlaceEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void preventLeafDecay(LeavesDecayEvent event){
        if (!EliteMobsWorld.isEliteMobsWorld(event.getBlock().getWorld().getUID())) return;
        event.setCancelled(true);
    }


}
