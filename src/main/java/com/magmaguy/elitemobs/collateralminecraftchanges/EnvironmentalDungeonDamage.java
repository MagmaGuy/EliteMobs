package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EnvironmentalDungeonDamage implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!EliteMobsWorld.isEliteMobsWorld(event.getEntity().getWorld().getUID())) return;
        switch (event.getCause()) {
            case POISON -> event.setDamage(event.getDamage() * DungeonsConfig.getPoisonDamageMultiplier());
            case WITHER -> event.setDamage(event.getDamage() * DungeonsConfig.getWitherDamageMultiplier());
            case FIRE_TICK -> event.setDamage(event.getDamage() * DungeonsConfig.getFireDamageMultiplier());
        }
    }
}
