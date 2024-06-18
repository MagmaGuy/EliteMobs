package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import org.bukkit.enchantments.Enchantment;

public class EliteEnchantments {

    public static boolean isPotentialEliteEnchantment(Enchantment enchantment) {
        if (!ItemSettingsConfig.isUseEliteEnchantments()) return false;
        return enchantment.getKey().equals(Enchantment.SHARPNESS.getKey()) ||
                enchantment.getKey().equals(Enchantment.POWER.getKey()) ||
                enchantment.getKey().equals(Enchantment.PROTECTION.getKey()) ||
                enchantment.getKey().equals(Enchantment.BANE_OF_ARTHROPODS.getKey()) ||
                enchantment.getKey().equals(Enchantment.SMITE.getKey()) ||
                enchantment.getKey().equals(Enchantment.BLAST_PROTECTION.getKey()) ||
                enchantment.getKey().equals(Enchantment.FIRE_PROTECTION.getKey()) ||
                enchantment.getKey().equals(Enchantment.PROJECTILE_PROTECTION.getKey()) ||
                enchantment.getKey().equals(Enchantment.THORNS.getKey());
    }

}
