package com.magmaguy.elitemobs.powers.majorpowers.skeleton;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MajorPower;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class SkeletonPillar extends MajorPower implements Listener {

    public SkeletonPillar() {
        super(PowersConfig.getPower("skeleton_pillar.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        SkeletonPillar skeletonPillar = (SkeletonPillar) event.getEliteMobEntity().getPower(this);
        if (skeletonPillar == null) return;
        if (skeletonPillar.isCooldown()) return;

        /*
        Run random check to see if the power should activate
         */
        if (ThreadLocalRandom.current().nextDouble() > 0.20) return;
        skeletonPillar.doCooldown(20 * 27);

        event.getEliteMobEntity().getLivingEntity().setAI(false);

        Location location1 = event.getEliteMobEntity().getLivingEntity().getLocation().clone()
                .add(locationMover(event.getEliteMobEntity().getLivingEntity().getLocation().clone(), 20, 7));
        Location location2 = event.getEliteMobEntity().getLivingEntity().getLocation().clone()
                .add(locationMover(event.getEliteMobEntity().getLivingEntity().getLocation().clone(), 20, -7));

        new BukkitRunnable() {

            int timer = 1;

            @Override
            public void run() {

                if (timer > 20 * 7 || !event.getEliteMobEntity().getLivingEntity().isValid()) {

                    event.getEliteMobEntity().getLivingEntity().setAI(true);
                    cancel();

                } else if (timer > 20 * 1 && timer < 20 * 7) {

                    pillarEffect(event.getEliteMobEntity().getLivingEntity().getLocation().clone(), timer, 7);
                    pillarEffect(event.getEliteMobEntity().getLivingEntity().getLocation().clone(), timer, -7);

                } else {

                    pillarWarningEffect(location1);
                    pillarWarningEffect(location2);

                }

                timer++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void pillarEffect(Location location, int timer, int offset) {
        location.add(locationMover(location, timer, offset));
        location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, 15, 0.1, 5, 0.1, 0.05);
        pillarDamage(location);
    }

    private void pillarWarningEffect(Location location) {
        location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 5, 0.1, 5, 0.1, 0.05);
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
        return newLocation;
    }

}
