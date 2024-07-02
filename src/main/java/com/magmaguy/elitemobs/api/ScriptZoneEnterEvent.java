package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ScriptZoneEnterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final EliteEntity eliteEntity;
    @Getter
    private final LivingEntity entity;

    public ScriptZoneEnterEvent(EliteEntity customBossEntity, LivingEntity entity) {
        this.eliteEntity = customBossEntity;
        this.entity = entity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
