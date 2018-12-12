package com.magmaguy.elitemobs.mobpowers.miscellaneouspowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Implosion extends MinorPower implements Listener {

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        EliteMobEntity eliteMob = EntityTracker.getEliteMobEntity(event.getEntity());

        if (eliteMob == null) return;

        if (!eliteMob.hasPower(this)) return;

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
