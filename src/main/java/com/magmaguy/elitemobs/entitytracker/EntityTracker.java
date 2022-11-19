package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.NPCEntitySpawnEvent;
import com.magmaguy.elitemobs.api.SuperMobSpawnEvent;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;

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

    public static boolean isEliteMob(Entity entity) {
        return PersistentTagger.isEliteEntity(entity);
    }

    @Nullable
    public static EliteEntity getEliteMobEntity(Entity entity) {
        return PersistentTagger.getEliteEntity(entity);
    }

    public static void unregisterEliteEntity(Entity entity, RemovalReason removalReason) {
        EliteEntity eliteEntity = getEliteMobEntity(entity);
        if (eliteEntity == null) return;
        //Removal from the hashmap is not guaranteed here as some forms of removal don't completely wipe the elite entity out
        eliteEntity.remove(removalReason);
    }

    //Super Mobs
    public static void registerSuperMob(LivingEntity livingEntity) {
        SuperMobSpawnEvent superMobSpawnEvent = new SuperMobSpawnEvent(livingEntity);
        new EventCaller(superMobSpawnEvent);
        if (superMobSpawnEvent.isCancelled()) return;
        PersistentTagger.tagSuperMob(livingEntity);
    }

    public static boolean isSuperMob(Entity entity) {
        return PersistentTagger.isSuperMob(entity);
    }

    public static List<LivingEntity> getSuperMobs() {
        List<LivingEntity> superMobs = new ArrayList<>();
        for (World world : Bukkit.getWorlds())
            for (Entity entity : world.getEntities())
                if (isSuperMob(entity))
                    superMobs.add((LivingEntity) entity);
        return superMobs;
    }

    //Visual effects - usually trails around elites
    public static List<Entity> getItemVisualEffects() {
        List<Entity> visualEffects = new ArrayList<>();
        for (World world : Bukkit.getWorlds())
            for (Entity entity : world.getEntities())
                if (isSuperMob(entity))
                    visualEffects.add(entity);
        return visualEffects;
    }

    public static void registerVisualEffects(Entity entity) {
        PersistentTagger.tagVisualEffect(entity);
    }

    public static boolean isVisualEffect(Entity entity) {
        return PersistentTagger.isVisualEffect(entity);
    }

    public static void unregisterVisualEffect(Entity entity) {
        if (isVisualEffect(entity)) entity.remove();
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

    public static void unregisterNPCEntity(Entity entity, RemovalReason removalReason) {
        NPCEntity npcEntity = getNPCEntity(entity);
        if (npcEntity == null) return;
        //Removal from the hashmap is not guaranteed here as some forms of removal don't completely wipe the elite entity out
        npcEntity.remove(removalReason);
    }

    //Temporary blocks - blocks in powers
    public static void addTemporaryBlock(Block block, int ticks, Material replacementMaterial) {
        Material previousMaterial = block.getType();
        if (temporaryBlocks.contains(block)) previousMaterial = null;
        temporaryBlocks.add(block);
        block.setType(replacementMaterial);
        Material finalPreviousMaterial = previousMaterial;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType().equals(replacementMaterial) && finalPreviousMaterial != null)
                    block.setType(finalPreviousMaterial);
                temporaryBlocks.remove(block);
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

    public static void unregisterProjectileEntity(Entity entity) {
        if (isProjectileEntity(entity)) entity.remove();
    }

    public static void unregister(Entity entity, RemovalReason removalReason) {
        unregisterEliteEntity(entity, removalReason);
        unregisterVisualEffect(entity);
        unregisterProjectileEntity(entity);
        unregisterNPCEntity(entity, removalReason);
    }

    public static void wipeChunk(Chunk chunk, RemovalReason removalReason) {
        for (Entity entity : chunk.getEntities())
            unregister(entity, removalReason);
    }

    public static void wipeWorld(World world, RemovalReason removalReason) {
        for (Chunk chunk : world.getLoadedChunks())
            wipeChunk(chunk, removalReason);
    }

    public static void wipeShutdown() {
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


    //Events
    @EventHandler(ignoreCancelled = true)
    public void onUnload(ChunkUnloadEvent event) {
        EntityTracker.wipeChunk(event.getChunk(), RemovalReason.CHUNK_UNLOAD);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        EntityTracker.wipeWorld(event.getWorld(), RemovalReason.WORLD_UNLOAD);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMine(BlockBreakEvent event) {
        if (!isTemporaryBlock(event.getBlock())) return;
        event.setDropItems(false);
        removeTemporaryBlock(event.getBlock());
    }

}
