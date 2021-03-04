package com.magmaguy.elitemobs.thirdparty.worldguard;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WorldGuardDungeonFlag implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!EliteMobs.worldguardIsEnabled) return;
        if (event.getEntity().getType().equals(EntityType.ARMOR_STAND) ||
                event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM))
            return;
        if (WorldGuardFlagChecker.checkFlag(event.getLocation(), WorldGuardCompatibility.getEliteMobsDungeonFlag())) {
            EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteMobEntity != null)
                if (eliteMobEntity instanceof CustomBossEntity)
                    return;
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

}
