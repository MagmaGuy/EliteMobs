package com.magmaguy.elitemobs.mobpowers.miscellaneouspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobpowers.MinorPower;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class Implosion extends MinorPower implements Listener {

    public Implosion() {
        super("Implosion", Material.SLIME_BALL);
    }

    @EventHandler
    public void onDeath(EliteMobDeathEvent event) {
        if (!event.getEliteMobEntity().hasPower(this)) return;

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter < 20)
                    for (int i = 0; i < 20; i++)
                        event.getEntity().getLocation().getWorld().spawnParticle(Particle.PORTAL, event.getEntity().getLocation(), 1, 0.1, 0.1, 0.1, 1);
                if (counter > 20 * 3) {
                    for (Entity entity : event.getEntity().getWorld().getNearbyEntities(event.getEntity().getLocation(), 10, 10, 10))
                        if (entity instanceof LivingEntity)
                            entity.setVelocity(event.getEntity().getLocation().clone().subtract(entity.getLocation()).toVector().multiply(0.5));
                    cancel();
                }
                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 0);
    }

}
