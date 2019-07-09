package com.magmaguy.elitemobs.config.customloot;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class CustomLootConfigFields {

    private static List<CustomLootConfigFields> customLootConfigFields = new ArrayList<>();

    public static List<CustomLootConfigFields> getCustomLootConfigFields() {
        return customLootConfigFields;
    }

    public static void addCustomLootConfigField(CustomLootConfigFields customLootConfig) {
        customLootConfigFields.add(customLootConfig);
    }

    private String fileName;
    private boolean isEnabled;
    private String material;
    private String name;
    private List<String> lore;
    private List<String> enchantments;
    private List<String> potionEffects;
    private String dropWeight;
    private String scalability;
    private String itemType;


    public CustomLootConfigFields(String fileName,
                                  boolean isEnabled,
                                  String material,
                                  String name,
                                  List<String> lore,
                                  List<String> enchantments,
                                  List<String> potionEffects,
                                  String dropWeight,
                                  String scalability,
                                  String itemType) {
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
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("material", material);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("lore", lore);
        fileConfiguration.addDefault("enchantments", enchantments);
        fileConfiguration.addDefault("potionEffects", potionEffects);
        fileConfiguration.addDefault("dropWeight", dropWeight);
        fileConfiguration.addDefault("scalability", scalability);
        fileConfiguration.addDefault("itemType", itemType);
    }

    public CustomLootConfigFields(String fileName, FileConfiguration configuration) {
        this.fileName = fileName;
        if (configuration.get("isEnabled") != null)
            this.isEnabled = configuration.getBoolean("isEnabled");
        else this.isEnabled = false;
        this.material = configuration.getString("material");
        this.name = configuration.getString("name");
        this.lore = configuration.getStringList("lore");
        this.enchantments = configuration.getStringList("enchantments");
        this.potionEffects = configuration.getStringList("potionEffects");
        this.dropWeight = configuration.getString("dropWeight");
        this.scalability = configuration.getString("scalability");
        this.itemType = configuration.getString("itemType");
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getMaterial() {
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

    public String getItemType() {
        return itemType;
    }
}
