package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EliteMobDamagedByEliteMobEvent extends EliteDamageEvent {

    private static final HandlerList handlers = new HandlerList();
    private final EliteEntity damager;
    private final EliteEntity damagee;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;

    /**
     * Event fired when an elite gets damaged by another elite.
     *
     * @param damager Damager in the event.
     * @param damagee Damaged entity in the event.
     * @param event   Original Minecraft damage event.
     * @param damage  Damage in the event. Can be modified!
     */
    public EliteMobDamagedByEliteMobEvent(EliteEntity damager, EliteEntity damagee, EntityDamageByEntityEvent event, double damage) {
        super(damage, event);
        this.damager = damager;
        this.damagee = damagee;
        this.entityDamageByEntityEvent = event;
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    //The thing that calls the event
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
            if (eliteMobDamagedByEliteMobEvent.isCancelled()) return;
            event.setDamage(damage);
            if (damagee instanceof RegionalBossEntity regionalBossEntity) regionalBossEntity.removeSlow();
        }
    }


}
