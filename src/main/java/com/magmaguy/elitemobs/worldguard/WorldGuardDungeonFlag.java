package com.magmaguy.elitemobs.worldguard;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WorldGuardDungeonFlag implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!EliteMobs.worldguardIsEnabled) return;
        if (event.getEntity().getType().equals(EntityType.ARMOR_STAND)) return;
        if (WorldGuardFlagChecker.checkFlag(event.getLocation(), (StateFlag) WorldGuardCompatibility.getEliteMobsDungeonFlag())) {
            EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteMobEntity != null)
                if (eliteMobEntity instanceof CustomBossEntity)
                    return;
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

}
