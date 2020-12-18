package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EliteMobDamagedByEliteMobEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final EliteMobEntity damager;
    private final EliteMobEntity damagee;
    private boolean isCancelled = false;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;

    public EliteMobDamagedByEliteMobEvent(EliteMobEntity damager, EliteMobEntity damagee, EntityDamageByEntityEvent event) {
        this.damager = damager;
        this.damagee = damagee;
        this.entityDamageByEntityEvent = event;
    }

    public EliteMobEntity getDamager() {
        return damager;
    }

    public EliteMobEntity getDamagee() {
        return damagee;
    }

    public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
        return entityDamageByEntityEvent;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


    public static class EliteMobDamagedByEliteMobFilter implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

            if (event.isCancelled()) return;
            EliteMobEntity damagee = EntityTracker.getEliteMobEntity(event.getEntity());
            if (damagee == null) return;
            EliteMobEntity damager = null;
            if (event.getDamager() instanceof LivingEntity)
                damager = EntityTracker.getEliteMobEntity(event.getDamager());
            if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity)
                damager = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getDamager()).getShooter());
            if (damager == null) return;

            Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByEliteMobEvent(damager, damagee, event));

        }

    }


}
