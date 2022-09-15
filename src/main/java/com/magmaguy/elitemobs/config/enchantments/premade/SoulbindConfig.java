package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class SoulbindConfig extends EnchantmentsConfigFields {
    public static String loreStrings;
    public static String hologramStrings;

    public SoulbindConfig() {
        super("soulbind",
                true,
                "Soulbind",
                1,
                10);
    }

    @Override
    public void processAdditionalFields() {
        loreStrings = ConfigurationEngine.setString(file, super.fileConfiguration, "loreStrings", "&7Soulbound to &f$player", true);
        hologramStrings = ConfigurationEngine.setString(file, super.fileConfiguration, "hologramString", "$player&f's", true);
    }
}
