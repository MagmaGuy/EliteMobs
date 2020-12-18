package com.magmaguy.elitemobs.entitytracker;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.UUID;

public class TrackedEntity {

    public static HashMap<UUID, TrackedEntity> trackedEntities = new HashMap<>();

    public static TrackedEntity getTrackedEntity(UUID uuid) {
        return trackedEntities.get(uuid);
    }

    public static TrackedEntity getTrackedEntity(Entity entity) {
        return trackedEntities.get(entity.getUniqueId());
    }

    public static void doChunkUnload(UUID uuid) {
        TrackedEntity trackedEntity = getTrackedEntity(uuid);
        if (trackedEntity != null)
            trackedEntity.doChunkUnload();
    }

    public static void doShutdown(UUID uuid) {

    }

    /**
     * All types of entities that EliteMobs needs to keep track of.
     * The following should despawn on chunk unload:
     * VISUAL_EFFECT, ELITE_MOB, NPC, PROJECTILE
     * The following should persist through chunk unloads:
     * REGIONAL_BOSS, SUPER_MOB
     */
    public enum EliteEntityType {
        VISUAL_EFFECT,
        ELITE_MOB,
        SUPER_MOB,
        REGIONAL_BOSS,
        NPC,
        PROJECTILE,
        CUSTOM_BOSS_PERSISTENT,
        CUSTOM_BOSS_NON_PERSISTENT
    }

    public UUID uuid;
    public EliteEntityType eliteEntityType;

    public TrackedEntity(UUID uuid, EliteEntityType eliteEntityType) {

    }

    public void doChunkUnload() {
        switch (eliteEntityType) {
            case VISUAL_EFFECT:
            case ELITE_MOB:
            case NPC:
            case PROJECTILE:
            case CUSTOM_BOSS_NON_PERSISTENT:
                //delete
            case SUPER_MOB:
            case REGIONAL_BOSS:
            case CUSTOM_BOSS_PERSISTENT:
            default:
                return;
            //don't do anything
        }
    }

}
