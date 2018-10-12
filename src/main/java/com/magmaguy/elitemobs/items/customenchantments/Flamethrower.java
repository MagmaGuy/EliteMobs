package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.CustomEnchantmentsConfig;

public class Flamethrower extends CustomEnchantment {

    public static String setKey() {
        return "FLAMETHROWER";
    }

    public static String setName() {
        return ConfigValues.customEnchantmentsConfig.getString(CustomEnchantmentsConfig.FLAMETHROWER_NAME);
    }

}
