package com.magmaguy.elitemobs;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityTracker implements Listener {

    public static HashMap<World, LivingEntity> passiveMobs = new HashMap<>();
    public static HashMap<World, LivingEntity> eliteMobs = new HashMap<>();
    public static HashMap<World, LivingEntity> bossMobs = new HashMap<>();
    public static List<Entity> allPluginEntities = new ArrayList<>();
    public static HashMap<World, LivingEntity> naturalEntity = new HashMap<>();

    public static void registerEliteMob(LivingEntity livingEntity) {
        eliteMobs.put(livingEntity.getWorld(), livingEntity);
        allPluginEntities.add(livingEntity);
    }

    public static void registerPassiveMob(LivingEntity livingEntity) {
        passiveMobs.put(livingEntity.getWorld(), livingEntity);
        allPluginEntities.add(livingEntity);
    }

    public static void registerBossMob(LivingEntity livingEntity) {
        bossMobs.put(livingEntity.getWorld(), livingEntity);
        allPluginEntities.add(livingEntity);
    }

    public static void registerEntity(Entity entity) {
        allPluginEntities.add(entity);
    }

    public static void registerNaturalEntity(LivingEntity livingEntity) {
        naturalEntity.put(livingEntity.getWorld(), livingEntity);
    }

    @EventHandler
    public void registerNaturalEntity(EntitySpawnEvent event) {
        if (event instanceof LivingEntity)
    }

}
