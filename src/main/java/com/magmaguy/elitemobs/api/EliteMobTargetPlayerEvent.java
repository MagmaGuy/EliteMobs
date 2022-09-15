package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EliteMobTargetPlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteEntity eliteEntity;
    private final Player player;
    private final EntityTargetLivingEntityEvent entityTargetLivingEntityEvent;

    public EliteMobTargetPlayerEvent(EliteEntity eliteEntity, Player player, EntityTargetLivingEntityEvent event) {
        this.entity = event.getEntity();
        this.eliteEntity = eliteEntity;
        this.player = player;
        this.entityTargetLivingEntityEvent = event;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteEntity getEliteMobEntity() {
        return this.eliteEntity;
    }

    public Player getPlayer() {
        return this.player;
    }

    public EntityTargetLivingEntityEvent entityTargetLivingEntityEvent() {
        return this.entityTargetLivingEntityEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class EliteMobTargetPlayerEventFilter implements Listener {
        @EventHandler
        public void onMobTarget(EntityTargetLivingEntityEvent event) {
            if (!(event.getTarget() instanceof Player player)) return;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteEntity == null) return;

            if (player.getGameMode().equals(GameMode.ADVENTURE) || player.getGameMode().equals(GameMode.SURVIVAL))
                Bukkit.getServer().getPluginManager().callEvent(new EliteMobTargetPlayerEvent(eliteEntity, player, event));
        }
    }

}
