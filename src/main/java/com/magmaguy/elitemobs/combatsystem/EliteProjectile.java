package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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

    private static Location generateLocation(Entity shooter, Entity victim) {
        Vector vector = victim.getLocation().clone().subtract(shooter.getLocation().clone()).toVector().normalize();
        return shooter.getLocation().clone().add(0, 1, 0).add(vector).setDirection(vector);
    }
}
