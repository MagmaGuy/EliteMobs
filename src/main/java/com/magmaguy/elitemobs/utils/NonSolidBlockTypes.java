package com.magmaguy.elitemobs.utils;

import org.bukkit.Material;

import java.util.ArrayList;

public class NonSolidBlockTypes {

    private static ArrayList<Material> nonSolidBlocks = new ArrayList<>();

    public static boolean isNonSolidBlock(Material material) {
        return nonSolidBlocks.contains(material);
    }

    public static void initializeNonSolidBlocks() {
        nonSolidBlocks.add(Material.AIR);
        nonSolidBlocks.add(Material.GRASS);
        nonSolidBlocks.add(Material.LONG_GRASS);
        nonSolidBlocks.add(Material.BANNER);
        nonSolidBlocks.add(Material.PAINTING);
        nonSolidBlocks.add(Material.SIGN);
        nonSolidBlocks.add(Material.WALL_SIGN);
        nonSolidBlocks.add(Material.TORCH);
        nonSolidBlocks.add(Material.STONE_BUTTON);
        nonSolidBlocks.add(Material.WOOD_BUTTON);
        nonSolidBlocks.add(Material.LEVER);
        nonSolidBlocks.add(Material.LADDER);
        nonSolidBlocks.add(Material.WEB);
        nonSolidBlocks.add(Material.YELLOW_FLOWER);
        nonSolidBlocks.add(Material.RED_ROSE);
        nonSolidBlocks.add(Material.WATER);
        nonSolidBlocks.add(Material.SAPLING);
        nonSolidBlocks.add(Material.WHEAT);
        nonSolidBlocks.add(Material.PUMPKIN_STEM);
        nonSolidBlocks.add(Material.SNOW_BLOCK);
        nonSolidBlocks.add(Material.REDSTONE_WIRE);
        nonSolidBlocks.add(Material.REDSTONE_TORCH_ON);
        nonSolidBlocks.add(Material.REDSTONE_TORCH_OFF);
        nonSolidBlocks.add(Material.TORCH);
        nonSolidBlocks.add(Material.TRIPWIRE);
        nonSolidBlocks.add(Material.VINE);
        nonSolidBlocks.add(Material.REDSTONE_COMPARATOR_OFF);
        nonSolidBlocks.add(Material.REDSTONE_COMPARATOR_ON);
        nonSolidBlocks.add(Material.ACTIVATOR_RAIL);
        nonSolidBlocks.add(Material.RAILS);
        nonSolidBlocks.add(Material.DETECTOR_RAIL);
        nonSolidBlocks.add(Material.POWERED_RAIL);
        nonSolidBlocks.add(Material.ITEM_FRAME);
        nonSolidBlocks.add(Material.DEAD_BUSH);
        nonSolidBlocks.add(Material.RED_MUSHROOM);
        nonSolidBlocks.add(Material.BROWN_MUSHROOM);
        nonSolidBlocks.add(Material.SNOW);
        nonSolidBlocks.add(Material.SUGAR_CANE_BLOCK);
        nonSolidBlocks.add(Material.ARMOR_STAND);
        nonSolidBlocks.add(Material.LAVA);
        nonSolidBlocks.add(Material.STATIONARY_LAVA);
        nonSolidBlocks.add(Material.STATIONARY_WATER);
        nonSolidBlocks.add(Material.FIRE);
        nonSolidBlocks.add(Material.NETHER_WART_BLOCK);
    }

}
