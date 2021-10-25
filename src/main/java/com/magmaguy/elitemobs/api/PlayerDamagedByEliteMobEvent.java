package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.combatsystem.PlayerDamagedByEliteMobHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

public class PlayerDamagedByEliteMobEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteEntity eliteEntity;
    private final Player player;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    private final Projectile projectile;
    private boolean isCancelled = false;

    public PlayerDamagedByEliteMobEvent(EliteEntity eliteEntity, Player player, EntityDamageByEntityEvent event, Projectile projectile) {
        this.entity = event.getEntity();
        this.eliteEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        this.projectile = projectile;
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

    public Projectile getProjectile() {
        return projectile;
    }

    public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
        return this.entityDamageByEntityEvent;
    }

    @Override
    public HandlerList getHandlers() {
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
        public void onEliteMobAttack2(EntityDamageByEntityEvent event) {
            if (event.isCancelled() && !PlayerDamagedByEliteMobHandler.bypass) return;
            if (event.isCancelled()) event.setCancelled(false);
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();

            //citizens
            if (player.hasMetadata("NPC"))
                return;

            Projectile projectile = null;

            EliteEntity eliteEntity = null;
            if (event.getDamager() instanceof LivingEntity)
                eliteEntity = EntityTracker.getEliteMobEntity(event.getDamager());
            else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {
                eliteEntity = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getDamager()).getShooter());
                projectile = (Projectile) event.getDamager();
            } else if (event.getDamager().getType().equals(EntityType.EVOKER_FANGS))
                if (((EvokerFangs) event.getDamager()).getOwner() != null)
                    eliteEntity = EntityTracker.getEliteMobEntity(((EvokerFangs) event.getDamager()).getOwner());

            if (eliteEntity == null) return;

            //dodge chance
            if (ThreadLocalRandom.current().nextDouble() < GuildRank.dodgeBonusValue(GuildRank.getGuildPrestigeRank(player), GuildRank.getActiveGuildRank(player)) / 100) {
                player.sendTitle("", "Dodged!");
                event.setCancelled(true);
                return;
            }

            PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent = new PlayerDamagedByEliteMobEvent(eliteEntity, player, event, projectile);
            if (!playerDamagedByEliteMobEvent.isCancelled)
                new EventCaller(playerDamagedByEliteMobEvent);
        }
    }

}
