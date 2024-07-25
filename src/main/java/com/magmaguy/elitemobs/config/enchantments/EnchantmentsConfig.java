package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.magmacore.config.CustomConfig;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Locale;

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
        String newString = LegacyValueConverter.parseEnchantment(string.replace(".yml", "")) + ".yml";
        newString = newString.toLowerCase(Locale.ROOT);
        EnchantmentsConfigFields test = enchantments.get(newString);
        if (test == null) {
            Logger.warn("Failed to find enchant file " + newString);
            new Exception().printStackTrace();
        }
        return enchantments.get(newString);
    }

    public static EnchantmentsConfigFields getEnchantment(Enchantment enchantment) {
        return getEnchantment(enchantment.getKey().getKey().toLowerCase(Locale.ROOT) + ".yml");
    }
}
