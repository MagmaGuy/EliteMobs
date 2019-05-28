package com.magmaguy.elitemobs.utils;

import java.util.HashMap;

public class WeightedProbability {

    public static String pickWeighedProbability(HashMap<String, Double> weighedValues) {

        double totalWeight = 0;

        for (String string : weighedValues.keySet()) {

            totalWeight += weighedValues.get(string);

        }

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


}
