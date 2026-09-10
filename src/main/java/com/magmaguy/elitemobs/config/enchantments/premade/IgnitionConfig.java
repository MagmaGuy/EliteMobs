package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public final class IgnitionConfig extends EnchantmentsConfigFields {
    private static double generationChance = .30;

    public IgnitionConfig() { super("ignition", true, "Ignition", 3, 14, true, 3); }

    @Override public void processAdditionalFields() {
        generationChance = Math.max(0, Math.min(1, processDouble("generationChance", .30, .30, true)));
        if (!Double.isFinite(generationChance)) generationChance = .30;
    }

    public static double generationChance() { return generationChance; }
}
