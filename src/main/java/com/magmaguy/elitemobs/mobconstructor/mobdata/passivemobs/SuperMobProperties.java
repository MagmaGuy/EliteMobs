package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class SuperMobProperties extends PluginMobProperties {

    public static List<EntityType> superMobTypeList = new ArrayList<>();
    public static List<SuperMobProperties> superMobData = new ArrayList<>();

    public static void initializeSuperMobValues() {
        SuperChicken superChicken = new SuperChicken();
        superMobData.add(superChicken);
        SuperCow superCow = new SuperCow();
        superMobData.add(superCow);
        SuperMushroomCow superMushroomCow = new SuperMushroomCow();
        superMobData.add(superMushroomCow);
        SuperPig superPig = new SuperPig();
        superMobData.add(superPig);
        SuperSheep superSheep = new SuperSheep();
        superMobData.add(superSheep);
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

}
