package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EliteMobDamagedByEliteMobEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final EliteMobEntity damager;
    private final EliteMobEntity damagee;
    private boolean isCancelled = false;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    private final double damage;

    public EliteMobDamagedByEliteMobEvent(EliteMobEntity damager, EliteMobEntity damagee, EntityDamageByEntityEvent event, double damage) {
        this.damager = damager;
        this.damagee = damagee;
        this.entityDamageByEntityEvent = event;
        this.damage = damage;
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

    public static HandlerList getHandlerList() {
        return handlers;
    }


    public static class EliteMobDamagedByEliteMobFilter implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
            EliteMobEntity damagee = EntityTracker.getEliteMobEntity(event.getEntity());
            if (damagee == null) return;
            LivingEntity livingEntity = EntityFinder.filterRangedDamagers(event.getDamager());
            EliteMobEntity damager = EntityTracker.getEliteMobEntity(livingEntity);
            if (damager == null) return;
            double damage = EliteMobProperties.getPluginData(damager.getLivingEntity().getType()).baseDamage +
                    damager.getTier();
            EliteMobDamagedByEliteMobEvent eliteMobDamagedByEliteMobEvent = new EliteMobDamagedByEliteMobEvent(damager, damagee, event, damage);
            new EventCaller(eliteMobDamagedByEliteMobEvent);
            if (eliteMobDamagedByEliteMobEvent.isCancelled) return;
            event.setDamage(damage);
        }

    }


}
