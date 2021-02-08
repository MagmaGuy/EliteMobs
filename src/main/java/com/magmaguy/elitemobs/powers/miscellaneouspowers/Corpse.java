package com.magmaguy.elitemobs.powers.miscellaneouspowers;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Corpse extends MinorPower implements Listener {

    public Corpse() {
        super(PowersConfig.getPower("corpse.yml"));
    }

    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        if (!event.getEntity().getLocation().getBlock().getType().equals(Material.AIR)) return;

        EntityTracker.addTemporaryBlock(event.getEliteMobEntity().getLivingEntity().getLocation().getBlock(), 20 * 60 * 2, Material.BONE_BLOCK);
    }

}
