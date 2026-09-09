package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.NPCEntitySpawnEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.TemporaryBlockManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class EntityTracker implements Listener {

    @Getter
    private static final HashMap<UUID, EliteEntity> eliteMobEntities = new HashMap<>();
    @Getter
    private static final HashMap<UUID, NPCEntity> npcEntities = new HashMap<>();

    public static void registerEliteMob(EliteEntity eliteEntity) {
        tryRegisterEliteMob(eliteEntity);
    }

    /** Publishes only a fully initialized actor accepted by its single spawn event. */
    public static boolean tryRegisterEliteMob(EliteEntity eliteEntity) {
        LivingEntity body = eliteEntity.getLivingEntity();
        if (body == null || body.isDead()) return false;
        EliteMobSpawnEvent eliteMobSpawnEvent = new EliteMobSpawnEvent(eliteEntity);
        new EventCaller(eliteMobSpawnEvent);
        if (eliteMobSpawnEvent.isCancelled() || eliteEntity.getLivingEntity() != body || body.isDead()) return false;
        PersistentTagger.tagElite(body, eliteEntity.getEliteUUID());
        eliteMobEntities.put(eliteEntity.getEliteUUID(), eliteEntity);
        return true;
    }

    public static void registerEliteMob(EliteEntity eliteEntity, LivingEntity livingEntity) {
        if (eliteEntity.getLivingEntity() != livingEntity)
            throw new IllegalArgumentException("Elite actor must be initialized before registration");
        registerEliteMob(eliteEntity);
    }

    public static boolean isEliteMob(Entity entity) {
        return PersistentTagger.isEliteEntity(entity);
    }

    @Nullable
    public static EliteEntity getEliteMobEntity(Entity entity) {
        return PersistentTagger.getEliteEntity(entity);
    }

    private static BukkitTask ManagedEntityTask = null;

    public static void registerVisualEffects(Entity entity) {
        PersistentTagger.tagVisualEffect(entity);
    }

    public static boolean isVisualEffect(Entity entity) {
        return PersistentTagger.isVisualEffect(entity);
    }

    public static boolean unregisterEliteEntity(Entity entity, RemovalReason removalReason) {
        EliteEntity eliteEntity = getEliteMobEntity(entity);
        if (eliteEntity == null) return false;
        //Removal from the hashmap is not guaranteed here as some forms of removal don't completely wipe the elite entity out
        eliteEntity.remove(removalReason);
        return true;
    }

    public static void registerNPCEntity(NPCEntity npc) {
        NPCEntitySpawnEvent npcEntitySpawnEvent = new NPCEntitySpawnEvent(npc.getVillager(), npc);
        new EventCaller(npcEntitySpawnEvent);
        if (npcEntitySpawnEvent.isCancelled()) return;
        npcEntities.put(npc.getUuid(), npc);
        PersistentTagger.tagNPC(npc.getVillager(), npc.getUuid());
    }

    public static boolean isNPCEntity(Entity entity) {
        return PersistentTagger.isNPC(entity);
    }

    public static NPCEntity getNPCEntity(Entity entity) {
        return PersistentTagger.getNPC(entity);
    }

    public static boolean unregisterVisualEffect(Entity entity) {
        if (isVisualEffect(entity)) {
            entity.remove();
            return true;
        }
        return false;
    }

    //Temporary blocks - delegated to MagmaCore's TemporaryBlockManager
    public static void addTemporaryBlock(Block block, int ticks, Material replacementMaterial) {
        //Don't override death banners, this causes issues
        if (TemporaryBlockManager.isTemporaryBlock(block) && block.getType().equals(Material.RED_BANNER)) return;
        TemporaryBlockManager.addTemporaryBlock(block, ticks, replacementMaterial);
    }

    public static boolean isTemporaryBlock(Block block) {
        return TemporaryBlockManager.isTemporaryBlock(block);
    }

    public static void removeTemporaryBlock(Block block) {
        TemporaryBlockManager.removeTemporaryBlock(block);
    }

    //Projectile entities - Minecraft already stores data about who fired them, so just simple entities.
    //NOTE: there are no events for firing elitemobs projectiles, does not seem like it would serve a good function as of right now
    public static void registerProjectileEntity(Projectile projectile) {
        PersistentTagger.tagEliteProjectile(projectile);
    }

    public static boolean isProjectileEntity(Entity entity) {
        return PersistentTagger.isEliteProjectile(entity);
    }

    public static boolean unregisterNPCEntity(Entity entity, RemovalReason removalReason) {
        NPCEntity npcEntity = getNPCEntity(entity);
        if (npcEntity == null) return false;
        //Removal from the hashmap is not guaranteed here as some forms of removal don't completely wipe the elite entity out
        npcEntity.remove(removalReason);
        return true;
    }

    public static boolean unregisterProjectileEntity(Entity entity) {
        if (isProjectileEntity(entity)) {
            entity.remove();
            return true;
        }
        return false;
    }

    public static void wipeChunk(Chunk chunk, RemovalReason removalReason) {
        for (Entity entity : chunk.getEntities())
            unregister(entity, removalReason);
    }

    public static void wipeWorld(World world, RemovalReason removalReason) {
        for (Chunk chunk : world.getLoadedChunks())
            wipeChunk(chunk, removalReason);
    }

    public static void unregister(Entity entity, RemovalReason removalReason) {
        if (unregisterEliteEntity(entity, removalReason)) return;
        if (unregisterVisualEffect(entity)) return;
        if (unregisterProjectileEntity(entity)) return;
        if (unregisterNPCEntity(entity, removalReason)) {
        }
    }


    //Events
//    @EventHandler(ignoreCancelled = true)
//    public void onUnload(ChunkUnloadEvent event) {
//        EntityTracker.wipeChunk(event.getChunk(), RemovalReason.CHUNK_UNLOAD);
//    }

    public static void wipeShutdown() {
        if (ManagedEntityTask != null)
            ManagedEntityTask.cancel();
        for (EliteEntity eliteEntity : ((HashMap<UUID, EliteEntity>) eliteMobEntities.clone()).values())
            eliteEntity.remove(RemovalReason.SHUTDOWN);
        getEliteMobEntities().clear();
        for (NPCEntity npcEntity : ((HashMap<UUID, NPCEntity>) npcEntities.clone()).values())
            npcEntity.remove(RemovalReason.SHUTDOWN);
        getNpcEntities().clear();
        TemporaryBlockManager.shutdown();
        //Necessary for things such as visual effects which are not stored in memory, only tagged
        for (World world : Bukkit.getWorlds())
            for (Entity entity : world.getEntities())
                unregister(entity, RemovalReason.SHUTDOWN);
        CrashFix.knownSessionChunks.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        EntityTracker.wipeWorld(event.getWorld(), RemovalReason.WORLD_UNLOAD);
        //Undisguise everything left in the unloading world: LibsDisguises' registry
        //holds hard entity references, its own world-unload handler saves disguises
        //rather than removing them, and untracked entities never pass through
        //EliteEntity.remove() — each disguised entity left behind pins the unloaded
        //ServerLevel in memory.
        if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises"))
            for (Entity entity : event.getWorld().getEntities())
                com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity.undisguise(entity);
        purgeWorldReferences(event.getWorld());
        scheduleWorldRetentionCanary(event.getWorld());
        // Temporary block world cleanup is handled by MagmaCore's TemporaryBlockManager
    }

    /**
     * wipeWorld only reaches entities in still-loaded chunks of the unloading world. Tracked
     * elites whose chunks already unloaded, whose boss is waiting on a respawn timer, or whose
     * removal path intentionally keeps them tracked (regional bosses) stay in these maps holding
     * the unloaded world through their entities and locations — enough to keep every completed
     * instanced dungeon's ServerLevel in memory. Anything still pointing at the world gets a
     * proper removal, and whatever a removal path chose to keep is evicted anyway: a later spawn
     * re-registers through registerEliteMob, so eviction is always safe here.
     */
    private static void purgeWorldReferences(World world) {
        for (EliteEntity eliteEntity : new java.util.ArrayList<>(eliteMobEntities.values()))
            if (eliteEntity.referencesWorld(world))
                eliteEntity.remove(RemovalReason.WORLD_UNLOAD);
        eliteMobEntities.values().removeIf(eliteEntity -> eliteEntity.referencesWorld(world));

        for (NPCEntity npcEntity : new java.util.ArrayList<>(npcEntities.values()))
            if (npcEntity.referencesWorld(world))
                npcEntity.remove(RemovalReason.WORLD_UNLOAD);
        npcEntities.values().removeIf(npcEntity -> npcEntity.referencesWorld(world));
    }

    /**
     * Leak canary: a successfully unloaded world should become garbage-collectable.
     * If it is still strongly reachable minutes later, some plugin retains it — a
     * condition otherwise only visible in a heap dump.
     */
    private static void scheduleWorldRetentionCanary(World world) {
        String worldName = world.getName();
        UUID worldUUID = world.getUID();
        java.lang.ref.WeakReference<World> reference = new java.lang.ref.WeakReference<>(world);
        //Test/diagnostic override: -Delitemobs.worldRetentionCanaryTicks=<ticks> checks sooner,
        //forces a GC first so the weak reference is meaningful, and also logs the healthy case,
        //giving automated tests a positive line to assert instead of the absence of a warning.
        Long canaryTicksOverride = Long.getLong("elitemobs.worldRetentionCanaryTicks");
        long delayTicks = canaryTicksOverride != null ? canaryTicksOverride : 5L * 60L * 20L;
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            if (Bukkit.getWorld(worldUUID) != null) return; //unload was cancelled or the world was reloaded
            if (canaryTicksOverride != null) System.gc();
            if (reference.get() == null) {
                if (canaryTicksOverride != null)
                    com.magmaguy.magmacore.util.Logger.info("World retention canary: " + worldName
                            + " was garbage collected after unloading.");
                return;
            }
            com.magmaguy.magmacore.util.Logger.warn("World " + worldName + " was unloaded " + (delayTicks / 20L)
                    + " seconds ago but may still be retained in memory. If this appears after every instanced" +
                    " dungeon, a plugin is holding references to unloaded worlds (this leaks RAM) - take a heap" +
                    " dump and look for the retaining plugin.");
        }, delayTicks);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMine(BlockBreakEvent event) {
        if (!isTemporaryBlock(event.getBlock())) return;
        event.setDropItems(false);
        removeTemporaryBlock(event.getBlock());
    }

    //After many years of trying to make the chunk unload event work, I gave up and am now using a clock instead.
    //There's just too many bugs with how the chunk unloading works, unfortunately
    public static void managedEntityWatchdog() {
        ManagedEntityTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Safely iterate over eliteMobEntities by cloning the values first to avoid CME
                new HashSet<>(eliteMobEntities.values()).forEach(value -> {
                    if (value.getLivingEntity() != null && !value.getLivingEntity().isValid()) {
                        LivingEntity removedBody = value.getLivingEntity();
                        if (!EliteMindServiceModule.suspendForChunkUnload(value, removedBody))
                            value.remove(RemovalReason.CHUNK_UNLOAD);
                    }
                });

                // Safely iterate over npcEntities by cloning the values first to avoid CME
                new HashSet<>(npcEntities.values()).forEach(value -> {
                    if (value.getVillager() != null && !value.getVillager().isValid()) {
                        value.remove(RemovalReason.CHUNK_UNLOAD);
                    }
                });
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0,1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRemove(EntityRemoveEvent event) {
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (!event.getCause().equals(EntityRemoveEvent.Cause.UNLOAD)) return;
        if (eliteEntity != null
                && event.getEntity() instanceof LivingEntity livingEntity
                && EliteMindServiceModule.suspendForChunkUnload(eliteEntity, livingEntity)) return;
        EntityTracker.unregister(event.getEntity(), RemovalReason.CHUNK_UNLOAD);
    }


}
