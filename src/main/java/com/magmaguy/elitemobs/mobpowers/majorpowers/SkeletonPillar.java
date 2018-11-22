package com.magmaguy.elitemobs.mobpowers.majorpowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class SkeletonPillar extends MajorPower implements Listener {

    private static HashSet<EliteMobEntity> cooldowns = new HashSet<>();

    private static boolean isInCooldown(EliteMobEntity eliteMobEntity) {
        return cooldowns.contains(eliteMobEntity);
    }

    public static HashSet<EliteMobEntity> getCooldowns(EliteMobEntity eliteMobEntity) {
        return cooldowns;
    }

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;
        if (!event.getEntity().getType().equals(EntityType.SKELETON)) return;

        /*
        Run random check to see if the power should activate
         */
        if (ThreadLocalRandom.current().nextDouble() > 0.20) return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;

        if (!eliteMobEntity.hasPower(this)) return;
        if (isInCooldown(eliteMobEntity)) return;

        eliteMobEntity.getLivingEntity().setAI(false);
        PowerCooldown.startCooldownTimer(eliteMobEntity, cooldowns, 20 * 27);

        Location location1 = locationMover(eliteMobEntity.getLivingEntity().getLocation().clone(), 20, 7);
        Location location2 = locationMover(eliteMobEntity.getLivingEntity().getLocation().clone(), 20, 7);

        new BukkitRunnable() {

            int timer = 1;

            @Override
            public void run() {

                if (timer > 20 * 7 || !eliteMobEntity.getLivingEntity().isValid()) {

                    eliteMobEntity.getLivingEntity().setAI(true);
                    cancel();

                } else if (timer > 20 * 1 && timer < 20 * 7) {

                    pillarEffect(location1, timer, 7);
                    pillarEffect(location2, timer, -7);

                } else {

                    pillarWarningEffect(location1);
                    pillarWarningEffect(location2);

                }

                timer++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void pillarEffect(Location location, int timer, int offset) {

        location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, 50, 0.1, 5, 0.1, 0.05);
        locationMover(location, timer, offset);
        pillarDamage(location);

    }

    private void pillarWarningEffect(Location location) {

        location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 10, 0.1, 5, 0.1, 0.05);

    }

    private void pillarDamage(Location location) {

        for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 5, 2))
            if (entity instanceof LivingEntity && !(entity instanceof Pig || entity instanceof Cow || entity instanceof Chicken ||
                    entity instanceof Wolf || entity instanceof Llama || entity instanceof Ocelot || entity instanceof Horse ||
                    entity instanceof Sheep || entity instanceof Rabbit || entity instanceof Parrot || entity instanceof Villager)) {

                LivingEntity livingEntity = (LivingEntity) entity;

                if (livingEntity.isValid())
                    livingEntity.damage(1);

            }

    }

    private Location locationMover(Location location, int timer, int offset) {

        int numberOfPointsPerRotation = 20 * 3;

        Location newLocation = GenericRotationMatrixMath.applyRotation(0, 1, 0, numberOfPointsPerRotation,
                0, 0, offset, timer).toLocation(location.getWorld());

        return newLocation.add(location);

    }

}
