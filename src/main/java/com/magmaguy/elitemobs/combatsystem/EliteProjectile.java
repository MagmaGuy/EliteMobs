package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class EliteProjectile {
    public static Projectile create(EntityType entityType, Entity shooter, Entity victim, Vector shotVector, boolean gravity) {
        Location projectileLocation = generateLocation(shooter, victim);
        Projectile projectile = (Projectile) projectileLocation.getWorld().spawnEntity(projectileLocation, entityType);
        projectile.setShooter((ProjectileSource) shooter);
        projectile.setVelocity(shotVector);
        projectile.setGravity(gravity);
        EntityTracker.registerProjectileEntity(projectile);
        return projectile;
    }

    public static Projectile create(EntityType entityType, Entity shooter, Player victim, boolean gravity) {
        Vector targetterToTargetted = victim.getEyeLocation().clone().subtract(victim.getLocation()).toVector().normalize().multiply(2);
        return create(entityType, shooter, victim, targetterToTargetted, gravity);
    }

    public static Projectile create(EntityType entityType, Entity shooter, Vector targetterToTargetted, boolean gravity) {
        Location projectileLocation = generateLocation(shooter, targetterToTargetted);
        Projectile projectile = (Projectile) projectileLocation.getWorld().spawnEntity(projectileLocation, entityType);
        projectile.setShooter((ProjectileSource) shooter);
        projectile.setVelocity(targetterToTargetted);
        projectile.setGravity(gravity);
        EntityTracker.registerProjectileEntity(projectile);
        return projectile;
    }

    private static Location generateLocation(Entity shooter, Entity victim) {
        Vector vector = victim.getLocation().clone().subtract(shooter.getLocation().clone()).toVector().normalize();
        return shooter.getLocation().clone().add(0, 1, 0).add(vector).setDirection(vector);
    }

    private static Location generateLocation(Entity shooter, Vector targetterToTargetted) {
        return shooter.getLocation().clone().add(targetterToTargetted).setDirection(targetterToTargetted).add(new Vector(0, 1, 0));
    }

    public static void signExplosiveWithPower(Projectile projectile, String powerName) {
        projectile.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, "detonationPower"), PersistentDataType.STRING, powerName);
    }

    public static String readExplosivePower(Projectile projectile) {
        return projectile.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, "detonationPower"), PersistentDataType.STRING);
    }

}
