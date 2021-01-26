package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.NPCEntitySpawnEvent;
import com.magmaguy.elitemobs.api.SuperMobSpawnEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.HashMap;
import java.util.UUID;

public class EntityTracker implements Listener {

    //Elite Mobs
    public static HashMap<UUID, EliteMobEntity> getEliteMobs() {
        return EliteEntityTracker.eliteMobEntities;
    }

    public static boolean registerEliteMob(EliteMobEntity eliteMobEntity) {
        EliteMobSpawnEvent eliteMobSpawnEvent = new EliteMobSpawnEvent(eliteMobEntity);
        new EventCaller(eliteMobSpawnEvent);
        if (eliteMobSpawnEvent.isCancelled()) return false;
        new EliteEntityTracker(eliteMobEntity, eliteMobEntity.getPersistent());
        return true;
    }

    public static boolean isEliteMob(Entity entity) {
        return EliteEntityTracker.eliteMobEntities.containsKey(entity.getUniqueId());
    }

    public static EliteMobEntity getEliteMobEntity(Entity entity) {
        return entity == null ? null : getEliteMobEntity(entity.getUniqueId());
    }

    public static EliteMobEntity getEliteMobEntity(UUID uuid) {
        return EliteEntityTracker.eliteMobEntities.get(uuid);
    }


    //Super Mobs
    public static void registerSuperMob(LivingEntity livingEntity) {
        SuperMobSpawnEvent superMobSpawnEvent = new SuperMobSpawnEvent(livingEntity);
        new EventCaller(superMobSpawnEvent);
        if (superMobSpawnEvent.isCancelled()) return;
        new SuperMobEntityTracker(livingEntity.getUniqueId(), livingEntity);
    }

    public static boolean isSuperMob(Entity entity) {
        return SuperMobEntityTracker.superMobEntities.containsKey(entity.getUniqueId());
    }

    public static LivingEntity getSuperMob(Entity entity) {
        return SuperMobEntityTracker.superMobEntities.get(entity.getUniqueId());
    }

    public static HashMap<UUID, LivingEntity> getSuperMobs() {
        return SuperMobEntityTracker.superMobEntities;
    }


    //Armor Stands - for dialogue, damage displays, health displays...
    public static void registerArmorStands(ArmorStand armorStand) {
        new ArmorStandEntityTracker(armorStand.getUniqueId(), armorStand);
    }

    public static boolean isArmorStand(Entity entity) {
        return ArmorStandEntityTracker.armorStands.containsKey(entity.getUniqueId());
    }


    //Visual effects - usually trails around elites
    public static HashMap<UUID, Item> getItemVisualEffects() {
        return VisualEffectsEntityTracker.visualEffectEntities;
    }

    public static void registerItemVisualEffects(Item item) {
        new VisualEffectsEntityTracker(item.getUniqueId(), item);
    }

    public static boolean isItemVisualEffect(Entity entity) {
        return (getItemVisualEffects().containsKey(entity.getUniqueId()));
    }


    //NPC Entities
    public static HashMap<UUID, NPCEntity> getNPCEntities() {
        return NPCEntityTracker.npcEntities;
    }

    public static boolean registerNPCEntity(NPCEntity npc) {
        NPCEntitySpawnEvent npcEntitySpawnEvent = new NPCEntitySpawnEvent(npc.getVillager(), npc);
        new EventCaller(npcEntitySpawnEvent);
        if (npcEntitySpawnEvent.isCancelled()) return false;
        new NPCEntityTracker(npc.getVillager().getUniqueId(), npc);
        return true;
    }

    public static boolean isNPCEntity(Entity entity) {
        return getNPCEntities().containsKey(entity.getUniqueId());
    }

    public static NPCEntity getNPCEntity(Entity entity) {
        return getNPCEntities().get(entity.getUniqueId());
    }


    //Temporary blocks - blocks in powers
    public static void addTemporaryBlock(Block block, int ticks, Material replacementMaterial) {
        TemporaryBlockTracker.addTemporaryBlock(block, ticks, replacementMaterial);
    }

    public static boolean getIsTemporaryBlock(Block block) {
        return TemporaryBlockTracker.temporaryBlocks.contains(block);
    }

    public static void removeTemporaryBlock(Block block) {
        TemporaryBlockTracker.temporaryBlocks.remove(block);
    }


    //Projectile entities - Minecraft already stores data about who fired them, so just simple entities.
    //NOTE: there are no events for firing elitemobs projectiles, does not seem like it would serve a good function as of right now
    public static void registerProjectileEntity(Projectile projectile) {
        new ProjectileEntityTracker(projectile.getUniqueId(), projectile);
    }

    public static Projectile getProjectileEntity(UUID uuid) {
        return ProjectileEntityTracker.projectileEntities.get(uuid);
    }


    //Global unregister
    public static void unregister(UUID uuid, RemovalReason removalReason) {
        TrackedEntity trackedEntity = TrackedEntity.trackedEntities.get(uuid);
        if (trackedEntity != null)
            TrackedEntity.trackedEntities.get(uuid).remove(removalReason);
    }

    public static void unregister(Entity entity, RemovalReason removalReason) {
        if (entity != null)
            unregister(entity.getUniqueId(), removalReason);
    }


    //Entity wiper
    public static void wipeEntity(Entity entity, RemovalReason removalReason) {
        TrackedEntity trackedEntity = TrackedEntity.trackedEntities.get(entity.getUniqueId());
        if (trackedEntity == null) return;
        trackedEntity.doUnload(removalReason);
    }

    public static void wipeChunk(Chunk chunk, RemovalReason removalReason) {
        for (Entity entity : chunk.getEntities())
            wipeEntity(entity, removalReason);
    }

    public static void wipeWorld(World world, RemovalReason removalReason) {
        for (Chunk chunk : world.getLoadedChunks())
            wipeChunk(chunk, removalReason);
    }

    public static void wipeShutdown() {
        for (TrackedEntity trackedEntity : TrackedEntity.trackedEntities.values())
            trackedEntity.doShutdown();
        TrackedEntity.trackedEntities.clear();
        EliteEntityTracker.eliteMobEntities.clear();
        SuperMobEntityTracker.superMobEntities.clear();
        ProjectileEntityTracker.projectileEntities.clear();
        NPCEntityTracker.npcEntities.clear();
        VisualEffectsEntityTracker.visualEffectEntities.clear();
        ArmorStandEntityTracker.armorStands.clear();
        for (Block block : TemporaryBlockTracker.temporaryBlocks)
            block.setType(Material.AIR);
        TemporaryBlockTracker.temporaryBlocks.clear();
        SimplePersistentEntity.persistentEntities.clear();
        CustomBossEntity.trackableCustomBosses.clear();
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

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        TrackedEntity trackedEntity = TrackedEntity.trackedEntities.get(event.getEntity().getUniqueId());
        if (trackedEntity != null)
            trackedEntity.doDeath();
    }

    @EventHandler(ignoreCancelled = true)
    public void onMine(BlockBreakEvent event) {
        if (!getIsTemporaryBlock(event.getBlock())) return;
        event.setDropItems(false);
        removeTemporaryBlock(event.getBlock());
    }

}
