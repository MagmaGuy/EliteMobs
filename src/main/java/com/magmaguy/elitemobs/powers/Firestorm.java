package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.BossPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class Firestorm extends BossPower implements Listener {

    public Firestorm() {
        super(PowersConfig.getPower("firestorm.yml"));
    }

    private static void doFirestorm(EliteEntity eliteEntity) {
        if (eliteEntity == null || !eliteEntity.isValid()) return;
        eliteEntity.getLivingEntity().setAI(false);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;

                if (counter > 20 * 10 || !eliteEntity.isValid()) {
                    if (eliteEntity.isValid())
                        eliteEntity.getLivingEntity().setAI(true);
                    cancel();
                    return;
                }

                if (counter % 5 == 0) {
                    Location randomLocation = eliteEntity.getLivingEntity().getLocation().add(new Vector(
                            ThreadLocalRandom.current().nextInt(-20, 20),
                            0,
                            ThreadLocalRandom.current().nextInt(-20, 20)
                    ));
                    doFlamePyrePhase1(randomLocation, eliteEntity);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    /**
     * Warning phase
     */
    private static void doFlamePyrePhase1(Location location, EliteEntity eliteEntity) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                spawnPhase1Particle(location, Particle.SMOKE_NORMAL);
                if (counter < 20 * 2) return;
                cancel();
                doFlamePyrePhase2(location, eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static void spawnPhase1Particle(Location location, Particle particle) {
        for (int i = 0; i < 10; i++) {
            location.getWorld().spawnParticle(particle, new Location(
                    location.getWorld(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.5 + location.getX(),
                    location.getY(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.5 + location.getZ()
            ), 0, 0, 1, 0, ThreadLocalRandom.current().nextDouble() * 2);
        }
    }

    /**
     * First damage phase
     */
    private static void doFlamePyrePhase2(Location location, EliteEntity eliteEntity) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                spawnPhase1Particle(location, Particle.FLAME);
                doDamage(location, eliteEntity, 0.5, 50, 0.5);
                spawnPhase2Particle(location, Particle.SMOKE_NORMAL);
                if (counter < 20 * 2) return;
                cancel();
                doFlamePyrePhase3(location, eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static void spawnPhase2Particle(Location location, Particle particle) {
        for (int i = 0; i < 10; i++) {
            location.getWorld().spawnParticle(particle, new Location(
                    location.getWorld(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 3 + location.getX(),
                    location.getY(),
                    (ThreadLocalRandom.current().nextDouble() - 0.5) * 3 + location.getZ()
            ), 0, 0, 1, 0, ThreadLocalRandom.current().nextDouble() * 2);
        }
    }

    private static void doDamage(Location location, EliteEntity eliteEntity, double range1, double range2, double range3) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, range1, range2, range3))
            if (entity instanceof LivingEntity)
                BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), (LivingEntity) entity, 1);
    }

    /**
     * Second damage phase / last warning phase
     */
    private static void doFlamePyrePhase3(Location location, EliteEntity eliteEntity) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                spawnPhase2Particle(location, Particle.FLAME);
                doDamage(location, eliteEntity, 3, 50, 3);
                spawnPhase3Particle(location, Particle.SMOKE_NORMAL);
                if (counter < 20 * 2) return;
                cancel();
                doFlamePyrePhase4(location, eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static void spawnPhase3Particle(Location location, Particle particle) {
        location.getWorld().spawnParticle(particle, location, 50, 0.01, 0.01, 0.01, 0.1);
    }

    /**
     * Final/full damage phase
     *
     * @param eliteEntity
     */
    private static void doFlamePyrePhase4(Location location, EliteEntity eliteEntity) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                spawnPhase3Particle(location, Particle.FLAME);
                doDamage(location, eliteEntity, 5, 50, 5);
                if (counter < 20 * 2) return;
                cancel();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    @EventHandler
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        Firestorm firestorm = (Firestorm) event.getEliteMobEntity().getPower(this);
        if (firestorm == null) return;
        if (!eventIsValid(event, firestorm)) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.25) return;

        firestorm.doGlobalCooldown(20 * 20, event.getEliteMobEntity());
        doFirestorm(event.getEliteMobEntity());

    }

}
