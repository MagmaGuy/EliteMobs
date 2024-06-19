package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import org.bukkit.enchantments.Enchantment;

public class WindBurstConfig extends EnchantmentsConfigFields {
    public WindBurstConfig() {
        super("wind_burst",
                true,
                "Wind Burst",
                4,
                6,
                true,
                Enchantment.WIND_BURST.getMaxLevel());
    }
}