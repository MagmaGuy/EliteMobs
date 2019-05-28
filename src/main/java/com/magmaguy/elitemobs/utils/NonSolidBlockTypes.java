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
        nonSolidBlocks.add(Material.CAVE_AIR);
        nonSolidBlocks.add(Material.TALL_GRASS);
        nonSolidBlocks.add(Material.PAINTING);
        nonSolidBlocks.add(Material.TORCH);
        nonSolidBlocks.add(Material.STONE_BUTTON);
        nonSolidBlocks.add(Material.LEVER);
        nonSolidBlocks.add(Material.LADDER);
        nonSolidBlocks.add(Material.COBWEB);
        nonSolidBlocks.add(Material.WATER);
        nonSolidBlocks.add(Material.WHEAT);
        nonSolidBlocks.add(Material.PUMPKIN_STEM);
        nonSolidBlocks.add(Material.REDSTONE_WIRE);
        nonSolidBlocks.add(Material.REDSTONE_TORCH);
        nonSolidBlocks.add(Material.REDSTONE_WALL_TORCH);
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
        nonSolidBlocks.add(Material.ARMOR_STAND);
        nonSolidBlocks.add(Material.FIRE);
        nonSolidBlocks.add(Material.NETHER_WART_BLOCK);
        nonSolidBlocks.add(Material.SUGAR_CANE);
        nonSolidBlocks.add(Material.LILY_PAD);
        nonSolidBlocks.add(Material.ALLIUM);
        nonSolidBlocks.add(Material.AZURE_BLUET);
        nonSolidBlocks.add(Material.BLUE_ORCHID);
//        nonSolidBlocks.add(Material.CORNFLOWER);
        nonSolidBlocks.add(Material.DANDELION);
        nonSolidBlocks.add(Material.LILAC);
//        nonSolidBlocks.add(Material.LILY_OF_THE_VALLEY);
        nonSolidBlocks.add(Material.OXEYE_DAISY);
        nonSolidBlocks.add(Material.PEONY);
        nonSolidBlocks.add(Material.POPPY);
        nonSolidBlocks.add(Material.ROSE_BUSH);
        nonSolidBlocks.add(Material.SUNFLOWER);
        nonSolidBlocks.add(Material.ORANGE_TULIP);
        nonSolidBlocks.add(Material.PINK_TULIP);
        nonSolidBlocks.add(Material.RED_TULIP);
        nonSolidBlocks.add(Material.WHITE_TULIP);
//        nonSolidBlocks.add(Material.WITHER_ROSE);
    }

}
