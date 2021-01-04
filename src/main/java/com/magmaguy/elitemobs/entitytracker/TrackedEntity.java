package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class tracks Minecraft entities and links them to EliteMobs objects such as EliteMobEntities.
 * This is necessary for the combat system
 */
public class TrackedEntity {

    public static HashMap<UUID, TrackedEntity> trackedEntities = new HashMap<>();

    public UUID uuid;
    public Entity entity;
    public BukkitTask bukkitTask;
    //these defaults only get overridden under certain circumstances
    public boolean removeWhenFarAway, removeOnShutdown;
    public HashMap trackedHashMap;

    public TrackedEntity(UUID uuid, Entity entity, boolean removeWhenFarAway, boolean removeOnShutdown, HashMap trackedHashMap) {
        this.uuid = uuid;
        this.entity = entity;
        this.removeWhenFarAway = removeWhenFarAway;
        this.removeOnShutdown = removeOnShutdown;
        if (removeOnShutdown)
            if (entity != null)
                CrashFix.persistentTracker(entity);
        trackedEntities.put(uuid, this);
        this.trackedHashMap = trackedHashMap;
        startTrackableWatchdog();
    }


    /**
     * Entities removed via the API .remove() call do not trigger any events. As such, this method checks if the entity
     * still exists on repeat until it is removed. This is to prevent memory leaks.
     * This runs in async to improve performance.
     */
    protected void startTrackableWatchdog() {
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (entity == null) {
                    remove(RemovalReason.OTHER);
                    cancel();
                    return;
                }

                if (!entity.isValid()) {
                    doUnload(RemovalReason.CHUNK_UNLOAD);
                    cancel();
                    return;
                }
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 60 * 5, 20 * 60 * 5);
    }

    protected void remove(RemovalReason removalReason) {
        if (entity != null) {
            if (removalReason.equals(RemovalReason.SHUTDOWN))
                if (!entity.isValid() && !removeWhenFarAway) {
                    SimplePersistentEntity.PersistentEntityEvent.ignore = true;
                    entity.getLocation().getChunk();
                    entity = Bukkit.getEntity(uuid);
                    SimplePersistentEntity.PersistentEntityEvent.ignore = false;
                }
            if (entity != null)
                entity.remove();
        }
        untrack(removalReason);
    }

    /**
     * This is used by the child classes to make sure that the removal of the entity is completed correctly.
     * NOTE: Entities which are simply untracked, like Super Mobs, also run this method at the end of their untracking.
     */
    protected void specificRemoveHandling(RemovalReason removalReason) {
    }

    public void untrack(RemovalReason removalReason) {
        trackedHashMap.remove(uuid);
        //Don't remove on shutdown due to CME error, clear all tracked entities during shutdown globally
        if (!removalReason.equals(RemovalReason.SHUTDOWN))
            trackedEntities.remove(uuid);
        bukkitTask.cancel();
        specificRemoveHandling(removalReason);
    }

    protected void doUnload(RemovalReason removalReason) {
        if (removeWhenFarAway)
            remove(removalReason);
        else
            untrack(removalReason);
    }

    protected void doShutdown() {
        if (removeOnShutdown)
            remove(RemovalReason.SHUTDOWN);
        else
            untrack(RemovalReason.SHUTDOWN);
    }

    public void doDeath() {
        remove(RemovalReason.DEATH);
    }

}
