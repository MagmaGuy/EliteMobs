package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class HunterConfig extends EnchantmentsConfigFields {
    public static double hunterSpawnBonus;

    public HunterConfig() {
        super("hunter",
                true,
                "Hunter",
                3,
                10,
                true,
                3);
    }

    @Override
    public void processAdditionalFields() {
        hunterSpawnBonus = ConfigurationEngine.setDouble(super.fileConfiguration, "hunterSpawnBonus", 0.05);
    }
}
