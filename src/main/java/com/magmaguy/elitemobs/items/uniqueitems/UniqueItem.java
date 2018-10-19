package com.magmaguy.elitemobs.items.uniqueitems;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.items.CustomItemConstructor;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.parserutil.CustomEnchantmentConfigParser;
import com.magmaguy.elitemobs.items.parserutil.DropWeightConfigParser;
import com.magmaguy.elitemobs.items.parserutil.EnchantmentConfigParser;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public abstract class UniqueItem {

    public abstract String definePath();

    public abstract String defineType();

    public abstract String defineName();

    public abstract List<String> defineLore();

    /*
    #defineEnchantments() also defines custom enchantments as they are written the same way into config files
    */
    public abstract List<String> defineEnchantments();

    public abstract List<String> definePotionEffects();

    public abstract String defineDropWeight();

    public void assembleConfigItem(Configuration configuration) {
        ConfigAssembler.assemble(configuration, definePath(), defineType(), defineName(), defineLore(),
                defineEnchantments(), definePotionEffects(), defineDropWeight());
    }

    public ItemStack constructItemStack() {
        HashMap<Enchantment, Integer> enchantments = EnchantmentConfigParser.parseEnchantments(ConfigValues.itemsUniqueConfig,
                "Items." + definePath());
        HashMap<String, Integer> customEnchantments = CustomEnchantmentConfigParser.parseCustomEnchantments(ConfigValues.itemsUniqueConfig,
                "Items." + definePath());
        List<String> potionEffects = CustomItemConstructor.itemPotionEffectHandler(ConfigValues.itemsUniqueConfig,
                "Items." + definePath());
        String dropType = DropWeightConfigParser.getDropType(ConfigValues.itemsUniqueConfig, "Items." + definePath());
        return ItemConstructor.constructItem(defineName(), Material.getMaterial(defineType()), enchantments, customEnchantments,
                potionEffects, defineLore(), dropType);
    }

}
