package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import org.bukkit.enchantments.Enchantment;

public class DensityConfig extends EnchantmentsConfigFields {
    public DensityConfig() {
        super("density",
                true,
                "Density",
                4,
                6,
                true,
                Enchantment.DENSITY.getMaxLevel());
    }
}