package com.magmaguy.elitemobs.utils;

import org.bukkit.Material;

import java.util.ArrayList;

public class NonSolidBlockTypes {

    private static final ArrayList<Material> nonSolidBlocks = new ArrayList<>();

    public static boolean isPassthrough(Material material) {
        return !material.isSolid();
    }

}
