package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.CustomEnchantmentsConfig;

public class Hunter extends CustomEnchantment {

    @Override
    public String setName() {
        return ConfigValues.customEnchantmentsConfig.getString(CustomEnchantmentsConfig.HUNTER_NAME);
    }

}
