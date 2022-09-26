package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PhotonRay extends CombatEnterScanPower {

    private final int range = 60;
    private List<Location> playerLocations = new ArrayList<>(5);
    private int tickCounter = 0;

    public PhotonRay() {
        super(PowersConfig.getPower("photon_ray.yml"));
    }

    private void doDamage(Location location, EliteEntity eliteEntity) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5))
            if (entity instanceof LivingEntity) {
                if (eliteEntity.getLivingEntity().equals(entity)) continue;
                BossCustomAttackDamage.dealCustomDamage(eliteEntity.getLivingEntity(), (LivingEntity) entity, 1);
            }

        tickCounter++;

    }

    @Override
    protected void finishActivation(EliteEntity eliteEntity) {
        super.bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (doExit(eliteEntity) || isInCooldown(eliteEntity)) {
                    return;
                }
                doPower(eliteEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void doPower(EliteEntity eliteEntity) {
        doCooldown(eliteEntity);
        for (Entity entity : eliteEntity.getLivingEntity().getNearbyEntities(60, 60, 60))
            if (entity.getType().equals(EntityType.PLAYER)) {
                if (((Player) entity).getGameMode().equals(GameMode.SPECTATOR)) continue;
                //Vector shotVector = entity.getLocation().subtract(eliteEntity.getLivingEntity().getLocation()).toVector().normalize().multiply(0.5);
                createRay((Player) entity, eliteEntity.getLocation(), eliteEntity);
                break;
            }
    }

    private void createRay(Player target, Location sourceLocation, EliteEntity sourceEntity) {
        sourceEntity.getLivingEntity().setAI(false);

        new BukkitRunnable() {
            int counter = 0;
            Vector laserVector = generateRayVector(sourceLocation, target.getLocation());

            @Override
            public void run() {
                if (counter > 30 ||
                        !target.isValid() ||
                        !target.getLocation().getWorld().equals(sourceLocation.getWorld()) ||
                        target.getLocation().distanceSquared(sourceLocation) > range * range ||
                        !sourceEntity.isValid()) {
                    if (sourceEntity.getLivingEntity() != null)
                        sourceEntity.getLivingEntity().setAI(true);
                    cancel();
                    return;
                }

                laserVector = dragTarget(laserVector, sourceEntity.getLocation(), target.getLocation());

                doRaytraceLaser(laserVector, sourceEntity.getLocation(), counter < 20 / 4d, sourceEntity);

                counter++;

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 2);
    }

    private void doRaytraceLaser(Vector laserVector, Location source, boolean warningPhase, EliteEntity eliteEntity) {
        Location cloneLocation = source.clone().add(new Vector(0, 1, 0));
        for (int i = 0; i < range * 2; i++) {
            if (!cloneLocation.clone().add(laserVector).getBlock().isPassable()) {
                Vector tentativeDistance = cloneLocation.clone().add(laserVector).getBlock().getLocation().add(new Vector(0.5, 0.5, 0.5))
                        .subtract(cloneLocation.clone()).toVector();
                double x = laserVector.getX(), y = laserVector.getY(), z = laserVector.getZ();
                double xAbs = Math.abs(tentativeDistance.getX()), yAbs = Math.abs(tentativeDistance.getY()), zAbs = Math.abs(tentativeDistance.getZ());
                if (xAbs > yAbs && xAbs > zAbs)
                    x *= -1;
                else if (yAbs > xAbs && yAbs > zAbs)
                    y *= -1;
                else if (zAbs > yAbs && zAbs > xAbs)
                    z *= -1;
                else new WarningMessage("MagmaGuy is bad at math!");
                laserVector.setX(x);
                laserVector.setY(y);
                laserVector.setZ(z);
            }
            if (warningPhase)
                doWarningParticle(cloneLocation.add(laserVector));
            else
                doDamageParticles(eliteEntity, cloneLocation.add(laserVector));
        }
    }

    private Vector generateRayVector(Location source, Location target) {
        return target.clone().add(new Vector(0, 1, 0))
                .subtract(source.clone().add(new Vector(0, 1, 0))).toVector().normalize().multiply(.5);
    }

    private Vector dragTarget(Vector originalVector, Location sourceLocation, Location targetLocation) {
        if (playerLocations.size() < 5) {
            playerLocations.add(targetLocation);
            return originalVector;
        }

        Location oldTarget = playerLocations.get(0);
        Vector newVector = generateRayVector(sourceLocation, oldTarget);

        playerLocations.remove(0);
        List<Location> newLocation = new ArrayList<>(playerLocations);
        newLocation.add(targetLocation);
        playerLocations = newLocation;


        return newVector;
    }

    private void doWarningParticle(Location location) {
        location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(),
                5, 0.2, 0.2, 0.2,
                1, new Particle.DustOptions(Color.BLACK, 1));
    }

    private void doDamageParticles(EliteEntity eliteEntity, Location location) {
        location.getWorld().spawnParticle(Particle.REDSTONE, location.getX(), location.getY(), location.getZ(),
                5, 0.2, 0.2, 0.2,
                1, new Particle.DustOptions(Color.fromRGB(
                        ThreadLocalRandom.current().nextInt(0, 100),
                        ThreadLocalRandom.current().nextInt(0, 100),
                        ThreadLocalRandom.current().nextInt(100, 255)
                ), 1));
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.25, 0.25, 0.25))
            if (entity.getType() == EntityType.PLAYER)
                doDamage(location, eliteEntity);


    }


    @Override
    protected void finishDeactivation(EliteEntity eliteEntity) {

    }
}
