package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public final class BlastRadiusConfig extends EnchantmentsConfigFields {
    private static double radiusPerLevel = .15;
    private static double generationChance = .30;

    public BlastRadiusConfig() { super("blast_radius", true, "Blast Radius", 3, 14, true, 3); }

    @Override public void processAdditionalFields() {
        radiusPerLevel = Math.max(0, Math.min(1D / 3D, processDouble("radiusIncreasePerLevel", .15, .15, true)));
        generationChance = Math.max(0, Math.min(1, processDouble("generationChance", .30, .30, true)));
        if (!Double.isFinite(radiusPerLevel)) radiusPerLevel = .15;
        if (!Double.isFinite(generationChance)) generationChance = .30;
    }

    public static double radiusMultiplier(int level) { return 1 + radiusPerLevel * Math.max(0, Math.min(3, level)); }
    public static double generationChance() { return generationChance; }
}
