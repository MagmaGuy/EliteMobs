package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.NPCEntitySpawnEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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
    @Getter
    private static final HashSet<Block> temporaryBlocks = new HashSet<>();

    public static void registerEliteMob(EliteEntity eliteEntity) {
        EliteMobSpawnEvent eliteMobSpawnEvent = new EliteMobSpawnEvent(eliteEntity);
        new EventCaller(eliteMobSpawnEvent);
        if (eliteMobSpawnEvent.isCancelled()) return;
        PersistentTagger.tagElite(eliteEntity.getLivingEntity(), eliteEntity.getEliteUUID());
        eliteMobEntities.put(eliteEntity.getEliteUUID(), eliteEntity);
    }

    public static void registerEliteMob(EliteEntity eliteEntity, LivingEntity livingEntity) {
        EliteMobSpawnEvent eliteMobSpawnEvent = new EliteMobSpawnEvent(eliteEntity);
        new EventCaller(eliteMobSpawnEvent);
        if (eliteMobSpawnEvent.isCancelled()) return;
        PersistentTagger.tagElite(livingEntity, eliteEntity.getEliteUUID());
        eliteMobEntities.put(eliteEntity.getEliteUUID(), eliteEntity);
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

    //Temporary blocks - blocks in powers
    public static void addTemporaryBlock(Block block, int ticks, Material replacementMaterial) {
        BlockData previousBlockData = block.getBlockData().clone();
        if (temporaryBlocks.contains(block)) previousBlockData = null;
        //Don't override death banners, this causes issues
        if (temporaryBlocks.contains(block) && block.getType().equals(Material.RED_BANNER)) return;
        temporaryBlocks.add(block);
        block.setType(replacementMaterial);
        UUID worldUUID = block.getWorld().getUID();
        BlockData finalPreviousBlockData = previousBlockData;
        //Death banners for instanced content don't timeout
        if (ticks < 0) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getWorld(worldUUID) == null) return;
                temporaryBlocks.remove(block);
                if (!block.getBlockData().equals(finalPreviousBlockData))
                    if (finalPreviousBlockData != null)
                        //Case if a temp block was placed and now needs to be restored
                        block.setBlockData(finalPreviousBlockData);
                    else
                        //Case if a temp block was placed on a temp block
                        block.setType(Material.AIR);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

    public static boolean isTemporaryBlock(Block block) {
        return temporaryBlocks.contains(block);
    }

    public static void removeTemporaryBlock(Block block) {
        temporaryBlocks.remove(block);
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
        for (Block block : temporaryBlocks)
            block.setType(Material.AIR);
        temporaryBlocks.clear();
        //Necessary for things such as visual effects which are not stored in memory, only tagged
        for (World world : Bukkit.getWorlds())
            for (Entity entity : world.getEntities())
                unregister(entity, RemovalReason.SHUTDOWN);
        CustomBossEntity.getTrackableCustomBosses().clear();
        CrashFix.knownSessionChunks.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        EntityTracker.wipeWorld(event.getWorld(), RemovalReason.WORLD_UNLOAD);
        temporaryBlocks.removeIf(block -> block.getWorld().equals(event.getWorld()));
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
        if (event.getCause().equals(EntityRemoveEvent.Cause.UNLOAD))
            EntityTracker.unregister(event.getEntity(), RemovalReason.CHUNK_UNLOAD);
    }


}
