package com.magmaguy.elitemobs.powers.majorpowers.enderdragon;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
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

public class EnderDragonDiscoFireballs extends MajorCombatEnterScanningPower {

    public EnderDragonDiscoFireballs() {
        super(PowersConfig.getPower("ender_dragon_disco_fireballs.yml"));
    }

    @Override
    protected void finishActivation(EliteMobEntity eliteMobEntity) {
        super.bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (doExit(eliteMobEntity) || isInCooldown(eliteMobEntity)) {
                    return;
                }

                if (eliteMobEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
                    if (!EnderDragonPhaseSimplifier.isLanded(((EnderDragon) eliteMobEntity.getLivingEntity()).getPhase()))
                        return;

                doPower(eliteMobEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 10);
    }

    @Override
    protected void finishDeactivation(EliteMobEntity eliteMobEntity) {

    }

    private void doPower(EliteMobEntity eliteMobEntity) {
        doCooldown(eliteMobEntity);

        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {

                if (eliteMobEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON))
                    ((EnderDragon) eliteMobEntity.getLivingEntity()).setPhase(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET);

                if (doExit(eliteMobEntity) || !EnderDragonPhaseSimplifier.isLanded(((EnderDragon) eliteMobEntity.getLivingEntity()).getPhase())) {
                    cancel();
                    return;
                }

                if (counter == 0) {
                    //todo: reset fields if needed
                    generateLocations();
                    commitLocations(eliteMobEntity);
                    randomTiltSeed = ThreadLocalRandom.current().nextInt();
                    warningCounter = 0;
                }

                if (counter < 20 * 6) {
                    //todo: warning phase
                    doWarningPhase(eliteMobEntity);
                    warningCounter++;
                }

                if (counter > 20 * 6) {
                    doDamagePhase(eliteMobEntity);
                    cancel();
                }

                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private ArrayList<Vector> relativeLocationOffsets;

    private void generateLocations() {
        relativeLocationOffsets = new ArrayList<>();
        for (int i = 0; i < 12; i++)
            relativeLocationOffsets.add(new Vector(7, 0, 0).rotateAroundY(2D * Math.PI / 12D * i));
    }

    private ArrayList<Location> realLocations = new ArrayList<>();

    private void commitLocations(EliteMobEntity eliteMobEntity) {
        realLocations = new ArrayList<>();
        for (Vector vector : relativeLocationOffsets)
            realLocations.add(eliteMobEntity.getLivingEntity().getLocation().clone().add(vector));
    }

    private int warningCounter = 0;

    private ArrayList<Fireball> fireballs = new ArrayList<>();

    private void doWarningPhase(EliteMobEntity eliteMobEntity) {
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
                    Vector relativeLocation = fireball.getLocation().clone().subtract(eliteMobEntity.getLivingEntity().getLocation())
                            .toVector().rotateAroundY(2D * Math.PI / 96D);
                    Location rotatedLocation = eliteMobEntity.getLivingEntity().getLocation().clone().add(relativeLocation);
                    Vector velocity = rotatedLocation.clone().subtract(fireball.getLocation()).toVector().multiply(0.001);
                    fireball.setDirection(velocity);
                    fireball.setVelocity(velocity);
                    fireball.setYield(5F);
                }

                generateVisualParticles(eliteMobEntity, fireball);
            }
        }

    }

    private void doDamagePhase(EliteMobEntity eliteMobEntity) {
        for (Fireball fireball : fireballs)
            fireball.setDirection(generateDownwardsVector(eliteMobEntity, fireball).multiply(0.1));
    }

    int randomTiltSeed;

    private Vector generateDownwardsVector(EliteMobEntity eliteMobEntity, Fireball fireball) {
        double yValue = Math.cos(warningCounter) / 2 - 0.5;
        return fireball.getLocation().clone().subtract(eliteMobEntity.getLivingEntity().getLocation()).toVector().normalize().setY(yValue);
    }

    private void generateVisualParticles(EliteMobEntity eliteMobEntity, Fireball fireball) {
        Vector downwardsVector = generateDownwardsVector(eliteMobEntity, fireball);
        Location particleLocation = fireball.getLocation().clone();
        for (int i = 0; i < 200; i++) {
            particleLocation.add(downwardsVector.clone().multiply(0.3));
            if (!particleLocation.getBlock().isPassable()) break;
            fireball.getWorld().spawnParticle(Particle.SMOKE_NORMAL, particleLocation, 1, 1, 0, 0, 0);
        }
    }

}
