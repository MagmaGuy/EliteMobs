package com.magmaguy.elitemobs.mobpowers.miscellaneouspowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Corpse extends MinorPower implements Listener {

    public Corpse() {
        super("Corpse", Material.BONE);
    }

    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;
        if (!event.getEntity().getLocation().getBlock().getType().equals(Material.AIR)) return;

        EntityTracker.addTemporaryBlock(event.getEliteMobEntity().getLivingEntity().getLocation().getBlock(), 20 * 60 * 2, Material.BONE_BLOCK);
    }

}
