package com.magmaguy.elitemobs.config.enchantments;

import com.magmaguy.elitemobs.config.CustomConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
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
        EnchantmentsConfigFields test = enchantments.get(string);
        if (test == null) {
            new WarningMessage("Failed to find enchant file " + string);
            new Exception().printStackTrace();
        }
        return enchantments.get(string);
    }

    public static EnchantmentsConfigFields getEnchantment(Enchantment enchantment) {
        return getEnchantment(enchantment.getKey().getKey().toLowerCase(Locale.ROOT) + ".yml");
    }
}
