package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import org.bukkit.enchantments.Enchantment;

public class LungeConfig extends EnchantmentsConfigFields {
    public LungeConfig() {
        super("lunge",
                true,
                "Lunge",
                3,
                6,
                true,
                Enchantment.LUNGE.getMaxLevel());
    }
}
