package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.worldguard.WorldGuardFlagChecker;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EliteMobDamagedByPlayerEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Entity entity;
    private EliteMobEntity eliteMobEntity;
    private Player player;
    private boolean isCancelled = false;
    private EntityDamageByEntityEvent entityDamageByEntityEvent;

    public EliteMobDamagedByPlayerEvent(EliteMobEntity eliteMobEntity, Player player, EntityDamageByEntityEvent event) {
        this.entity = eliteMobEntity.getLivingEntity();
        this.eliteMobEntity = eliteMobEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        if (event.isCancelled()) return;
        if (!eliteMobEntity.getLivingEntity().hasAI()) return;
        if (eliteMobEntity.isInAntiExploitCooldown()) return;
        //No antiexploit checks for dungeons
        if (WorldGuardFlagChecker.checkFlag(eliteMobEntity.getLivingEntity().getLocation(), (StateFlag) WorldGuardCompatibility.getEliteMobsDungeonFlag()))
            return;
        Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByPlayerAntiExploitEvent(eliteMobEntity, this));
    }

    public Entity getEntity() {
        return entity;
    }

    public EliteMobEntity getEliteMobEntity() {
        return eliteMobEntity;
    }

    public Player getPlayer() {
        return player;
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
        entityDamageByEntityEvent.setCancelled(b);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


    public static class EntityDamagedByEntityFilter implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

            if (event.isCancelled()) return;
            EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteMobEntity == null) return;
            Player player = null;
            if (event.getDamager() instanceof Player)
                player = (Player) event.getDamager();
            if ((event.getDamager() instanceof Arrow || event.getDamager() instanceof Trident) &&
                    ((Projectile) event.getDamager()).getShooter() instanceof Player)
                player = (Player) ((Projectile) event.getDamager()).getShooter();
            if (player == null) return;

            Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByPlayerEvent(eliteMobEntity, player, event));
            if (eliteMobEntity.isInAntiExploitCooldown()) return;

        }

    }

}
