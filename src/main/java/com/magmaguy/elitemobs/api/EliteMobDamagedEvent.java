package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobDamagedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Entity entity;
    @Getter
    private final EliteEntity eliteEntity;
    @Getter
    private final EntityDamageEvent entityDamageEvent;
    private boolean isCancelled = false;
    @Getter
    private double damage;

    public EliteMobDamagedEvent(EliteEntity eliteEntity, EntityDamageEvent event, double damage) {
        this.entity = eliteEntity.getLivingEntity();
        this.eliteEntity = eliteEntity;
        this.entityDamageEvent = event;
        this.damage = damage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
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
        public void onEntityDamagedEvent(EntityDamageEvent event) {

            if (event.isCancelled()) return;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteEntity == null) return;

            EliteMobDamagedEvent eliteMobDamagedEvent = new EliteMobDamagedEvent(eliteEntity, event, event.getDamage());
            new EventCaller(eliteMobDamagedEvent);
            if (eliteMobDamagedEvent.isCancelled) {
                eliteMobDamagedEvent.setCancelled(true);
                return;
            }

            //happens if it dies
            if (!eliteEntity.isValid()) return;

            if (EliteMobs.worldGuardIsEnabled && !WorldGuardFlagChecker.checkFlag(eliteEntity.getLivingEntity().getLocation(),
                    WorldGuardCompatibility.getEliteMobsAntiExploitFlag()))
                return;

            Bukkit.getServer().getPluginManager().callEvent(new GenericAntiExploitEvent(eliteEntity, event));

        }

    }

}
