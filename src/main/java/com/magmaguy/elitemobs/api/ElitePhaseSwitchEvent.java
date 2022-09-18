package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ElitePhaseSwitchEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final CustomBossEntity customBossEntity;
    @Getter
    private final PhaseBossEntity phaseBossEntity;

    public ElitePhaseSwitchEvent(CustomBossEntity customBossEntity, PhaseBossEntity phaseBossEntity) {
        this.customBossEntity = customBossEntity;
        this.phaseBossEntity = phaseBossEntity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
