package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.entity.EntityType;

import java.util.HashSet;

public class EliteEvent {

    private static HashSet<EliteEvent> activeEvents = new HashSet<>();

    public static void addActiveEvent(EliteEvent eliteEvent) {
        activeEvents.add(eliteEvent);
    }

    public static void removeActiveEvent(EliteEvent eliteEvent) {
        activeEvents.remove(eliteEvent);
    }

    public static EliteEvent getEliteEvent(EliteMobEntity eliteMobEntity) {
        for (EliteEvent eliteEvent : activeEvents)
            if (eliteEvent.eventType.equals(EventType.KILL_BOSS) && eliteEvent.getBossEntity().equals(eliteMobEntity))
                return eliteEvent;
        return null;
    }

    public static EliteEvent getEliteEvent(EntityType entityType) {
        for (EliteEvent eliteEvent : activeEvents)
            if (eliteEvent.eventType.equals(EventType.KILL_COUNT) && eliteEvent.getEntityType().equals(entityType))
                return eliteEvent;
        return null;
    }

    public enum EventType {
        KILL_BOSS,
        KILL_COUNT
    }

    private boolean hasLivingBoss;
    private EliteMobEntity bossEntity;
    private EntityType entityType;
    private EventType eventType;

    public EliteEvent(EventType eventType, EliteMobEntity bossEntity) {
        setEventType(eventType);
        setBossEntity(bossEntity);
    }

    public EliteEvent(EventType eventType, EntityType entityType) {
        setEventType(eventType);
        setEntityType(entityType);
    }

    public boolean gethasLivingBoss() {
        return this.hasLivingBoss;
    }

    public void setHasLivingBoss(boolean hasLivingBoss) {
        this.hasLivingBoss = hasLivingBoss;
    }

    public EliteMobEntity getBossEntity() {
        return this.bossEntity;
    }

    public void setBossEntity(EliteMobEntity bossEntity) {
        this.bossEntity = bossEntity;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void completeEvent() {
        removeActiveEvent(this);
    }

}
