package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class SummonMerchantConfig extends EnchantmentsConfigFields {
    public SummonMerchantConfig() {
        super("summon_merchant",
                true,
                "Summon Merchant",
                1,
                10);
        super.getAdditionalConfigOptions().put("message", "Jeeves!");
    }
}
