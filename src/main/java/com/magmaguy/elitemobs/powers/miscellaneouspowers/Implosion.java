package com.magmaguy.elitemobs.powers.miscellaneouspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class Implosion extends MinorPower implements Listener {

    public Implosion() {
        super(PowersConfig.getPower("implosion.yml"));
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
                        if (entity instanceof LivingEntity) {
                            EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(entity);
                            if (eliteMobEntity != null && eliteMobEntity instanceof CustomBossEntity) {
                                CustomBossEntity customBossEntity = (CustomBossEntity) eliteMobEntity;
                                if (customBossEntity.customBossConfigFields.getFrozen())
                                    continue;
                            }
                            entity.setVelocity(event.getEntity().getLocation().clone().subtract(entity.getLocation()).toVector().multiply(0.5));
                        }
                    cancel();
                }
                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 0);
    }

}
