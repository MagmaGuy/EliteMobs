package com.magmaguy.elitemobs.powers.miscellaneouspowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MinorPower;
import com.magmaguy.elitemobs.utils.NonSolidBlockTypes;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class GroundPound extends MinorPower implements Listener {

    public GroundPound() {
        super(PowersConfig.getPower("ground_pound.yml"));
    }

    @EventHandler
    public void onEliteDamaged(EliteMobDamagedByPlayerEvent event) {
        GroundPound groundPound = (GroundPound) event.getEliteMobEntity().getPower(this);
        if (groundPound == null) return;
        if (groundPound.isCooldown()) return;

        if (ThreadLocalRandom.current().nextDouble() > 0.10) return;
        groundPound.doCooldown(20 * 10);

        doGroundPound(event.getEliteMobEntity());

    }


    public void doGroundPound(EliteMobEntity eliteMobEntity) {

        //step 1: make boss go up
        new BukkitRunnable() {
            @Override
            public void run() {
                eliteMobEntity.getLivingEntity().setVelocity(new Vector(0, 1.5, 0));
                cloudParticle(eliteMobEntity.getLivingEntity().getLocation());

            }
        }.runTaskLater(MetadataHandler.PLUGIN, 1);

        //step 2: make boss go down
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                if (!NonSolidBlockTypes.isPassthrough(eliteMobEntity.getLivingEntity().getLocation().clone().subtract(new Vector(0, 0.2, 0)).getBlock().getType())) {

                    eliteMobEntity.getLivingEntity().setVelocity(new Vector(0, -2, 0));
                    cloudParticle(eliteMobEntity.getLivingEntity().getLocation());

                    new BukkitRunnable() {
                        int counter = 0;

                        @Override
                        public void run() {
                            if (counter > 20 * 5 || !eliteMobEntity.getLivingEntity().isValid()) {
                                cancel();
                                return;
                            }

                            counter++;

                            if (!eliteMobEntity.getLivingEntity().isOnGround())
                                return;

                            cancel();

                            landCloudParticle(eliteMobEntity.getLivingEntity().getLocation());

                            for (Entity entity : eliteMobEntity.getLivingEntity().getNearbyEntities(10, 10, 10)) {
                                entity.setVelocity(entity.getLocation().clone().subtract(eliteMobEntity.getLivingEntity().getLocation()).toVector().normalize().multiply(2).setY(1.5));
                                if (entity instanceof LivingEntity) {
                                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 2));
                                }
                            }


                        }
                    }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

                    cancel();
                    return;

                }

                if (counter > 20 * 5)
                    cancel();

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 1);

    }

    private static void cloudParticle(Location location) {
        location.getWorld().spawnParticle(Particle.CLOUD, location, 10, 0.01, 0.01, 0.01, 0.7);
    }

    private static void landCloudParticle(Location location) {
        location.getWorld().spawnParticle(Particle.CLOUD, location, 20, 0.1, 0.01, 0.1, 0.7);
    }

}
