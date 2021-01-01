package com.magmaguy.elitemobs.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityFinder {

    public static LivingEntity filterRangedDamagers(Entity entity) {
        if (entity instanceof LivingEntity)
            return (LivingEntity) entity;
        else if (entity instanceof Projectile && ((Projectile) entity).getShooter() instanceof LivingEntity)
            return (LivingEntity) ((Projectile) entity).getShooter();
        return null;
    }

    public static LivingEntity getRealDamager(EntityDamageByEntityEvent event) {
        return filterRangedDamagers(event.getDamager());
    }

    public static LivingEntity getRealDamager(EntityTargetLivingEntityEvent event) {
        return filterRangedDamagers(event.getEntity());
    }

    private static Player playerFilter(Entity entity) {
        return (entity instanceof Player) ? (Player) entity : null;
    }

    public static Player findPlayer(EntityDamageByEntityEvent event) {
        return playerFilter(event.getEntity());
    }

    public static Player findPlayer(EntityTargetLivingEntityEvent event) {
        return playerFilter(event.getTarget());
    }

}
