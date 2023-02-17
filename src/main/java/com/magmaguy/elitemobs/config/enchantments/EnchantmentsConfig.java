package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.CustomConfig;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;

public class EnchantmentsConfig extends CustomConfig {

    @Getter
    private static HashMap<String, EnchantmentsConfigFields> enchantments = new HashMap();

    public EnchantmentsConfig() {
        super("enchantments", "com.magmaguy.elitemobs.config.enchantments.premade", EnchantmentsConfigFields.class);
        enchantments = new HashMap<>();
        for (String key : super.getCustomConfigFieldsHashMap().keySet())
            enchantments.put(key, (EnchantmentsConfigFields) super.getCustomConfigFieldsHashMap().get(key));
    }

    public static EnchantmentsConfigFields getEnchantment(String string) {
        return enchantments.get(string);
    }

    public static EnchantmentsConfigFields getEnchantment(Enchantment enchantment) {
        return getEnchantment(enchantment.getName().toLowerCase() + ".yml");
    }
}
