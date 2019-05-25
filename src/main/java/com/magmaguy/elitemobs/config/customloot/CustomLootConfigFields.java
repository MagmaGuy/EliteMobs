package com.magmaguy.elitemobs.config.customloot;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomLootConfigFields {

    /**
     * Used to get a CustomLootConfigFields from an item's filename
     *
     * @param fileName
     * @return
     */
    public static CustomLootConfigFields getCustomLootConfigFields(String fileName) {
        return new CustomLootConfigFields(CustomLootConfig.getCustomLootConfig(fileName));
    }

    public enum ItemType {
        CUSTOM,
        UNIQUE
    }

    private String fileName;
    private boolean isEnabled;
    private Material material;
    private String name;
    private List<String> lore;
    private List<String> enchantments;
    private List<String> potionEffects;
    private String dropWeight;
    private String scalability;
    private ItemType itemType;


    public CustomLootConfigFields(String fileName,
                                  boolean isEnabled,
                                  Material material,
                                  String name,
                                  List<String> lore,
                                  List<String> enchantments,
                                  List<String> potionEffects,
                                  String dropWeight,
                                  String scalability,
                                  ItemType itemType) {
        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.enchantments = enchantments;
        this.potionEffects = potionEffects;
        this.dropWeight = dropWeight;
        this.scalability = scalability;
        this.itemType = itemType;
    }

    /**
     * Generates config defaults to be used by CustomBossesConfig
     */
    public Map<String, Object> generateConfigDefaults() {
        Map<String, Object> configDefaults = new HashMap<>();
        configDefaults.put("isEnabled", isEnabled);
        configDefaults.put("material", material);
        configDefaults.put("name", name);
        configDefaults.put("lore", lore);
        configDefaults.put("enchantments", enchantments);
        configDefaults.put("potionEffects", potionEffects);
        configDefaults.put("dropWeight", dropWeight);
        configDefaults.put("scalability", scalability);
        configDefaults.put("itemType", itemType);
        return configDefaults;
    }

    public CustomLootConfigFields(FileConfiguration configuration) {
        this.fileName = configuration.getName();
        if (configuration.get("isEnabled") != null)
            this.isEnabled = configuration.getBoolean("isEnabled");
        else this.isEnabled = false;
        this.material = Material.getMaterial(configuration.getString("material"));
        this.name = configuration.getString("name");
        this.lore = configuration.getStringList("lore");
        this.enchantments = configuration.getStringList("enchantments");
        this.potionEffects = configuration.getStringList("potionEffects");
        this.dropWeight = configuration.getString("dropWeight");
        this.scalability = configuration.getString("scalability");
        this.itemType = ItemType.valueOf(configuration.getString("itemType"));
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getEnchantments() {
        return enchantments;
    }

    public List<String> getPotionEffects() {
        return potionEffects;
    }

    public String getDropWeight() {
        return dropWeight;
    }

    public String getScalability() {
        return scalability;
    }

    public ItemType getItemType() {
        return itemType;
    }
}
