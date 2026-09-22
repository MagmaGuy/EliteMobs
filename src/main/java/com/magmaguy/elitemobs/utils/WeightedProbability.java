package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.items.customitems.CustomItem;

import java.util.HashMap;

public class WeightedProbability {

    public static String pickWeighedProbability(HashMap<String, Double> weighedValues) {
        return pick(weighedValues);
    }

    public static CustomItem pickWeighedProbabilityFromCustomItems(HashMap<CustomItem, Double> weighedValues) {
        return pick(weighedValues);
    }

    /** Normalize by the largest weight so a finite authored distribution cannot overflow its sum. */
    public static <T> T pick(java.util.Map<T, Double> weights) {
        double scale = 0;
        for (double weight : weights.values())
            if (Double.isFinite(weight) && weight > scale) scale = weight;
        if (scale == 0) return null;
        double total = 0;
        for (double weight : weights.values())
            if (Double.isFinite(weight) && weight > 0) total += weight / scale;
        double selected = java.util.concurrent.ThreadLocalRandom.current().nextDouble(total);
        T last = null;
        for (var entry : weights.entrySet()) {
            double weight = entry.getValue();
            if (!Double.isFinite(weight) || weight <= 0) continue;
            last = entry.getKey();
            selected -= weight / scale;
            if (selected < 0) return last;
        }
        return last; // Floating-point accumulation can leave a final sub-ulp remainder.
    }
}
