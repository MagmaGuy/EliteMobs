package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class HunterConfig extends EnchantmentsConfigFields {
    public HunterConfig() {
        super("hunter",
                true,
                "Hunter",
                3,
                10);
        super.getAdditionalConfigOptions().put("hunterSpawnBonus", 0.1);
    }
}
