package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;

public abstract class SuperMobProperties extends PluginMobProperties {

    public static HashSet<EntityType> superMobTypeList = new HashSet<>();
    public static HashSet<SuperMobProperties> superMobData = new HashSet<>();

    public static void initializeSuperMobValues() {
        SuperChicken superChicken = new SuperChicken();
        SuperCow superCow = new SuperCow();
        SuperMushroomCow superMushroomCow = new SuperMushroomCow();
        SuperPig superPig = new SuperPig();
        SuperSheep superSheep = new SuperSheep();
    }

    public static boolean isValidSuperMobType(EntityType entityType) {
        return superMobTypeList.contains(entityType);
    }

    public static boolean isValidSuperMobType(Entity entity) {
        if (entity instanceof LivingEntity)
            return isValidSuperMobType(entity.getType());
        return false;
    }

    public static SuperMobProperties getDataInstance(EntityType entityType) {

        for (SuperMobProperties superMobType : superMobData)
            if (superMobType.getEntityType().equals(entityType))
                return superMobType;

        Bukkit.getLogger().warning("[EliteMobs] Something is wrong with the Super Mob data, notify the dev!");
        return null;

    }

    public static SuperMobProperties getDataInstance(Entity entity) {
        return getDataInstance(entity.getType());
    }

    public static double getSuperMobMaxHealth(Entity entity) {
        return getDataInstance(entity.getType()).getSuperMobMaxHealth();
    }

}
