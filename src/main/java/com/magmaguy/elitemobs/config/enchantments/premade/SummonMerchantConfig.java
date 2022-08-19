package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class SummonMerchantConfig extends EnchantmentsConfigFields {
    public static String message;

    public SummonMerchantConfig() {
        super("summon_merchant",
                true,
                "Summon Merchant",
                1,
                10);

    }

    @Override
    public void processAdditionalFields() {
        message = ConfigurationEngine.setString(file, super.fileConfiguration, "message", "Jeeves!", true);
    }
}
