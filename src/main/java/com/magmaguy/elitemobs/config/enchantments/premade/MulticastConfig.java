package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public final class MulticastConfig extends EnchantmentsConfigFields {
    private static final double[] damage = {1, .75, .85, .70};
    private static double generationChance = .35;
    private static double rareLevelChance = .01;
    private static int rareMinimumLevel = 75;

    public MulticastConfig() { super("multicast", true, "Multicast", 3, 18, true, 3); }

    @Override public void processAdditionalFields() {
        for (int level = 1; level <= 3; level++)
            damage[level] = Math.max(.05, Math.min(1, processDouble("damageMultiplier.level" + level,
                    damage[level], damage[level], false)));
        generationChance = Math.max(0, Math.min(1, processDouble("generationChance", .35, .35, false)));
        rareLevelChance = Math.max(0, Math.min(1, processDouble("levelThreeChance", .01, .01, false)));
        rareMinimumLevel = Math.max(1, processInt("levelThreeMinimumItemLevel", 75, 75, false));
    }

    public static double damageMultiplier(int level) { return damage[Math.max(0, Math.min(3, level))]; }
    public static double generationChance() { return generationChance; }
    public static double rareLevelChance() { return rareLevelChance; }
    public static int rareMinimumLevel() { return rareMinimumLevel; }
}
