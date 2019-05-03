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
        nonSolidBlocks.add(Material.TALL_GRASS);
        nonSolidBlocks.add(Material.PAINTING);
        nonSolidBlocks.add(Material.TORCH);
        nonSolidBlocks.add(Material.STONE_BUTTON);
        nonSolidBlocks.add(Material.LEVER);
        nonSolidBlocks.add(Material.LADDER);
        nonSolidBlocks.add(Material.COBWEB);
        nonSolidBlocks.add(Material.SUNFLOWER);
        nonSolidBlocks.add(Material.ROSE_BUSH);
        nonSolidBlocks.add(Material.WATER);
        nonSolidBlocks.add(Material.WHEAT);
        nonSolidBlocks.add(Material.PUMPKIN_STEM);
        nonSolidBlocks.add(Material.SNOW_BLOCK);
        nonSolidBlocks.add(Material.REDSTONE_WIRE);
        nonSolidBlocks.add(Material.TORCH);
        nonSolidBlocks.add(Material.TRIPWIRE);
        nonSolidBlocks.add(Material.VINE);
        nonSolidBlocks.add(Material.ACTIVATOR_RAIL);
        nonSolidBlocks.add(Material.DETECTOR_RAIL);
        nonSolidBlocks.add(Material.POWERED_RAIL);
        nonSolidBlocks.add(Material.ITEM_FRAME);
        nonSolidBlocks.add(Material.DEAD_BUSH);
        nonSolidBlocks.add(Material.RED_MUSHROOM);
        nonSolidBlocks.add(Material.BROWN_MUSHROOM);
        nonSolidBlocks.add(Material.SNOW);
        nonSolidBlocks.add(Material.ARMOR_STAND);
        nonSolidBlocks.add(Material.FIRE);
        nonSolidBlocks.add(Material.NETHER_WART_BLOCK);
    }

}
