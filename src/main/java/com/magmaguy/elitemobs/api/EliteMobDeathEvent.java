package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EliteMobDeathEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteMobEntity eliteMobEntity;
    private final EntityDeathEvent entityDeathEvent;

    public EliteMobDeathEvent(EliteMobEntity eliteMobEntity, EntityDeathEvent entityDeathEvent) {
        this.entity = eliteMobEntity.getLivingEntity();
        this.eliteMobEntity = eliteMobEntity;
        this.entityDeathEvent = entityDeathEvent;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteMobEntity getEliteMobEntity() {
        return this.eliteMobEntity;
    }

    public EntityDeathEvent getEntityDeathEvent() {
        return this.entityDeathEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static class EliteMobDeathEventFilter implements Listener {
        @EventHandler
        public void onMobDeath(EntityDeathEvent event) {
            EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteMobEntity == null) return;
            new EventCaller(new EliteMobDeathEvent(eliteMobEntity, event));
            new EventCaller(new EliteMobRemoveEvent(eliteMobEntity, RemovalReason.DEATH));
        }
    }

}
