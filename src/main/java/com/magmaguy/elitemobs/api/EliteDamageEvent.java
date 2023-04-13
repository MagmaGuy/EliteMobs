package com.magmaguy.elitemobs.api;


import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteDamageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled = false;
    @Getter
    private double damage;

    private final Event event;

    /**
     * Classes that deal with damage in EliteMobs extend this class in order to inherit modifiable and cancellable behavior.
     * This is purely just used for API purposes, such as with the Elite Scripting system. Does not do anything on its own.
     *
     * @param damage Original damage which can be modified.
     * @param event  Original event which can be cancelled.
     */

    public EliteDamageEvent(double damage, Event event) {
        this.damage = damage;
        this.event = event;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
        ((Cancellable) event).setCancelled(b);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
