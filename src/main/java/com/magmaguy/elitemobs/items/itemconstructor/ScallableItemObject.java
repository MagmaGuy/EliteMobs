package com.magmaguy.elitemobs.items.itemconstructor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;

public class ScallableItemObject {

    public String rawName;
    public Material material;
    public HashMap<Enchantment, Integer> enchantments;
    public HashMap<String, Integer> customEnchantments;
    public List<String> potionEffects;
    public List<String> lore;

    /*
    Stores fields for later use
     */
    public void initializeItemObject(String rawName, Material material, HashMap<Enchantment,
            Integer> enchantments, HashMap<String, Integer> customEnchantments, List<String> potionEffects,
                                     List<String> lore) {

        Bukkit.getLogger().info("INITIALIZING " + rawName);

        this.rawName = rawName;
        this.material = material;
        this.enchantments = enchantments;
        this.customEnchantments = customEnchantments;
        this.potionEffects = potionEffects;
        this.lore = lore;

    }

}
