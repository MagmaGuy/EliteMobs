package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import org.bukkit.enchantments.Enchantment;

public class BreachConfig extends EnchantmentsConfigFields {
    public BreachConfig() {
        super("breach",
                true,
                "Breach",
                4,
                6,
                true,
                Enchantment.BREACH.getMaxLevel());
    }
}