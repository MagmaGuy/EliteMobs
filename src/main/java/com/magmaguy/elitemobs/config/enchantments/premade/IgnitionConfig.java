package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public final class IgnitionConfig extends EnchantmentsConfigFields {
    private static int baseTicks = 20;
    private static int ticksPerLevel = 20;
    private static double generationChance = .30;

    public IgnitionConfig() { super("ignition", true, "Ignition", 3, 14, true, 3); }

    @Override public void processAdditionalFields() {
        baseTicks = Math.max(0, Math.min(200, processInt("baseFireTicks", 20, 20, false)));
        ticksPerLevel = Math.max(0, Math.min(60, processInt("fireTicksPerLevel", 20, 20, false)));
        generationChance = Math.max(0, Math.min(1, processDouble("generationChance", .30, .30, false)));
    }

    public static int fireTicks(int level) { return level <= 0 ? 0 : Math.min(200, baseTicks + ticksPerLevel * Math.min(3, level)); }
    public static double generationChance() { return generationChance; }
}
