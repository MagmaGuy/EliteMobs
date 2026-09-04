package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.function.Predicate;

/** Resolves the player involved in damage against a qualifying combat enemy. */
final class CombatParticipantResolver {

    private CombatParticipantResolver() {
    }

    static Player resolveEliteCombatPlayer(EntityDamageByEntityEvent event) {
        return resolveEliteCombatPlayer(
                event.getEntity(), event.getDamager(), EntityTracker::isEliteMob);
    }

    static Player resolveEnemyCombatPlayer(EntityDamageByEntityEvent event) {
        return resolveEnemyCombatPlayer(
                event.getEntity(), event.getDamager(), EntityTracker::isEliteMob);
    }

    static Player resolveEliteCombatPlayer(
            Entity damaged,
            Entity damager,
            Predicate<Entity> isEliteMob) {
        return resolve(damaged, damager, isEliteMob);
    }

    static Player resolveEnemyCombatPlayer(
            Entity damaged,
            Entity damager,
            Predicate<Entity> isEliteMob) {
        return resolve(damaged, damager,
                entity -> entity instanceof Enemy || isEliteMob.test(entity));
    }

    private static Player resolve(Entity damaged, Entity damager, Predicate<Entity> isEnemy) {
        if (damager instanceof Player player && isEnemy.test(damaged))
            return player;
        if (damaged instanceof Player player &&
                (isEnemy.test(damager) ||
                        damager instanceof Projectile projectile &&
                                projectile.getShooter() instanceof LivingEntity shooter &&
                                isEnemy.test(shooter)))
            return player;
        if (damaged instanceof Player player &&
                damager instanceof EvokerFangs evokerFangs &&
                evokerFangs.getOwner() != null &&
                isEnemy.test(evokerFangs.getOwner()))
            return player;
        if (damager instanceof Projectile projectile &&
                projectile.getShooter() instanceof Player player &&
                isEnemy.test(damaged))
            return player;

        return null;
    }
}
