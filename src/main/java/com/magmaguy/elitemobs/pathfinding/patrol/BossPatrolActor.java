package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

final class BossPatrolActor implements PatrolActor {
    private final CustomBossEntity boss;
    private final PatrolOrigin origin;
    private final String canonicalIdentity;

    BossPatrolActor(CustomBossEntity boss) {
        this.boss = boss;
        this.origin = PatrolOrigin.from(boss.getSpawnLocation(), boss.getWorldName());
        this.canonicalIdentity = PatrolIdentity.canonical(
                "boss", boss.getCustomBossesConfigFields().getFilename(), origin);
    }

    @Override public UUID runtimeId() { return boss.getEliteUUID(); }
    @Override public String canonicalIdentity() { return canonicalIdentity; }
    @Override public String displayName() { return boss.getCustomBossesConfigFields().getFilename(); }
    @Override public PatrolOrigin origin() { return origin; }
    @Override public PatrolRoute route() { return boss.getCustomBossesConfigFields().getPatrolRoute(); }
    @Override public LivingEntity body() { return boss.getLivingEntity(); }
    @Override public boolean persistsWhileDetached() {
        return Boolean.TRUE.equals(boss.getIsPersistent()) && !(boss instanceof InstancedBossEntity);
    }
    @Override public boolean isInCombat() { return boss.isInCombat(); }

    @Override
    public boolean prepareBody() {
        LivingEntity body = body();
        return body != null && body.isValid()
                && boss.getCustomBossesConfigFields().isAi()
                && boss.getMovementSpeedAttribute() > 0D
                && !body.isInsideVehicle();
    }

    @Override
    public String ineligibleReason() {
        if (!boss.getCustomBossesConfigFields().isAi()) return "ai is disabled";
        if (boss.getMovementSpeedAttribute() <= 0D) return "movement speed is zero";
        LivingEntity body = body();
        if (body != null && body.isInsideVehicle()) return "the routed boss is a passenger";
        return "the entity cannot use native pathfinding";
    }

    @Override
    public double currentMovementSpeed() {
        LivingEntity body = body();
        return body == null ? boss.getMovementSpeedAttribute()
                : AttributeManager.getAttributeBaseValue(body, "generic_movement_speed");
    }

    @Override public void updatePersistentLocation(Location safeLocation) {
        boss.updatePersistentLocation(safeLocation);
    }
    @Override public void materialize() {
        if (boss instanceof RegionalBossEntity regionalBoss && regionalBoss.isRespawning()) return;
        boss.chunkLoad();
    }
    @Override public void syncVisuals() { }
    @Override public Object owner() { return boss; }
}
