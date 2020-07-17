package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.worldguard.WorldGuardFlagChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EliteMobDamagedByPlayerEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteMobEntity eliteMobEntity;
    private final Player player;
    private boolean isCancelled = false;
    public boolean rangedAttack;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    private final double damage;

    public EliteMobDamagedByPlayerEvent(EliteMobEntity eliteMobEntity, Player player, EntityDamageByEntityEvent event, double damage) {
        this.damage = damage;
        this.entity = eliteMobEntity.getLivingEntity();
        this.eliteMobEntity = eliteMobEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        this.isCancelled = event.isCancelled();
        this.rangedAttack = event.getDamager() instanceof Projectile;
        if (event.isCancelled()) return;
        if (eliteMobEntity.isInAntiExploitCooldown()) return;
        //No antiexploit checks for dungeons
        if (EliteMobs.worldguardIsEnabled && !WorldGuardFlagChecker.checkFlag(eliteMobEntity.getLivingEntity().getLocation(), WorldGuardCompatibility.getEliteMobsAntiExploitFlag()))
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

}
