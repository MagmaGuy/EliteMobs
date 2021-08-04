package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobDamagedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteEntity eliteEntity;
    private final EntityDamageEvent entityDamageEvent;
    private boolean isCancelled = false;

    public EliteMobDamagedEvent(EliteEntity eliteEntity, EntityDamageEvent event) {
        this.entity = eliteEntity.getLivingEntity();
        this.eliteEntity = eliteEntity;
        this.entityDamageEvent = event;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getEntity() {
        return entity;
    }

    public EliteEntity getEliteMobEntity() {
        return eliteEntity;
    }

    public EntityDamageEvent getEntityDamageEvent() {
        return entityDamageEvent;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
        entityDamageEvent.setCancelled(b);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class EliteMobDamageEventFilter implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onEntityDamageByEntityEvent(EntityDamageEvent event) {

            if (event.isCancelled()) return;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteEntity == null) return;

            EliteMobDamagedEvent eliteMobDamagedEvent = new EliteMobDamagedEvent(eliteEntity, event);
            new EventCaller(eliteMobDamagedEvent);
            if (eliteMobDamagedEvent.isCancelled) {
                eliteMobDamagedEvent.setCancelled(true);
                return;
            }

            if (EliteMobs.worldGuardIsEnabled && !WorldGuardFlagChecker.checkFlag(eliteEntity.getLivingEntity().getLocation(),
                    WorldGuardCompatibility.getEliteMobsAntiExploitFlag()))
                return;
            Bukkit.getServer().getPluginManager().callEvent(new GenericAntiExploitEvent(eliteEntity, event));

        }

    }

}
