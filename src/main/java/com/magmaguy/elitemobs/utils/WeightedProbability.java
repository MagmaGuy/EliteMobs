package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.items.customitems.CustomItem;

import java.util.HashMap;

public class WeightedProbability {

    public static String pickWeighedProbability(HashMap<String, Double> weighedValues) {
        double totalWeight = 0;
        for (String string : weighedValues.keySet())
            totalWeight += weighedValues.get(string);
        String selectedString = null;
        double random = Math.random() * totalWeight;
        for (String string : weighedValues.keySet()) {
            random -= weighedValues.get(string);
            if (random <= 0) {
                selectedString = string;
                break;
            }
        }
        return selectedString;
    }

    public static CustomItem pickWeighedProbabilityFromCustomItems(HashMap<CustomItem, Double> weighedValues) {
        double totalWeight = 0;
        for (CustomItem customItem : weighedValues.keySet())
            totalWeight += weighedValues.get(customItem);
        CustomItem selectedCustomItem = null;
        double random = Math.random() * totalWeight;
        for (CustomItem customItem : weighedValues.keySet()) {
            random -= weighedValues.get(customItem);
            if (random <= 0) {
                selectedCustomItem = customItem;
                break;
            }
        }
        return selectedCustomItem;
    }

}
