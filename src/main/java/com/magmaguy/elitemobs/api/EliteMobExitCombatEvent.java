package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteMobExitCombatEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final EliteMobEntity eliteMobEntity;

    public EliteMobExitCombatEvent(EliteMobEntity eliteMobEntity) {
        this.eliteMobEntity = eliteMobEntity;
        eliteMobEntity.setIsInCombat(false);
        if (MobCombatSettingsConfig.regenerateCustomBossHealthOnCombatEnd)
            if (!eliteMobEntity.getLivingEntity().getType().equals(EntityType.PHANTOM))
                eliteMobEntity.fullHeal();
    }

    public EliteMobEntity getEliteMobEntity() {
        return this.eliteMobEntity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
