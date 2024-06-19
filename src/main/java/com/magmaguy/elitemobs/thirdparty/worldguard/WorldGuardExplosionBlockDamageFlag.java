package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WorldGuardExplosionBlockDamageFlag implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void WorldGuardExplosionBlockDamageFlag(EntityExplodeEvent event) {
        if (WorldGuardFlagChecker.checkFlag(event.getLocation(), WorldGuardCompatibility.getELITEMOBS_EXPLOSION_BLOCK_DAMAGE()))
            return;
        if (!EntityTracker.isProjectileEntity(event.getEntity()) && !EntityTracker.isEliteMob(event.getEntity()))
            return;
        event.blockList().clear();
    }
}
