package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class CustomEnchantment {

    /*
    Code here has been maintained as static because the key value is frequently accessed and stores a static value
    setKey() and setName() need to be rewritten in every class
     */

    public void initialize() {

        setKey();

    }

    /*
    Key is used to hide the enchantment in the lore to later ID the item, is independent from name to avoid issues with
    config changes invalidating old enchantment names
    It's also used as the value to write in for the configuration file
     */
    public String key = setKey();

    public String setKey() {
        return "placeholder key";
    }

    public String getKey() {
        return key;
    }

    /*
    Get the enchantment string for assembling configurations
     */
    public String assembleConfigString(int enchantmentLevel) {
        return key + "," + enchantmentLevel;
    }

    /*
    Check an itemstack has this enchantment
     */
    public boolean hasCustomEnchantment(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) return false;
        if (!itemStack.hasItemMeta()) return false;
        if (!itemStack.getItemMeta().hasLore()) return false;
        return hasCustomEnchantment(itemStack.getItemMeta());
    }

    public boolean hasCustomEnchantment(ItemMeta itemMeta) {
        return ItemTagger.getEnchantment(itemMeta, new NamespacedKey(MetadataHandler.PLUGIN, getKey())) != 0;
    }

    /*
    Get a custom enchantment's level
     */
    public int getCustomEnchantmentLevel(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return 0;
        if (!itemStack.getItemMeta().hasLore()) return 0;
        return getCustomEnchantmentLevel(itemStack.getItemMeta().getLore());
    }

    public int getCustomEnchantmentLevel(List<String> lore) {

        for (String string : lore.get(0).replace("§", "").split(","))
            if (string.contains(key))
                return Integer.parseInt(string.split(":")[1]);

        return 0;

    }

}
