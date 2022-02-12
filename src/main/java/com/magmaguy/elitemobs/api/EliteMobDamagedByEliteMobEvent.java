package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EliteMobDamagedByEliteMobEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final EliteEntity damager;
    private final EliteEntity damagee;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    private final double damage;
    private boolean isCancelled = false;

    public EliteMobDamagedByEliteMobEvent(EliteEntity damager, EliteEntity damagee, EntityDamageByEntityEvent event, double damage) {
        this.damager = damager;
        this.damagee = damagee;
        this.entityDamageByEntityEvent = event;
        this.damage = damage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public EliteEntity getDamager() {
        return damager;
    }

    public EliteEntity getDamagee() {
        return damagee;
    }

    public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
        return entityDamageByEntityEvent;
    }

    public double getDamage() {
        return damage;
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

    public static class EliteMobDamagedByEliteMobFilter implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
            EliteEntity damagee = EntityTracker.getEliteMobEntity(event.getEntity());
            if (damagee == null) return;
            LivingEntity livingEntity = EntityFinder.filterRangedDamagers(event.getDamager());
            EliteEntity damager = EntityTracker.getEliteMobEntity(livingEntity);
            if (damager == null) return;
            if (damager.getLivingEntity() == null) return;
            if (EliteMobProperties.getPluginData(damager.getLivingEntity().getType()) == null) return;
            double damage = EliteMobProperties.getBaselineDamage(damager.getLivingEntity().getType(), damager) + damager.getLevel();
            EliteMobDamagedByEliteMobEvent eliteMobDamagedByEliteMobEvent = new EliteMobDamagedByEliteMobEvent(damager, damagee, event, damage);
            new EventCaller(eliteMobDamagedByEliteMobEvent);
            if (eliteMobDamagedByEliteMobEvent.isCancelled) return;
            event.setDamage(damage);
        }

    }


}
