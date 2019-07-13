package com.magmaguy.elitemobs.config.enchantments;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;

public class EnchantmentsConfigFields {

    private String fileName;
    private boolean isEnabled;
    private String name;
    private int maxLevel;
    private Enchantment enchantment;
    private double value;

    public EnchantmentsConfigFields(String fileName,
                                    boolean isEnabled,
                                    String name,
                                    int maxLevel,
                                    double value) {
        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
        this.name = name;
        this.maxLevel = maxLevel;
        this.value = value;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("maxLevel", maxLevel);
        fileConfiguration.addDefault("value", value);
    }

    public EnchantmentsConfigFields(FileConfiguration fileConfiguration, File file) {
        this.fileName = file.getName();
        this.isEnabled = fileConfiguration.getBoolean("isEnabled");
        this.name = fileConfiguration.getString("name");
        this.maxLevel = fileConfiguration.getInt("maxLevel");
        try {
            this.enchantment = Enchantment.getByName(this.fileName.replace(".yml", ""));
        } catch (Exception ex) {
            this.enchantment = null;
        }
        this.value = fileConfiguration.getDouble("value");
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public double getValue() {
        return value;
    }
}
