package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;

public class EntityTracker implements Listener {

    /*
    These HashSets track basically everything for live plugin entities
     */
    private static HashMap<World, HashSet<LivingEntity>> superMobs = new HashMap<>();
    private static HashMap<World, HashSet<EliteMobEntity>> eliteMobs = new HashMap<>();

    private static HashMap<World, HashSet<LivingEntity>> naturalEntities = new HashMap<>();
    private static HashMap<World, HashSet<ArmorStand>> armorStands = new HashMap<>();
    private static HashMap<World, HashSet<Item>> itemVisualEffects = new HashMap<>();

    /*
    This HashSet shouldn't really be scanned during runtime for aside from the occasional updates, it mostly exists to
    cull entities once the server shuts down
     */
    private static HashSet<Entity> cullablePluginEntities = new HashSet<>();

    /*
    Starts tracking elite mobs
     */
    public static void registerEliteMob(EliteMobEntity eliteMobEntity) {
        genericHashMapHashSetAdder(eliteMobs, eliteMobEntity.getLivingEntity().getWorld(), eliteMobEntity);
        registerCullableEntity(eliteMobEntity.getLivingEntity());
    }

    /*
    Starts tracking super mob
     */
    public static void registerSuperMob(LivingEntity livingEntity) {
        if (!SuperMobProperties.isValidSuperMobType(livingEntity)) return;
        genericHashMapHashSetAdder(superMobs, livingEntity.getWorld(), livingEntity);
    }

    /*
    Registers mobs that spawn naturally, necessary for elite mob rewards
     */
    public static void registerNaturalEntity(LivingEntity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return;
        genericHashMapHashSetAdder(naturalEntities, entity.getWorld(), entity);
    }


    public static void registerArmorStands(ArmorStand armorStand) {
        genericHashMapHashSetAdder(armorStands, armorStand.getWorld(), armorStand);
        registerCullableEntity(armorStand);
    }

    public static void registerItemVisualEffects(Item item) {
        genericHashMapHashSetAdder(itemVisualEffects, item.getWorld(), item);
        registerCullableEntity(item);
    }

    private static HashMap genericHashMapHashSetAdder(HashMap hashMap, Object world, Object object) {

        HashSet hashSet = new HashSet();

        if (hashMap.containsKey(world))
            hashSet = (HashSet) hashMap.get(world);

        hashSet.add(object);

        hashMap.put(world, hashSet);

        return hashMap;

    }

    /*
    Starts tracking any entity generated or managed by EliteMobs, useful for when they need to be culled
    Does not include super mobs to avoid culling them by mistake
     */
    public static void registerCullableEntity(Entity entity) {
        cullablePluginEntities.add(entity);
    }

    public static void checkEntityState() {

        new BukkitRunnable() {
            @Override
            public void run() {
                updateSuperMobs();
                updateEliteMobEntities();
                updateLivingEntities();
                updateCullables();
                updateAmorStands();
                updateItems();
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 60 * 5, 20 * 60 * 5);

    }

    private static void updateSuperMobs() {
        HashMap<World, HashSet<LivingEntity>> cloneSet = (HashMap<World, HashSet<LivingEntity>>) superMobs.clone();
        for (HashSet<LivingEntity> hashSet : cloneSet.values()) {
            for (LivingEntity livingEntity : (HashSet<LivingEntity>) hashSet.clone())
                if (!livingEntity.isValid())
                    superMobs.get(livingEntity.getWorld()).remove(livingEntity);
        }
    }

    private static void updateLivingEntities() {
        HashMap<World, HashSet<LivingEntity>> cloneSet = (HashMap<World, HashSet<LivingEntity>>) naturalEntities.clone();
        for (HashSet<LivingEntity> hashSet : cloneSet.values()) {
            for (LivingEntity livingEntity : (HashSet<LivingEntity>) hashSet.clone())
                if (!livingEntity.isValid())
                    naturalEntities.get(livingEntity.getWorld()).remove(livingEntity);
        }
    }

    private static void updateEliteMobEntities() {
        HashMap<World, HashSet<EliteMobEntity>> cloneSet = (HashMap<World, HashSet<EliteMobEntity>>) eliteMobs.clone();
        for (HashSet<EliteMobEntity> hashSet : cloneSet.values())
            for (EliteMobEntity eliteMobEntity : (HashSet<EliteMobEntity>) hashSet.clone())
                if (eliteMobEntity.getLivingEntity().isDead() ||
                        !eliteMobEntity.getLivingEntity().isValid() && eliteMobEntity.getLivingEntity().getRemoveWhenFarAway())
                    eliteMobs.get(eliteMobEntity.getLivingEntity().getWorld()).remove(eliteMobEntity);
    }

    private static void updateAmorStands() {
        HashMap<World, HashSet<ArmorStand>> cloneSet = (HashMap<World, HashSet<ArmorStand>>) armorStands.clone();
        for (HashSet<ArmorStand> hashSet : cloneSet.values())
            for (ArmorStand armorStand : (HashSet<ArmorStand>) hashSet.clone())
                if (!armorStand.isValid())
                    unregisterArmorStand(armorStand);
    }

    private static void updateCullables() {
        HashSet<Entity> cloneSet = (HashSet<Entity>) cullablePluginEntities.clone();
        for (Entity entity : (HashSet<Entity>) cloneSet.clone())
            if (!entity.isValid())
                cullablePluginEntities.remove(entity);
    }

    private static void updateItems() {
        HashMap<World, HashSet<Item>> cloneSet = (HashMap<World, HashSet<Item>>) itemVisualEffects.clone();
        for (HashSet<Item> hashSet : cloneSet.values())
            for (Item item : (HashSet<Item>) hashSet.clone())
                if (!item.isValid())
                    itemVisualEffects.remove(item);
    }

    public static boolean isSuperMob(Entity entity) {
        if (!SuperMobProperties.isValidSuperMobType(entity)) return false;
        return checkLivingEntityMap(superMobs, (LivingEntity) entity);
    }

    public static boolean isEliteMob(Entity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return false;
        return checkMap(eliteMobs, (LivingEntity) entity);
    }

    public static EliteMobEntity getEliteMobEntity(Entity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return null;
        return getMap(eliteMobs, entity);
    }

    public static boolean isNaturalEntity(Entity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return false;
        return checkLivingEntityMap(naturalEntities, (LivingEntity) entity);
    }

    private static boolean checkLivingEntityMap(HashMap<World, HashSet<LivingEntity>> hashMap, LivingEntity livingEntity) {
        if (hashMap.containsKey(livingEntity.getWorld()))
            return hashMap.get(livingEntity.getWorld()).contains(livingEntity);
        return false;
    }

    public static boolean isItemVisualEffect(Entity entity) {
        if (itemVisualEffects.containsKey(entity.getWorld()))
            return itemVisualEffects.get(entity.getWorld()).contains(entity);
        return false;
    }

    private static boolean checkMap(HashMap<World, HashSet<EliteMobEntity>> hashMap, LivingEntity livingEntity) {
        if (hashMap.containsKey(livingEntity.getWorld()))
            for (EliteMobEntity eliteMobEntity : hashMap.get(livingEntity.getWorld()))
                if (eliteMobEntity.getLivingEntity().equals(livingEntity))
                    return true;
        return false;
    }

    private static EliteMobEntity getMap(HashMap<World, HashSet<EliteMobEntity>> hashMap, Entity entity) {
        if (hashMap.containsKey(entity.getWorld()))
            for (EliteMobEntity eliteMobEntity : hashMap.get(entity.getWorld()))
                if (eliteMobEntity.getLivingEntity().equals(entity))
                    return eliteMobEntity;
        return null;
    }

    public static boolean hasPower(ElitePower mobPower, Entity entity) {
        EliteMobEntity eliteMobEntity = getEliteMobEntity(entity);
        if (eliteMobEntity == null) return false;
        return eliteMobEntity.hasPower(mobPower);
    }

    public static boolean hasPower(ElitePower mobPower, EliteMobEntity eliteMobEntity) {
        if (eliteMobEntity == null) return false;
        return eliteMobEntity.hasPower(mobPower);
    }

    public static void unregisterCullableEntity(Entity entity) {
        cullablePluginEntities.remove(entity);
    }

    public static void unregisterItemEntity(Item item) {
        itemVisualEffects.get(item.getWorld()).remove(item);
    }

    /*
    Used outside this class to remove individual elite mobs
     */
    public static void unregisterEliteMob(EliteMobEntity eliteMobEntity) {
        eliteMobs.get(eliteMobEntity.getLivingEntity().getWorld()).remove(eliteMobEntity);
        naturalEntities.get(eliteMobEntity.getLivingEntity().getWorld()).remove(eliteMobEntity.getLivingEntity());
        cullablePluginEntities.remove(eliteMobEntity.getLivingEntity());
    }

    /*
    Used in this class to do mass wipes of data
     */
    private static void unregisterEliteMob(Entity entity) {
        EliteMobEntity eliteMobEntity = getEliteMobEntity(entity);
        if (eliteMobEntity == null) return;
        eliteMobs.get(eliteMobEntity.getLivingEntity().getWorld()).remove(eliteMobEntity);
    }

    public static void unregisterArmorStand(Entity armorStand) {
        if (!armorStand.getType().equals(EntityType.ARMOR_STAND)) return;
        armorStands.get(armorStand.getWorld()).remove(armorStand);
    }

    public static HashMap<World, HashSet<EliteMobEntity>> getEliteMobs() {
        return eliteMobs;
    }

    public static boolean getIsArmorStand(Entity entity) {
        if (!entity.getType().equals(EntityType.ARMOR_STAND)) return false;
        if (armorStands.containsKey(entity.getWorld()))
            return armorStands.get(entity.getWorld()).contains(entity);
        return false;
    }

    public static void unregisterSuperMob(Entity entity) {
        if (!SuperMobProperties.isValidSuperMobType(entity)) return;
        superMobs.remove(entity);
    }

    public static void unregisterNaturalEntity(Entity entity) {
        if (EliteMobProperties.isValidEliteMobType(entity)) return;
        naturalEntities.remove(entity);
    }

    /*
    Custom spawn reasons can be considered as natural spawns under specific config options
     */
    @EventHandler(priority = EventPriority.LOW)
    public void registerNaturalEntity(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL) ||
                event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM) &&
                        !ConfigValues.defaultConfig.getBoolean(DefaultConfig.STRICT_SPAWNING_RULES))
            registerNaturalEntity(event.getEntity());
    }

    /*
    Natural entities get unregistered from being natural when exploit abuse is detected from the players
     */
    public static void unregisterNaturalEntity(LivingEntity livingEntity) {
        HashSet<LivingEntity> entityHashSet = naturalEntities.get(livingEntity.getWorld());
        entityHashSet.remove(livingEntity);
        naturalEntities.put(livingEntity.getWorld(), entityHashSet);
    }

    public static void shutdownPurger() {

        for (Entity entity : cullablePluginEntities)
            entity.remove();

    }

    /*
    This is run in sync for performance reasons
     */
    public static void wipeEntity(Entity entity) {
        unregisterEliteMob(entity);
        unregisterSuperMob(entity);
        unregisterNaturalEntity(entity);
        unregisterArmorStand(entity);
        unregisterCullableEntity(entity);
    }

    public static void chunkWiper(ChunkUnloadEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : event.getChunk().getEntities())
                    wipeEntity(entity);
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    public static void deathWipe(EntityDeathEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                wipeEntity(event.getEntity());
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

}
