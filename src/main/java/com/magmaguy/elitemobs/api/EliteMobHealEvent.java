package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteMobHealEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final EliteEntity eliteEntity;
    @Getter
    private double healAmount = 0;
    private boolean isCancelled = false;
    @Getter
    private boolean fullHeal = false;

    public EliteMobHealEvent(EliteEntity eliteEntity, double healAmount) {
        this.eliteEntity = eliteEntity;
        this.healAmount = healAmount;
    }

    public EliteMobHealEvent(EliteEntity eliteEntity, boolean fullHeal) {
        this.eliteEntity = eliteEntity;
        this.fullHeal = fullHeal;
    }

    public static HandlerList getHandlerList() {
        return handlers;
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
    }

}
