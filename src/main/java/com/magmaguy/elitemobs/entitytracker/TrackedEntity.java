package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntity;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
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

    public Entity entity;
    public UUID uuid;
    //these defaults only get overridden under certain circumstances
    public boolean removeWhenFarAway, removeOnShutdown;
    public HashMap trackedHashMap;
    private BukkitTask memoryWatchdog;

    private void memoryWatchdog() {
        memoryWatchdog = new BukkitRunnable() {
            @Override
            public void run() {
                //sometimes this seems to not get cancelled properly, so first check if it's still even registered
                if (!trackedEntities.containsKey(uuid)) {
                    cancel();
                    return;
                }
                if (entity == null || !entity.isValid())
                    remove(RemovalReason.OTHER);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 60 * 5, 20 * 60 * 5);
    }

    public TrackedEntity(UUID uuid, Entity entity, boolean removeWhenFarAway, boolean removeOnShutdown, HashMap trackedHashMap) {
        if (entity == null) {
            new DeveloperMessage("Failed to register entity: was null");
            return;
        }
        this.uuid = uuid;
        this.entity = entity;
        this.removeWhenFarAway = removeWhenFarAway;
        this.removeOnShutdown = removeOnShutdown;
        if (removeOnShutdown)
            CrashFix.persistentTracker(entity);
        trackedEntities.put(uuid, this);
        this.trackedHashMap = trackedHashMap;
        memoryWatchdog();
    }

    protected void remove(RemovalReason removalReason) {
        if (entity != null) {
            if (removalReason.equals(RemovalReason.SHUTDOWN))
                if (!entity.isValid() && !removeWhenFarAway) {
                    SimplePersistentEntity.PersistentEntityEvent.ignore = true;
                    entity.getLocation().getChunk();
                    entity = Bukkit.getEntity(entity.getUniqueId());
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
        memoryWatchdog.cancel();
        if (!removalReason.equals(RemovalReason.KILL_COMMAND))
            trackedHashMap.remove(uuid);
        //Don't remove on shutdown due to CME error, clear all tracked entities during shutdown globally
        if (!removalReason.equals(RemovalReason.SHUTDOWN) && !removalReason.equals(RemovalReason.KILL_COMMAND))
            trackedEntities.remove(uuid);
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
