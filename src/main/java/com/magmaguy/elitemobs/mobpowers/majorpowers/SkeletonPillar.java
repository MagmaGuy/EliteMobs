package com.magmaguy.elitemobs.mobpowers.majorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class SkeletonPillar extends MajorPowers implements Listener {

    String powerMetadata = MetadataHandler.SKELETON_PILLAR_MD;

    @Override
    public void applyPowers(Entity entity) {

        MetadataHandler.registerMetadata(entity, powerMetadata, true);
        MajorPowerPowerStance majorPowerStanceMath = new MajorPowerPowerStance();
        majorPowerStanceMath.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {
        return entity.hasMetadata(powerMetadata);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity().hasMetadata(powerMetadata) && event.getEntity() instanceof Skeleton)) return;
        if (event.getEntity().hasMetadata(MetadataHandler.SKELETON_PILLAR_COOLDOWN)) return;

        /*
        Run random check to see if the power should activate
         */
        if (ThreadLocalRandom.current().nextDouble() > 0.20) return;

        Skeleton eventSkeleton = (Skeleton) event.getEntity();
        eventSkeleton.setAI(false);
        PowerCooldown.startCooldownTimer(eventSkeleton, MetadataHandler.SKELETON_PILLAR_COOLDOWN, 20 * 27);

        ArmorStand armorStand1 = (ArmorStand) eventSkeleton.getWorld().spawnEntity(eventSkeleton.getLocation(), EntityType.ARMOR_STAND);
        armorStand1.setMarker(true);
        armorStand1.setVisible(false);
        MetadataHandler.registerMetadata(armorStand1, MetadataHandler.ARMOR_STAND_DISPLAY, true);
        armorStandMover(eventSkeleton, armorStand1, 20 * 1, 7);

        ArmorStand armorStand2 = (ArmorStand) eventSkeleton.getWorld().spawnEntity(eventSkeleton.getLocation(), EntityType.ARMOR_STAND);
        armorStand2.setMarker(true);
        armorStand2.setVisible(false);
        MetadataHandler.registerMetadata(armorStand2, MetadataHandler.ARMOR_STAND_DISPLAY, true);
        armorStandMover(eventSkeleton, armorStand2, 20 * 1, -7);

        new BukkitRunnable() {

            int timer = 1;


            @Override
            public void run() {

                if (timer > 20 * 7) {

                    armorStand1.remove();
                    armorStand2.remove();
                    eventSkeleton.setAI(true);
                    cancel();

                } else if (timer > 20 * 1 && timer < 20 * 7) {

                    pillarEffect(eventSkeleton, armorStand1, timer, 7);
                    pillarEffect(eventSkeleton, armorStand2, timer, -7);

                } else {

                    pillarWarningEffect(armorStand1);
                    pillarWarningEffect(armorStand2);

                }

                timer++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void pillarEffect(Skeleton eventSkeleton, ArmorStand armorStand, int timer, int offset) {

        armorStand.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, armorStand.getLocation(), 50, 0.1, 5, 0.1, 0.05);
        armorStandMover(eventSkeleton, armorStand, timer, offset);
        pillarDamage(armorStand);

    }

    private void pillarWarningEffect(ArmorStand armorStand) {

        armorStand.getWorld().spawnParticle(Particle.SMOKE_LARGE, armorStand.getLocation(), 10, 0.1, 5, 0.1, 0.05);

    }

    private void pillarDamage(ArmorStand armorStand) {

        for (Entity entity : armorStand.getNearbyEntities(2, 5, 2)) {

            if (entity instanceof LivingEntity && !(entity instanceof Pig || entity instanceof Cow || entity instanceof Chicken ||
                    entity instanceof Wolf || entity instanceof Llama || entity instanceof Ocelot || entity instanceof Horse ||
                    entity instanceof Sheep || entity instanceof Rabbit || entity instanceof Parrot || entity instanceof Villager)) {

                LivingEntity livingEntity = (LivingEntity) entity;

                if (livingEntity.isValid() && !livingEntity.isDead()) {

                    livingEntity.damage(1);

                }

            }

        }

    }

    private void armorStandMover(Skeleton entitySkeleton, ArmorStand armorStand, int timer, int offset) {

        int numberOfPointsPerRotation = 20 * 3;

        Location newLocation = GenericRotationMatrixMath.applyRotation(0, 1, 0, numberOfPointsPerRotation, 0, 0, offset, timer).toLocation(armorStand.getWorld());

        armorStand.teleport(newLocation.add(entitySkeleton.getLocation()));

    }

}
