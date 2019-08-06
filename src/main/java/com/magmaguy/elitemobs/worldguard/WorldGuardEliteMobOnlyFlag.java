package com.magmaguy.elitemobs.worldguard;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.EntityTracker;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WorldGuardEliteMobOnlyFlag implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!EliteMobs.worldguardIsEnabled) return;
        if (EntityTracker.isEliteMob(event.getEntity())) return;
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(event.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
        if (set.testState(null, (StateFlag) WorldGuardCompatibility.getEliteMobsOnlySpawnFlag()))
            event.setCancelled(true);

    }

}
