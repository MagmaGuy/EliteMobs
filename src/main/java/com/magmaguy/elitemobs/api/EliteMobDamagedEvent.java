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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobDamagedEvent extends EliteDamageEvent {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Entity entity;
    @Getter
    private final EliteEntity eliteEntity;
    @Getter
    private final EntityDamageEvent entityDamageEvent;

    public EliteMobDamagedEvent(EliteEntity eliteEntity, EntityDamageEvent event, double damage) {
        super(damage, event);
        this.entity = eliteEntity.getLivingEntity();
        this.eliteEntity = eliteEntity;
        this.entityDamageEvent = event;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class EliteMobDamageEventFilter implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityDamagedEvent(EntityDamageEvent event) {

            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            if (eliteEntity == null) return;

            EliteMobDamagedEvent eliteMobDamagedEvent = new EliteMobDamagedEvent(eliteEntity, event, event.getDamage());
            new EventCaller(eliteMobDamagedEvent);
            if (eliteMobDamagedEvent.isCancelled()) {
                eliteMobDamagedEvent.setCancelled(true);
                return;
            }

            //happens if it dies
            if (!eliteEntity.isValid()) return;

            if (EliteMobs.worldGuardIsEnabled && !WorldGuardFlagChecker.checkFlag(eliteEntity.getLivingEntity().getLocation(),
                    WorldGuardCompatibility.getELITEMOBS_ANTIEXPLOIT()))
                return;

            Bukkit.getServer().getPluginManager().callEvent(new GenericAntiExploitEvent(eliteEntity, event));

        }

    }

}
