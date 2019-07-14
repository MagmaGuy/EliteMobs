package com.magmaguy.elitemobs.config.powers;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PowersConfigFields {

    private String fileName;
    private boolean isEnabled;
    private String name;
    private String effect;
    private HashMap<String, Object> additionalConfigOptions = new HashMap<>();
    private FileConfiguration configuration;

    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String name,
                              String material) {
        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
        this.name = name;
        this.effect = material;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("effect", effect);
        if (additionalConfigOptions != null)
            fileConfiguration.addDefaults(additionalConfigOptions);
    }

    public PowersConfigFields(FileConfiguration fileConfiguration, File file) {
        this.fileName = file.getName();
        this.isEnabled = fileConfiguration.getBoolean("isEnabled");
        this.name = fileConfiguration.getString("name");
        this.effect = fileConfiguration.getString("effect");
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

    public String getEffect() {
        return effect;
    }

    public Map<String, Object> getAdditionalConfigOptions() {
        return additionalConfigOptions;
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }
}
