package com.magmaguy.elitemobs.items.parserutil;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class EnchantmentConfigParser {

    public static HashMap<Enchantment, Integer> parseEnchantments(Configuration configuration, String previousPath) {

        String path = automatedStringBuilder(previousPath, "Enchantments");
        List enchantments = configuration.getList(path);
        HashMap enchantmentMap = new HashMap();

        if (enchantments.isEmpty()) return enchantmentMap;

        try {
            for (Object object : enchantments) {

                String string = (String) object;

//                Custom enchantments have their own parser
                if (CustomEnchantmentConfigParser.isCustomEnchantment(string)) continue;

                Enchantment enchantment = Enchantment.getByName(string.split(",")[0]);

                if (enchantment != null)
                    enchantmentMap.put(enchantment, Integer.parseInt(string.split(",")[1]));

            }

        } catch (Exception e) {
            Bukkit.getLogger().warning("Warning: something on ItemsCustomLootList.yml is invalid.");
            Bukkit.getLogger().warning("Make sure you add a valid enchantment type and a valid level for it!");
        }

        return enchantmentMap;

    }


}
