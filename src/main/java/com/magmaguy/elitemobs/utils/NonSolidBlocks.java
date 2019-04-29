package com.magmaguy.elitemobs.utils;

import org.bukkit.Material;

import java.util.HashSet;

public class NonSolidBlocks {

    private static HashSet<Material> nonSolidBlocks = new HashSet<>();

    public static HashSet<Material> getNonSolidBlocks() {
        return nonSolidBlocks;
    }

    public static boolean isNonSolidBlock(Material material) {
        return nonSolidBlocks.contains(material);
    }

    public static void intializeNonSolidBlocks() {
//        nonSolidBlocks.add(Material.AIR);
//        nonSolidBlocks.add(Material.WATER);
//        nonSolidBlocks.add(Material.STATIONARY_WATER);
//        nonSolidBlocks.add(Material.LAVA);
//        nonSolidBlocks.add(Material.STATIONARY_LAVA);
//        nonSolidBlocks.add(Material.LONG_GRASS);
//        nonSolidBlocks.add(Material.YELLOW_FLOWER);
//        nonSolidBlocks.add(Material.RED_ROSE);
//        nonSolidBlocks.add(Material.REDSTONE);
//        nonSolidBlocks.add(Material.SUGAR_CANE_BLOCK);
//        nonSolidBlocks.add(Material.WHEAT);
//        nonSolidBlocks.add(Material.WEB);
//        nonSolidBlocks.add(Material.TORCH);
//        nonSolidBlocks.add(Material.REDSTONE_TORCH_ON);
//        nonSolidBlocks.add(Material.REDSTONE_TORCH_OFF);
//        nonSolidBlocks.add(Material.BANNER);
//        nonSolidBlocks.add(Material.STANDING_BANNER);
//        nonSolidBlocks.add(Material.WALL_BANNER);
//        nonSolidBlocks.add(Material.RAILS);
//        nonSolidBlocks.add(Material.ACTIVATOR_RAIL);
//        nonSolidBlocks.add(Material.DETECTOR_RAIL);
//        nonSolidBlocks.add(Material.POWERED_RAIL);
//        nonSolidBlocks.add(Material.ITEM_FRAME);
//        nonSolidBlocks.add(Material.NETHER_WARTS);
//        nonSolidBlocks.add(Material.PUMPKIN_STEM);
//        nonSolidBlocks.add(Material.MELON_STEM);
//        nonSolidBlocks.add(Material.VINE);
//        nonSolidBlocks.add(Material.TRIPWIRE);
//        nonSolidBlocks.add(Material.TRIPWIRE_HOOK);
//        nonSolidBlocks.add(Material.ARMOR_STAND);
//        nonSolidBlocks.add(Material.BREWING_STAND);
//        nonSolidBlocks.add(Material.BROWN_MUSHROOM);
//        nonSolidBlocks.add(Material.RED_MUSHROOM);
//        nonSolidBlocks.add(Material.LADDER);
    }

}
