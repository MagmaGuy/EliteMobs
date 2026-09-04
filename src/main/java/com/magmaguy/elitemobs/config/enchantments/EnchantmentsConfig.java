package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.magmacore.config.CustomConfig;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EnchantmentsConfig extends CustomConfig {

    @Getter
    private static HashMap<String, EnchantmentsConfigFields> enchantments = new HashMap();
    private static final Set<String> missingEnchantmentWarnings = new HashSet<>();

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
        if (test == null && missingEnchantmentWarnings.add(newString))
            Logger.warn("Failed to find enchant file " + newString);
        return enchantments.get(newString);
    }

    public static EnchantmentsConfigFields getEnchantment(Enchantment enchantment) {
        return getEnchantment(enchantment.getKey().getKey().toLowerCase(Locale.ROOT) + ".yml");
    }
}
