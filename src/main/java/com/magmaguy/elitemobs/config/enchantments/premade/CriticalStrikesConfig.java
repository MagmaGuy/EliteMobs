package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class CriticalStrikesConfig extends EnchantmentsConfigFields {
    public CriticalStrikesConfig() {
        super("critical_strikes",
                true,
                "Critical Strikes",
                2,
                13);
        super.getAdditionalConfigOptions().put("criticalHitPopup", "&5Critical Hit!");
        super.getAdditionalConfigOptions().put("criticalHitColor", "&5");
    }
}
