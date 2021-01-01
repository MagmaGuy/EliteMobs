package com.magmaguy.elitemobs.combatsystem;

public class CombatSystem {

    //todo: remove class, everything important's been moved out of it
    //TODO: Handle thorns damage and potion effects

    public static final double PER_LEVEL_POWER_INCREASE = 0.5;

    /**
     * This determines how many hits it takes to kill a boss.
     * Unfortunately, since it's flat, at a scale, all bosses tend to end up having the same amount of life.
     * todo: make the increase based off of a % of the boss max health instead to bypass this issue, potentially
     * It accomplishes this by adding this flat amount of health to bosses.
     */
    public static final double TARGET_HITS_TO_KILL = 7D; //affects max health assignment on EliteMobEntity.java

    //this sets the tier of various materials
    public static final int TRIDENT_TIER_LEVEL = 9;
    public static final int NETHERITE_TIER_LEVEL = 8;
    public static final int DIAMOND_TIER_LEVEL = 7;
    public static final int IRON_TIER_LEVEL = 6;
    public static final int STONE_CHAIN_TIER_LEVEL = 5;
    public static final int GOLD_WOOD_LEATHER_TIER_LEVEL = 3;

    //this bypasses the damage system, outputting a raw damage value
    public static boolean bypass = false;

}
