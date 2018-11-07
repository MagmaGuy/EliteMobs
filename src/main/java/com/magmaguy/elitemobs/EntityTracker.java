package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPowers;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EntityTracker implements Listener {

    /*
    These lists track basically everything for live plugin entities
     */
    private static HashMap<World, List<LivingEntity>> superMobs = new HashMap<>();
    public static HashMap<World, List<EliteMobEntity>> eliteMobs = new HashMap<>();
    private static HashMap<World, List<EliteMobEntity>> bossMobs = new HashMap<>();

    private static HashMap<World, List<LivingEntity>> naturalEntities = new HashMap<>();
    private static HashMap<World, List<ArmorStand>> armorStands = new HashMap<>();
    private static HashMap<World, List<Item>> itemVisualEffects = new HashMap<>();

    /*
    This list shouldn't really be scanned during runtime for aside from the occasional updates, it mostly exists to
    cull entities once the server shuts down
     */
    private static List<Entity> allCullablePluginEntities = new ArrayList<>();

    /*
    Starts tracking elite mob
     */
    public static void registerEliteMob(EliteMobEntity eliteMobEntity) {
        if (isEliteMob(eliteMobEntity.getLivingEntity())) return;
        eliteMobs = genericHashMapListAdder(eliteMobs, eliteMobEntity.getLivingEntity().getWorld(), eliteMobEntity);
        registerCullableEntity(eliteMobEntity.getLivingEntity());
    }

    /*
    Starts tracking boss mobs
     */
    public static void registerBossMob(EliteMobEntity eliteMobEntity) {
        bossMobs = genericHashMapListAdder(bossMobs, eliteMobEntity.getLivingEntity().getWorld(), eliteMobEntity);
        registerCullableEntity(eliteMobEntity.getLivingEntity());
    }

    /*
    Starts tracking super mob
     */
    public static void registerSuperMob(LivingEntity livingEntity) {
        superMobs = genericHashMapListAdder(superMobs, livingEntity.getWorld(), livingEntity);
    }

    /*
    Registers mobs that spawn naturally, necessary for elite mob rewards
     */
    public static void registerNaturalEntity(LivingEntity entity) {
        naturalEntities = genericHashMapListAdder(superMobs, entity.getWorld(), entity);
    }


    public static void registerArmorStands(ArmorStand armorStand) {
        armorStands = genericHashMapListAdder(armorStands, armorStand.getWorld(), armorStand);
        registerCullableEntity(armorStand);
    }

    public static void registerItemVisualEffectsStands(Item item) {
        itemVisualEffects = genericHashMapListAdder(itemVisualEffects, item.getWorld(), item);
        registerCullableEntity(item);
    }

    private static HashMap genericHashMapListAdder(HashMap hashMap, Object world, Object object) {

        if (hashMap.containsKey(world)) {
            ArrayList arrayList = (ArrayList) hashMap.get(world);
            /*
            Check if the entity is already in the list
             */
            if (arrayList.contains(object))
                return hashMap;
            arrayList.add(object);
            hashMap.put(world, object);
        } else
            hashMap.put(world, new ArrayList<>(Collections.singletonList(object)));

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
                checkEntityList(allCullablePluginEntities);
                updateAmorStands(armorStands);
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 60 * 5, 20 * 60 * 5);

    }

    private static void updateLivingEntities(HashMap<World, List<LivingEntity>> livingEntityHashMap) {
        for (World world : livingEntityHashMap.keySet()) {
            ArrayList<LivingEntity> livingEntityList = new ArrayList<>(livingEntityHashMap.get(world));
            for (LivingEntity livingEntity : livingEntityList)
                if (!livingEntity.isValid())
                    livingEntitySubtractor(livingEntityHashMap, livingEntityList, livingEntity);
        }
    }

    private static void livingEntitySubtractor(HashMap<World, List<LivingEntity>> currentHashMap, ArrayList<LivingEntity> livingEntities, LivingEntity livingEntity) {
        livingEntities.remove(livingEntity);
        currentHashMap.put(livingEntity.getWorld(), livingEntities);
    }

    private static void updateEliteMobEntities(HashMap<World, List<EliteMobEntity>> livingEntityHashMap) {
        for (World world : livingEntityHashMap.keySet()) {
            ArrayList<EliteMobEntity> eliteMobEntities = new ArrayList<>(livingEntityHashMap.get(world));
            for (EliteMobEntity eliteMobEntity : eliteMobEntities)
                if (!eliteMobEntity.getLivingEntity().isValid())
                    eliteMobsSubtractor(livingEntityHashMap, eliteMobEntities, eliteMobEntity);
        }
    }

    private static void eliteMobsSubtractor(HashMap<World, List<EliteMobEntity>> currentHashMap, ArrayList<EliteMobEntity> eliteMobEntities, EliteMobEntity eliteMobEntity) {
        eliteMobEntities.remove(eliteMobEntity);
        currentHashMap.put(eliteMobEntity.getLivingEntity().getWorld(), eliteMobEntities);
    }

    private static void updateAmorStands(HashMap<World, List<ArmorStand>> armorStandsHashMap) {
        for (World world : armorStandsHashMap.keySet()) {
            ArrayList<ArmorStand> armorStands = new ArrayList<>(armorStandsHashMap.get(world));
            for (ArmorStand armorStand : armorStands)
                if (!armorStand.isValid())
                    armorStandSubtractor(armorStandsHashMap, armorStands, armorStand);
        }
    }

    private static void armorStandSubtractor(HashMap<World, List<ArmorStand>> currentHashMap, ArrayList<ArmorStand> livingEntities, ArmorStand armorStand) {
        livingEntities.remove(armorStand);
        currentHashMap.put(armorStand.getWorld(), livingEntities);
    }

    private static List<Entity> checkEntityList(List<Entity> entityList) {

        List<Entity> newArrayList = entityList;

        for (Entity entity : entityList)
            if (!entity.isValid())
                newArrayList.remove(entity);

        return newArrayList;

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

    public static boolean isBossMob(LivingEntity livingEntity) {
        return checkMap(bossMobs, livingEntity); //todo: this returns isEliteMob
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

    private static boolean checkLivingEntityMap(HashMap<World, List<LivingEntity>> hashMap, LivingEntity livingEntity) {
        if (hashMap.containsKey(livingEntity.getWorld()))
            return hashMap.get(livingEntity.getWorld()).contains(livingEntity);
        return false;
    }

    private static boolean checkMap(HashMap<World, List<EliteMobEntity>> hashMap, LivingEntity livingEntity) {
        if (hashMap.containsKey(livingEntity.getWorld()))
            for (EliteMobEntity eliteMobEntity : hashMap.get(livingEntity.getWorld()))
                if (eliteMobEntity.getLivingEntity().equals(livingEntity))
                    return true;
        return false;
    }

    private static EliteMobEntity getMap(HashMap<World, List<EliteMobEntity>> hashMap, LivingEntity livingEntity) {
        if (hashMap.containsKey(livingEntity.getWorld()))
            for (EliteMobEntity eliteMobEntity : hashMap.get(livingEntity.getWorld()))
                if (eliteMobEntity.getLivingEntity().equals(livingEntity))
                    return eliteMobEntity;
        return null;
    }

    public static boolean hasPower(MajorPowers majorPowers, LivingEntity livingEntity) {
        if (!isEliteMob(livingEntity)) return false;
        return getEliteMobEntity(livingEntity).hasPower(majorPowers);
    }

    public static boolean hasPower(MajorPowers majorPowers, Entity entity) {
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
    public static void unregisterNaturalEntity(Entity livingEntity) {
        List<LivingEntity> entityList = naturalEntities.get(livingEntity.getWorld());
        entityList.remove(livingEntity);
        naturalEntities.put(livingEntity.getWorld(), entityList);
    }

    public static void shutdownPurger() {

        for (Entity entity : allCullablePluginEntities)
            entity.remove();


    }

}
