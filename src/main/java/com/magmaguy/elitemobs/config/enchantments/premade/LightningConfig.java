package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class LightningConfig extends EnchantmentsConfigFields {
    public static int minimumCooldown;

    public LightningConfig() {
        super("lightning",
                true,
                "Lightning",
                5,
                15,
                true,
                5);
    }

    @Override
    public void processAdditionalFields() {
        minimumCooldown = ConfigurationEngine.setInt(super.fileConfiguration, "minimumCooldownSeconds", 60 * 2);
    }
}
