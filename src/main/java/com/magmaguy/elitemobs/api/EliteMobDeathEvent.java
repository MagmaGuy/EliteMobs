package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
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
    private final EliteEntity eliteEntity;
    private final EntityDeathEvent entityDeathEvent;

    public EliteMobDeathEvent(EliteEntity eliteEntity, EntityDeathEvent entityDeathEvent) {
        this.entity = eliteEntity.getUnsyncedLivingEntity();
        this.eliteEntity = eliteEntity;
        this.entityDeathEvent = entityDeathEvent;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteEntity getEliteEntity() {
        return this.eliteEntity;
    }

    public EntityDeathEvent getEntityDeathEvent() {
        return this.entityDeathEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class EliteMobDeathEventFilter implements Listener {
        @EventHandler
        public void onMobDeath(EntityDeathEvent event) {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteEntity == null) return;
            new EventCaller(new EliteMobDeathEvent(eliteEntity, event));
            new EventCaller(new EliteMobRemoveEvent(eliteEntity, RemovalReason.DEATH));
            if (eliteEntity.getUnsyncedLivingEntity() == null) return;
            EntityTracker.unregister(eliteEntity.getUnsyncedLivingEntity().getUniqueId(), RemovalReason.DEATH);
        }
    }

}
