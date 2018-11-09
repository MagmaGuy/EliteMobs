package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPower;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;

public class EntityTracker implements Listener {

    /*
    These HashSets track basically everything for live plugin entities
     */
    private static HashMap<World, HashSet<LivingEntity>> superMobs = new HashMap<>();
    public static HashMap<World, HashSet<EliteMobEntity>> eliteMobs = new HashMap<>();

    private static HashMap<World, HashSet<LivingEntity>> naturalEntities = new HashMap<>();
    private static HashMap<World, HashSet<ArmorStand>> armorStands = new HashMap<>();
    private static HashMap<World, HashSet<Item>> itemVisualEffects = new HashMap<>();

    /*
    This HashSet shouldn't really be scanned during runtime for aside from the occasional updates, it mostly exists to
    cull entities once the server shuts down
     */
    private static HashSet<Entity> allCullablePluginEntities = new HashSet<>();

    /*
    Starts tracking elite mobs
     */
    public static void registerEliteMob(EliteMobEntity eliteMobEntity) {
        eliteMobs = genericHashMapHashSetAdder(eliteMobs, eliteMobEntity.getLivingEntity().getWorld(), eliteMobEntity);
        registerCullableEntity(eliteMobEntity.getLivingEntity());
    }

    /*
    Starts tracking super mob
     */
    public static void registerSuperMob(LivingEntity livingEntity) {
        superMobs = genericHashMapHashSetAdder(superMobs, livingEntity.getWorld(), livingEntity);
    }

    /*
    Registers mobs that spawn naturally, necessary for elite mob rewards
     */
    public static void registerNaturalEntity(LivingEntity entity) {
        naturalEntities = genericHashMapHashSetAdder(superMobs, entity.getWorld(), entity);
    }


    public static void registerArmorStands(ArmorStand armorStand) {
        armorStands = genericHashMapHashSetAdder(armorStands, armorStand.getWorld(), armorStand);
        registerCullableEntity(armorStand);
    }

    public static void registerItemVisualEffects(Item item) {
        itemVisualEffects = genericHashMapHashSetAdder(itemVisualEffects, item.getWorld(), item);
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
        allCullablePluginEntities.add(entity);
    }

    public static void checkEntityState() {

        new BukkitRunnable() {
            @Override
            public void run() {
                updateLivingEntities(superMobs);
                updateEliteMobEntities(eliteMobs);
                updateLivingEntities(naturalEntities);
                checkEntityHashSet(allCullablePluginEntities);
                updateAmorStands(armorStands);
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 60 * 5, 20 * 60 * 5);

    }

    private static void updateLivingEntities(HashMap<World, HashSet<LivingEntity>> livingEntityHashMap) {
        for (World world : livingEntityHashMap.keySet()) {
            HashSet<LivingEntity> livingEntityHashSet = new HashSet<>(livingEntityHashMap.get(world));
            for (LivingEntity livingEntity : livingEntityHashSet)
                if (!livingEntity.isValid())
                    livingEntitySubtractor(livingEntityHashMap, livingEntityHashSet, livingEntity);
        }
    }

    private static void livingEntitySubtractor(HashMap<World, HashSet<LivingEntity>> currentHashMap, HashSet<LivingEntity> livingEntities, LivingEntity livingEntity) {
        livingEntities.remove(livingEntity);
        currentHashMap.put(livingEntity.getWorld(), livingEntities);
    }

    private static void updateEliteMobEntities(HashMap<World, HashSet<EliteMobEntity>> livingEntityHashMap) {
        for (World world : livingEntityHashMap.keySet()) {
            HashSet<EliteMobEntity> eliteMobEntities = new HashSet<>(livingEntityHashMap.get(world));
            for (EliteMobEntity eliteMobEntity : eliteMobEntities)
                if (!eliteMobEntity.getLivingEntity().isValid())
                    eliteMobsSubtractor(livingEntityHashMap, eliteMobEntities, eliteMobEntity);
        }
    }

    private static void eliteMobsSubtractor(HashMap<World, HashSet<EliteMobEntity>> currentHashMap, HashSet<EliteMobEntity> eliteMobEntities, EliteMobEntity eliteMobEntity) {
        eliteMobEntities.remove(eliteMobEntity);
        currentHashMap.put(eliteMobEntity.getLivingEntity().getWorld(), eliteMobEntities);
    }

    private static void updateAmorStands(HashMap<World, HashSet<ArmorStand>> armorStandsHashMap) {
        for (World world : armorStandsHashMap.keySet()) {
            HashSet<ArmorStand> armorStands = new HashSet<>(armorStandsHashMap.get(world));
            for (ArmorStand armorStand : armorStands)
                if (!armorStand.isValid())
                    armorStandSubtractor(armorStandsHashMap, armorStands, armorStand);
        }
    }

    private static void armorStandSubtractor(HashMap<World, HashSet<ArmorStand>> currentHashMap, HashSet<ArmorStand> livingEntities, ArmorStand armorStand) {
        livingEntities.remove(armorStand);
        currentHashMap.put(armorStand.getWorld(), livingEntities);
    }

    private static HashSet<Entity> checkEntityHashSet(HashSet<Entity> entityHashSet) {

        HashSet<Entity> newHashSet = entityHashSet;

        for (Entity entity : entityHashSet)
            if (!entity.isValid())
                newHashSet.remove(entity);

        return newHashSet;

    }

    public static boolean isSuperMob(Entity entity) {
        if (!SuperMobProperties.isValidSuperMobType(entity)) return false;
        return checkLivingEntityMap(superMobs, (LivingEntity) entity);
    }

    public static boolean isEliteMob(Entity entity) {
        if (!EliteMobProperties.isValidEliteMobType(entity)) return false;
        return checkMap(eliteMobs, (LivingEntity) entity);
    }

    public static EliteMobEntity getEliteMobEntity(LivingEntity livingEntity) {
        return getMap(eliteMobs, livingEntity);
    }

    public static boolean isPluginEntity(Entity entity) {
        return allCullablePluginEntities.contains(entity);
    }

    public static boolean isNaturalEntity(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return checkLivingEntityMap(naturalEntities, (LivingEntity) entity);
    }

    public static boolean isCullablePluginEntity(Entity entity) {
        return allCullablePluginEntities.contains(entity);
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

    private static EliteMobEntity getMap(HashMap<World, HashSet<EliteMobEntity>> hashMap, LivingEntity livingEntity) {
        if (hashMap.containsKey(livingEntity.getWorld()))
            for (EliteMobEntity eliteMobEntity : hashMap.get(livingEntity.getWorld()))
                if (eliteMobEntity.getLivingEntity().equals(livingEntity))
                    return eliteMobEntity;
        return null;
    }

    public static boolean hasPower(MajorPower majorPowers, LivingEntity livingEntity) {
        if (!isEliteMob(livingEntity)) return false;
        return getEliteMobEntity(livingEntity).hasPower(majorPowers);
    }

    public static boolean hasPower(MajorPower majorPowers, Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return hasPower(majorPowers, (LivingEntity) entity);
    }

    public static boolean hasPower(MinorPower minorPower, LivingEntity livingEntity) {
        if (!isEliteMob(livingEntity)) return false;
        return getEliteMobEntity(livingEntity).hasPower(minorPower);
    }

    public static boolean hasPower(MinorPower minorPower, Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return hasPower(minorPower, (LivingEntity) entity);
    }

    public static void unregisterCullableEntity(Entity entity) {
        allCullablePluginEntities.remove(entity);
    }

    public static void unregisterItemEntity(Item item) {
        itemVisualEffects.get(item.getWorld()).remove(item);
    }

    /*
    Custom spawn reasons can be considered as natural spawns under specific config options
     */
    @EventHandler
    public void registerNaturalEntity(CreatureSpawnEvent event) {
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL))
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

        for (Entity entity : allCullablePluginEntities)
            entity.remove();

    }

}
