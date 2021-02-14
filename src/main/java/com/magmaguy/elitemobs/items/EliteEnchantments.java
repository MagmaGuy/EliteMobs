package com.magmaguy.elitemobs.items;

import org.bukkit.enchantments.Enchantment;

public class EliteEnchantments {

    public static boolean isPotentialEliteEnchantment(Enchantment enchantment) {
        return enchantment.getKey().equals(Enchantment.DAMAGE_ALL.getKey()) ||
                enchantment.getKey().equals(Enchantment.ARROW_DAMAGE.getKey()) ||
                enchantment.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL.getKey()) ||
                enchantment.getKey().equals(Enchantment.DAMAGE_ARTHROPODS.getKey()) ||
                enchantment.getKey().equals(Enchantment.DAMAGE_UNDEAD.getKey()) ||
                enchantment.getKey().equals(Enchantment.PROTECTION_EXPLOSIONS.getKey()) ||
                enchantment.getKey().equals(Enchantment.PROTECTION_FIRE.getKey()) ||
                enchantment.getKey().equals(Enchantment.PROTECTION_PROJECTILE.getKey()) ||
                enchantment.getKey().equals(Enchantment.THORNS.getKey());
    }

}
