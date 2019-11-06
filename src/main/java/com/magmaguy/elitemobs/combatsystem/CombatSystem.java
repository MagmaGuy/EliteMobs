package com.magmaguy.elitemobs.combatsystem;

import org.bukkit.entity.LivingEntity;

import java.util.HashSet;

public class CombatSystem {

    //todo: remove class, everything important's been moved out of it
    //TODO: Handle thorns damage and potion effects

    public static final double PER_LEVEL_POWER_INCREASE = 0.5;
    public static final double TARGET_HITS_TO_KILL = 7; //affects max health assignment on EliteMobEntity.java

    public static final double DIAMOND_TIER_LEVEL = 7;
    public static final double IRON_TIER_LEVEL = 6;
    public static final double STONE_CHAIN_TIER_LEVEL = 5;
    public static final double GOLD_WOOD_LEATHER_TIER_LEVEL = 3;

    private static HashSet<LivingEntity> customDamageEntity = new HashSet<>();

    public static boolean isCustomDamageEntity(LivingEntity livingEntity) {
        return customDamageEntity.contains(livingEntity);
    }

    public static void addCustomEntity(LivingEntity livingEntity) {
        customDamageEntity.add(livingEntity);
    }

    public static void removeCustomDamageEntity(LivingEntity livingEntity) {
        customDamageEntity.remove(livingEntity);
    }

}
