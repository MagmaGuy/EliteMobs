package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamagedByEliteMobEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Entity entity;
    private EliteMobEntity eliteMobEntity;
    private Player player;
    private boolean isCancelled = false;
    private EntityDamageByEntityEvent entityDamageByEntityEvent;

    public PlayerDamagedByEliteMobEvent(EliteMobEntity eliteMobEntity, Player player, EntityDamageByEntityEvent event) {
        this.entity = event.getEntity();
        this.eliteMobEntity = eliteMobEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteMobEntity getEliteMobEntity() {
        return this.eliteMobEntity;
    }

    public Player getPlayer() {
        return this.player;
    }

    public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
        return this.entityDamageByEntityEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
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
        entityDamageByEntityEvent.setCancelled(b);
    }

    public static class PlayerDamagedByEliteMobEventFilter implements Listener {
        @EventHandler
        public void onEliteMobAttack(EntityDamageByEntityEvent event) {
            if (event.isCancelled()) return;
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();
            EliteMobEntity eliteMobEntity = null;
            if (event.getDamager() instanceof LivingEntity)
                eliteMobEntity = EntityTracker.getEliteMobEntity(event.getDamager());
            else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity)
                eliteMobEntity = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getDamager()).getShooter());
            if (eliteMobEntity == null) return;

            Bukkit.getServer().getPluginManager().callEvent(new PlayerDamagedByEliteMobEvent(eliteMobEntity, player, event));
        }
    }

}
