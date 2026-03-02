package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class SoulbindConfig extends EnchantmentsConfigFields {
    public static String loreStrings;
    public static String hologramStrings;

    public SoulbindConfig() {
        super("soulbind",
                true,
                "Soulbind",
                1,
                10,
                true,
                1);
    }

    @Override
    public void processAdditionalFields() {
        loreStrings = translatable(filename, "loreStrings", processString("loreStrings", "&7Soulbound to &f$player", null, true));
        hologramStrings = translatable(filename, "hologramString", processString("hologramString", "$player&f's", null, true));
    }
}
