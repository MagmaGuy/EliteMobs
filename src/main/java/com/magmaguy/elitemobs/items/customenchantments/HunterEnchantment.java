package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.CustomEnchantmentsConfig;

public class HunterEnchantment extends CustomEnchantment {

    public static String setKey(){
        return "HUNTER";
    }

    public static String setName() {
        return ConfigValues.customEnchantmentsConfig.getString(CustomEnchantmentsConfig.HUNTER_NAME);
    }

}
