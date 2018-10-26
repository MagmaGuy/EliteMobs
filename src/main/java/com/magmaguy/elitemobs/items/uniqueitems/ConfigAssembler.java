package com.magmaguy.elitemobs.items.uniqueitems;

import org.bukkit.configuration.Configuration;

import java.util.List;

public class ConfigAssembler {

    public static void assemble(Configuration configuration, String path, String itemType, String itemName,
                                List<String> itemLore, List<String> enchantments,
                                List<String> potionEffects, String itemWeight, String itemScalability) {

        configuration.addDefault("Items." + path + ".Item Type", itemType);
        configuration.addDefault("Items." + path + ".Item Name", itemName);
        configuration.addDefault("Items." + path + ".Item Lore", itemLore);
        configuration.addDefault("Items." + path + ".Enchantments", enchantments);
        configuration.addDefault("Items." + path + ".Potion Effects", potionEffects);
        configuration.addDefault("Items." + path + ".Drop Weight", itemWeight);
        configuration.addDefault("Items." + path + ".Scalability", itemScalability);

    }

}
