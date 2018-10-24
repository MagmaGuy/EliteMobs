package com.magmaguy.elitemobs.items.itemconstructor;

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;

public class ScalabilityAssigner {

    public static void assign(String scalabilityType, HashMap<Enchantment, Integer> enchantments, HashMap<String, Integer> customEnchantments, String dropType){

        /*
        For the sake of clarity for the users and just overall reliability scalability will only be assigned to dynamic
        items, and all static items will be considered statically scalable
         */
        if (!DropWeightHandler.isDynamic(dropType)) return;
        if (!isScalable(scalabilityType)) return;

        if (scalabilityType == null || scalabilityType.equalsIgnoreCase("dynamic")){



        }

    }

    public static boolean isScalable (String scalabilityType) {

        return scalabilityType == null || scalabilityType.equalsIgnoreCase("dynamic") || scalabilityType.equalsIgnoreCase("dynamic_full");

    }

}
