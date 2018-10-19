package com.magmaguy.elitemobs.items.parserutil;

import com.magmaguy.elitemobs.items.customenchantments.CustomEnchantmentCache;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.List;

import static com.magmaguy.elitemobs.items.parserutil.ConfigPathBuilder.automatedStringBuilder;

public class CustomEnchantmentConfigParser {

    public static HashMap<String, Integer> parseCustomEnchantments(Configuration configuration, String previousPath) {

        String path = automatedStringBuilder(previousPath, "Enchantments");
        List enchantments = configuration.getList(path);
        HashMap<String, Integer> customEnchantmentMap = new HashMap();

        try {
            for (Object object : enchantments) {

                String string = (String) object;

//                Avoid parsing a regular enchantment
                if (!CustomEnchantmentConfigParser.isCustomEnchantment(string)) continue;

                String customEnchantment = string.split(",")[0];

                customEnchantmentMap.put(customEnchantment, Integer.parseInt(string.split(",")[1]));

            }

        } catch (Exception e) {
//            Already handled in the normal enchantment parser
        }

        return customEnchantmentMap;

    }

    public static boolean isCustomEnchantment(String string) {

        if (string.contains(CustomEnchantmentCache.hunterEnchantment.getKey())) return true;
        if (string.contains(CustomEnchantmentCache.flamethrowerEnchantment.getKey())) return true;

        return false;

    }

}
