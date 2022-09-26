package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.utils.EnderDragonPhaseSimplifier;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class EnderDragonDiscoFireballs extends CombatEnterScanPower {

    int randomTiltSeed;
    private ArrayList<Vector> relativeLocationOffsets;
    private ArrayList<Location> realLocations = new ArrayList<>();
    private int warningCounter = 0;
    private ArrayList<Fireball> fireballs = new ArrayList<>();

    public EnderDragonDiscoFireballs() {
        super(PowersConfig.getPower("ender_dragon_disco_fireballs.yml"));
    }

    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        super.bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                    return;
                }

                if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
                    if (!EnderDragonPhaseSimplifier.isLanded(((EnderDragon) eliteEntity.getLivingEntity()).getPhase()))
                        return;

                doPower(eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 10);
    }

    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {

    }

    private void doPower(EliteEntity eliteEntity) {
        doCooldown(eliteEntity);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                if (doExit(eliteEntity) ||
                        eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON) &&
                                !EnderDragonPhaseSimplifier.isLanded(((EnderDragon) eliteEntity.getLivingEntity()).getPhase())) {
                    cancel();
                    return;
                }

                if (eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
                    ((EnderDragon) eliteEntity.getLivingEntity()).setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);

                if (counter == 0) {
                    //todo: reset fields if needed
                    generateLocations();
                    commitLocations(eliteEntity);
                    randomTiltSeed = ThreadLocalRandom.current().nextInt();
                    warningCounter = 0;
                }

                if (counter < 20 * 6) {
                    //todo: warning phase
                    doWarningPhase(eliteEntity);
                    warningCounter++;
                }

                if (counter > 20 * 6) {
                    doDamagePhase(eliteEntity);
                    cancel();
                }

                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void generateLocations() {
        relativeLocationOffsets = new ArrayList<>();
        for (int i = 0; i < 12; i++)
            relativeLocationOffsets.add(new Vector(7, 0, 0).rotateAroundY(2D * Math.PI / 12D * i));
    }

    private void commitLocations(EliteEntity eliteEntity) {
        realLocations = new ArrayList<>();
        for (Vector vector : relativeLocationOffsets)
            realLocations.add(eliteEntity.getLivingEntity().getLocation().clone().add(vector));
    }

    private void doWarningPhase(EliteEntity eliteEntity) {
        //spawn fireballs and start rotation
        if (warningCounter == 0) {
            fireballs = new ArrayList<>();
            for (Location location : realLocations) {
                Fireball fireball = (Fireball) location.getWorld().spawnEntity(location, EntityType.FIREBALL);
                EntityTracker.registerProjectileEntity(fireball);
                EliteProjectile.signExplosiveWithPower(fireball, getFileName());
                fireballs.add(fireball);
            }
        }

        warningCounter++;

        for (Fireball fireball : fireballs) {
            if (fireball.isValid()) {

                if (warningCounter % 5 == 0) {
                    Vector relativeLocation = fireball.getLocation().clone().subtract(eliteEntity.getLivingEntity().getLocation())
                            .toVector().rotateAroundY(2D * Math.PI / 96D);
                    Location rotatedLocation = eliteEntity.getLivingEntity().getLocation().clone().add(relativeLocation);
                    Vector velocity = rotatedLocation.clone().subtract(fireball.getLocation()).toVector().multiply(0.001);
                    fireball.setDirection(velocity);
                    fireball.setVelocity(velocity);
                    fireball.setYield(5F);
                }

                generateVisualParticles(eliteEntity, fireball);
            }
        }

    }

    private void doDamagePhase(EliteEntity eliteEntity) {
        for (Fireball fireball : fireballs)
            fireball.setDirection(generateDownwardsVector(eliteEntity, fireball).multiply(0.1));
    }

    private Vector generateDownwardsVector(EliteEntity eliteEntity, Fireball fireball) {
        double yValue = Math.cos(warningCounter) / 2 - 0.5;
        return fireball.getLocation().clone().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().normalize().setY(yValue);
    }

    private void generateVisualParticles(EliteEntity eliteEntity, Fireball fireball) {
        Vector downwardsVector = generateDownwardsVector(eliteEntity, fireball);
        Location particleLocation = fireball.getLocation().clone();
        for (int i = 0; i < 200; i++) {
            particleLocation.add(downwardsVector.clone().multiply(0.3));
            if (!particleLocation.getBlock().isPassable()) break;
            fireball.getWorld().spawnParticle(Particle.SMOKE_NORMAL, particleLocation, 1, 1, 0, 0, 0);
        }
    }

}
