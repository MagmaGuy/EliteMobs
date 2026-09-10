package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * Cancellable authorization point fired immediately before one hostile class effect is applied.
 * Protection and encounter plugins can reject damage-independent control, marking, or taunting
 * without manufacturing a fake damage event and its associated combat/accounting side effects.
 */
public final class ClassAbilityAffectEliteEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player caster;
    private final EliteEntity eliteEntity;
    private final String abilityId;
    private final AbilityEffect effect;
    private boolean cancelled;

    public ClassAbilityAffectEliteEvent(
            Player caster,
            EliteEntity eliteEntity,
            String abilityId,
            AbilityEffect effect) {
        this.caster = Objects.requireNonNull(caster, "caster");
        this.eliteEntity = Objects.requireNonNull(eliteEntity, "eliteEntity");
        this.abilityId = Objects.requireNonNull(abilityId, "abilityId");
        this.effect = Objects.requireNonNull(effect, "effect");
    }

    public Player getCaster() {
        return caster;
    }

    public EliteEntity getEliteEntity() {
        return eliteEntity;
    }

    public String getAbilityId() {
        return abilityId;
    }

    public AbilityEffect getEffect() {
        return effect;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
