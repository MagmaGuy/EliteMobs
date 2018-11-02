package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPowers;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.World;
import org.bukkit.entity.Entity;
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
    private static HashMap<World, List<LivingEntity>> passiveMobs = new HashMap<>();
    private static HashMap<World, List<EliteMobEntity>> eliteMobs = new HashMap<>();
    private static HashMap<World, List<EliteMobEntity>> bossMobs = new HashMap<>();
    private static List<Entity> allCullableEliteMobEntities = new ArrayList<>();
    private static HashMap<World, List<LivingEntity>> naturalEntities = new HashMap<>();

    /*
    Starts tracking elite mob
     */
    public static void registerEliteMob(EliteMobEntity eliteMobEntity) {
        eliteMobs = eliteMobAdder(eliteMobs, eliteMobEntity);
        registerEntity(eliteMobEntity.getLivingEntity());
    }

    /*
    Starts tracking boss mob
     */
    public static void registerBossMob(EliteMobEntity eliteMobEntity) {
        bossMobs = eliteMobAdder(bossMobs, eliteMobEntity);
        registerEntity(eliteMobEntity.getLivingEntity());
    }

    private static HashMap<World, List<EliteMobEntity>> eliteMobAdder(HashMap<World, List<EliteMobEntity>> currentHashMap, EliteMobEntity eliteMob) {

        HashMap<World, List<EliteMobEntity>> newHashMap = (HashMap<World, List<EliteMobEntity>>) currentHashMap.clone();

        if (currentHashMap.containsKey(eliteMob.getLivingEntity().getWorld())) {
            List<EliteMobEntity> eliteMobEntityList = newHashMap.get(eliteMob.getLivingEntity().getWorld());
            /*
            Check if the entity is already in the list
             */
            if (eliteMobEntityList.contains(eliteMob))
                return newHashMap;

            eliteMobEntityList.add(eliteMob);
            newHashMap.put(eliteMob.getLivingEntity().getWorld(), eliteMobEntityList);
        } else
            newHashMap.put(eliteMob.getLivingEntity().getWorld(), new ArrayList(Collections.singletonList(eliteMob)));

        return newHashMap;

    }

    /*
    Starts tracking super mob
     */
    public static void registerPassiveMob(LivingEntity livingEntity) {
        passiveMobs = livingEntityAdder(passiveMobs, livingEntity);
    }

    private static HashMap<World, List<LivingEntity>> livingEntityAdder(HashMap<World, List<LivingEntity>> currentHashMap, LivingEntity livingEntity) {

        HashMap<World, List<LivingEntity>> newHashMap = currentHashMap;

        if (currentHashMap.containsKey(livingEntity.getWorld())) {
            List<LivingEntity> currentLivingEntities = currentHashMap.get(livingEntity.getWorld());
            /*
            Check if the entity is already in the list
             */
            if (currentLivingEntities.contains(livingEntity))
                return newHashMap;
            currentLivingEntities.add(livingEntity);
            newHashMap.put(livingEntity.getWorld(), currentLivingEntities);
        } else
            newHashMap.put(livingEntity.getWorld(), new ArrayList<>(Collections.singletonList(livingEntity)));

        return newHashMap;

    }

    /*
    Starts tracking any entity generated or managed by EliteMobs, useful for when they need to be culled
    Does not include passive mobs to avoid culling them by mistake
     */
    public static void registerEntity(Entity entity) {
        allCullableEliteMobEntities.add(entity);
    }

    /*
    Registers mobs that spawn naturally, necessary for elite mob rewards
     */
    public static void registerNaturalEntity(LivingEntity entity) {
        naturalEntities = livingEntityAdder(passiveMobs, entity);
    }

    public static void checkEntityState() {

        new BukkitRunnable() {
            @Override
            public void run() {
                passiveMobs = updateValidEntities(passiveMobs);
                eliteMobs = updateValidEliteMobs(eliteMobs);
                bossMobs = updateValidEliteMobs(bossMobs);
                naturalEntities = updateValidEntities(naturalEntities);
                allCullableEliteMobEntities = checkEntityList(allCullableEliteMobEntities);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static HashMap<World, List<LivingEntity>> updateValidEntities(HashMap<World, List<LivingEntity>> livingEntityHashMap) {

        HashMap<World, List<LivingEntity>> hashMap = (HashMap<World, List<LivingEntity>>) livingEntityHashMap.clone();

        for (World world : hashMap.keySet()) {
            List<LivingEntity> livingEntityList = new ArrayList<>();
            livingEntityList.addAll(hashMap.get(world));
            for (LivingEntity livingEntity : livingEntityList)
                if (!livingEntity.isValid())
                    hashMap = livingEntitySubtractor(livingEntityHashMap, livingEntity);
        }

        return hashMap;

    }

    private static HashMap<World, List<EliteMobEntity>> updateValidEliteMobs(HashMap<World, List<EliteMobEntity>> livingEntityHashMap) {

        HashMap<World, List<EliteMobEntity>> hashMap = (HashMap<World, List<EliteMobEntity>>) livingEntityHashMap.clone();

        for (World world : livingEntityHashMap.keySet()) {
            List<EliteMobEntity> livingEntityList = new ArrayList<>();
            livingEntityList.addAll(hashMap.get(world));
            for (EliteMobEntity livingEntity : livingEntityList)
                if (!livingEntity.getLivingEntity().isValid())
                    hashMap = eliteMobsSubtractor(hashMap, livingEntity.getLivingEntity());
        }

        return hashMap;

    }

    private static HashMap<World, List<LivingEntity>> livingEntitySubtractor(HashMap<World, List<LivingEntity>> currentHashMap, LivingEntity livingEntity) {

        HashMap<World, List<LivingEntity>> newHashMap = currentHashMap;

        List<LivingEntity> currentLivingEntities = currentHashMap.get(livingEntity.getWorld());
        currentLivingEntities.remove(livingEntity);
        newHashMap.put(livingEntity.getWorld(), currentLivingEntities);

        return newHashMap;

    }

    private static HashMap<World, List<EliteMobEntity>> eliteMobsSubtractor(HashMap<World, List<EliteMobEntity>> currentHashMap, LivingEntity livingEntity) {

        HashMap<World, List<EliteMobEntity>> newHashMap = currentHashMap;

        List<EliteMobEntity> currentLivingEntities = currentHashMap.get(livingEntity.getWorld());
        currentLivingEntities.remove(livingEntity);
        newHashMap.put(livingEntity.getWorld(), currentLivingEntities);

        return newHashMap;

    }

    private static List<Entity> checkEntityList(List<Entity> entityList) {

        List<Entity> newArrayList = entityList;

        for (Entity entity : entityList)
            if (!entity.isValid())
                newArrayList.remove(entity);

        return newArrayList;

    }

    public static boolean isPassiveMob(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return checkLivingEntityMap(passiveMobs, (LivingEntity) entity);
    }

    public static boolean isEliteMob(LivingEntity livingEntity) {
        return checkMap(eliteMobs, livingEntity);
    }

    public static boolean isEliteMob(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return isEliteMob((LivingEntity) entity);
    }

    public static EliteMobEntity getEliteMobEntity(LivingEntity livingEntity) {
        return getMap(eliteMobs, livingEntity);
    }

    public static boolean isBossMob(LivingEntity livingEntity) {
        return checkMap(bossMobs, livingEntity); //todo: this returns isEliteMob
    }

    public static boolean isPluginEntity(Entity entity) {
        return allCullableEliteMobEntities.contains(entity);
    }

    public static boolean isNaturalEntity(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return checkLivingEntityMap(naturalEntities, (LivingEntity) entity);
    }

    public static boolean isCullablePluginEntity(Entity entity) {
        return allCullableEliteMobEntities.contains(entity);
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

}
