package com.magmaguy.elitemobs.config.enchantments;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EnchantmentsConfigFields {

    private final String fileName;
    private final boolean isEnabled;
    private final String name;
    private final int maxLevel;
    private Enchantment enchantment;
    private final double value;
    private final HashMap<String, Object> additionalConfigOptions = new HashMap<>();
    private FileConfiguration fileConfiguration;

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
        if (!additionalConfigOptions.isEmpty())
            fileConfiguration.addDefaults(additionalConfigOptions);
    }

    public EnchantmentsConfigFields(FileConfiguration fileConfiguration, File file) {
        this.fileName = file.getName();
        this.fileConfiguration = fileConfiguration;
        this.isEnabled = fileConfiguration.getBoolean("isEnabled");
        this.name = fileConfiguration.getString("name");
        this.maxLevel = fileConfiguration.getInt("maxLevel");
        String cleanName = this.fileName.replace(".yml", "").toUpperCase();
        try {
            this.enchantment = Enchantment.getByName(cleanName);
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

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public Map<String, Object> getAdditionalConfigOptions() {
        return additionalConfigOptions;
    }

}
