package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.utils.EnderDragonPhaseSimplifier;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnderDragonTornado extends CombatEnterScanPower {

    private Location tornadoEye = null;
    private Vector tornadoSpeed = null;

    public EnderDragonTornado() {
        super(PowersConfig.getPower("ender_dragon_tornado.yml"));
    }

    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        super.bukkitTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                    return;
                }

                if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)) {
                    EnderDragon.Phase phase = ((EnderDragon) eliteEntity.getLivingEntity()).getPhase();
                    if (!EnderDragonPhaseSimplifier.isLanded(phase)) return;
                }

                doPower(eliteEntity);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 10);
    }

    private void doPower(EliteEntity eliteEntity) {
        doCooldown(eliteEntity);

        tornadoEye = eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(6, -6, 0))
                .toVector().rotateAroundY(ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI))
                .toLocation(eliteEntity.getLivingEntity().getWorld());

        tornadoSpeed = tornadoEye.clone().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().setY(0).normalize().multiply(0.2);
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                if (!eliteEntity.isValid()) {
                    cancel();
                    return;
                }

                if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
                    ((EnderDragon) eliteEntity.getLivingEntity()).setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);

                if (doExit(eliteEntity) || eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON) && !EnderDragonPhaseSimplifier.isLanded(((EnderDragon) eliteEntity.getLivingEntity()).getPhase())) {
                    cancel();
                    return;
                }

                tornadoEye.add(tornadoSpeed);

                if (counter % 2 == 0) {
                    doTornadoParticles();
                    doTerrainDestruction(eliteEntity);
                    doEntityDisplacement(eliteEntity.getLivingEntity());
                }

                if (counter > 20 * 6) {
                    cancel();
                }

                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void doTornadoParticles() {
        for (int i = 0; i < 21; i++)
            for (int x = 0; x < (i ^ 2 + 1); x++) {
                Location randomParticleLocation = tornadoEye.clone().add((new Vector(i / 2, i, 0))
                        .rotateAroundY(ThreadLocalRandom.current().nextDouble(2 * Math.PI)));
                randomParticleLocation.setDirection(randomParticleLocation.toVector().rotateAroundY(0.5 * Math.PI));
                if (ThreadLocalRandom.current().nextDouble() < 0.7)
                    tornadoEye.getWorld().spawnParticle(Particle.SMOKE_LARGE, randomParticleLocation, 1, 0, 0, 0, 0.05);
                else
                    tornadoEye.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, randomParticleLocation, 1, 0, 0, 0, 0.05);
            }
    }

    private void doTerrainDestruction(EliteEntity eliteEntity) {
        List<Block> blockList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Location location = tornadoEye.clone().add(new Vector(
                    ThreadLocalRandom.current().nextInt(-5, 5),
                    0,
                    ThreadLocalRandom.current().nextInt(-5, 5)));
            Block block = location.getWorld().getHighestBlockAt(location);
            if (block.getType().getBlastResistance() > 7) continue;
            if (block.getLocation().getY() != -1)
                blockList.add(block);
        }
        Explosion.generateFakeExplosion(blockList, eliteEntity.getLivingEntity(), (PowersConfigFields)getPowersConfigFields(), tornadoEye.clone());
    }

    private void doEntityDisplacement(LivingEntity sourceEntity) {
        for (Entity entity : tornadoEye.getWorld().getNearbyEntities(tornadoEye, 7, 21, 7))
            if (entity instanceof LivingEntity) {
                if (entity.equals(sourceEntity)) continue;
                Vector vector = entity.getLocation().clone().subtract(tornadoEye).toVector().rotateAroundY(0.5 * Math.PI)
                        .toLocation(entity.getWorld()).subtract(entity.getLocation().clone().subtract(tornadoEye).toVector()).toVector().normalize().multiply(0.5);
                vector.setY(0.5);
                entity.setVelocity(entity.getVelocity().add(vector));
            }
    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {

    }

}
