package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;

/**
 * Synchronous gameplay award hook, after eligibility checks and native/permission multipliers.
 * Administrative and PlayerData XP writes do not fire this event. Listeners modify the amount
 * in Bukkit priority order; cancellation or zero suppresses this award and its gain feedback.
 * EliteMobs commits the accepted amount and owns persistence and level-up effects.
 */
public final class EliteSkillXpGainEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    public enum Reason { COMBAT }

    private final Player player;
    private final SkillType skillType;
    private final EliteEntity eliteEntity;
    private final long originalXp;
    private long xp;
    private boolean cancelled;

    public EliteSkillXpGainEvent(Player player, SkillType skillType, EliteEntity eliteEntity, long xp) {
        this.player = Objects.requireNonNull(player, "player");
        this.skillType = Objects.requireNonNull(skillType, "skillType");
        this.eliteEntity = Objects.requireNonNull(eliteEntity, "eliteEntity");
        setXp(xp);
        this.originalXp = xp;
    }

    public Player getPlayer() { return player; }
    public SkillType getSkillType() { return skillType; }
    public EliteEntity getEliteEntity() { return eliteEntity; }
    public Reason getReason() { return Reason.COMBAT; }
    public long getOriginalXp() { return originalXp; }
    public long getXp() { return xp; }

    /** Negative values, including arithmetic that has overflowed below zero, are rejected. */
    public void setXp(long xp) {
        if (xp < 0) throw new IllegalArgumentException("Skill XP must be non-negative");
        this.xp = xp;
    }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
