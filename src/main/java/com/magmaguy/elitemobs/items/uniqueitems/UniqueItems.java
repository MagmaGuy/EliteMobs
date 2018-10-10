package com.magmaguy.elitemobs.items.uniqueitems;

import com.magmaguy.elitemobs.items.CustomItemConstructor;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public abstract class UniqueItems {

    public abstract String definePath();
    public abstract String defineType();
    public abstract String defineName();
    public abstract List<String> defineLore();
    public abstract List<String> defineEnchantments();
    public abstract List<String> definePotionEffects();
    public abstract String defineDropWeight();
    public void assembleConfigItem(Configuration configuration){
        ConfigAssembler.assemble(configuration, definePath(), defineType(), defineName(), defineLore(),
                defineEnchantments(), definePotionEffects(), defineDropWeight());
        constructItemStack();
    }
    public ItemStack itemStack;
    public void constructItemStack(){
        HashMap<Enchantment, Integer> enchantments = CustomItemConstructor.getEnchantments("Items." + definePath());
        List<String> potionEffects = CustomItemConstructor.itemPotionEffectHandler("Items." + definePath());
        itemStack = ItemConstructor.constructItem(defineName(), Material.getMaterial(defineType()), enchantments, potionEffects, defineLore());
        Bukkit.getLogger().info("EliteMobs - Added item " + itemStack.toString());
    }
    public ItemStack getItemStack(){
        return itemStack;
    }

}
