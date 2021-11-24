package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import lombok.Getter;

public class CriticalStrikesConfig extends EnchantmentsConfigFields {
    @Getter
    private static String criticalHitPopup;
    @Getter
    private static String criticalHitColor;

    public CriticalStrikesConfig() {
        super("critical_strikes",
                true,
                "Critical Strikes",
                2,
                13);
    }

    @Override
    public void processAdditionalFields(){
        criticalHitPopup = ConfigurationEngine.setString(super.fileConfiguration, "criticalHitPopup", "&5Critical Hit!");
        criticalHitColor = ConfigurationEngine.setString(super.fileConfiguration, "criticalHitColor", "&5");
    }
}
