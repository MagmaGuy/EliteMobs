package com.magmaguy.elitemobs.config.potioneffects;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PotionEffectsConfigFields {

    private String fileName;
    private boolean isEnabled;
    private String name;
    private PotionEffectType potionEffectType;
    private int onHitDuration;
    private double value;
    private HashMap<String, Object> additionalConfigOptions = new HashMap<>();
    private FileConfiguration configuration;

    public PotionEffectsConfigFields(String fileName,
                                     boolean isEnabled,
                                     String name,
                                     int onHitDuration,
                                     double value) {
        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
        this.name = name;
        this.onHitDuration = onHitDuration;
        this.value = value;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("onHitDuration", onHitDuration);
        fileConfiguration.addDefault("value", value);
        if (!additionalConfigOptions.isEmpty())
            fileConfiguration.addDefaults(additionalConfigOptions);
    }

    public PotionEffectsConfigFields(FileConfiguration fileConfiguration, File file) {
        this.fileName = file.getName();
        this.isEnabled = fileConfiguration.getBoolean("isEnabled");
        this.name = fileConfiguration.getString("name");
        this.onHitDuration = fileConfiguration.getInt("onHitDuration");
        this.value = fileConfiguration.getDouble("value");
        this.potionEffectType = PotionEffectType.getByName(this.fileName.toUpperCase());
        this.configuration = fileConfiguration;
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

    public PotionEffectType getPotionEffectType() {
        return potionEffectType;
    }

    public Map<String, Object> getAdditionalConfigOptions() {
        return additionalConfigOptions;
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public double getValue() {
        return value;
    }
}
