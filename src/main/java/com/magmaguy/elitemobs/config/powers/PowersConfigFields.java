package com.magmaguy.elitemobs.config.powers;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PowersConfigFields {

    private final String fileName;
    private final boolean isEnabled;
    private final String name;
    private final String effect;
    private final HashMap<String, Object> additionalConfigOptions = new HashMap<>();
    private FileConfiguration configuration;

    private int powerCooldown = 0;
    private int globalCooldown = 0;

    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String name,
                              String material) {
        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
        this.name = name;
        this.effect = material;
    }

    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String name,
                              String material,
                              int powerCooldown,
                              int globalCooldown) {
        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
        this.name = name;
        this.effect = material;
        this.powerCooldown = powerCooldown;
        this.globalCooldown = globalCooldown;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("effect", effect);
        if (this.powerCooldown > 0)
            fileConfiguration.addDefault("powerCooldown", powerCooldown);
        if (this.globalCooldown > 0)
            fileConfiguration.addDefault("globalCooldown", globalCooldown);
        if (!additionalConfigOptions.isEmpty())
            fileConfiguration.addDefaults(additionalConfigOptions);
    }

    public PowersConfigFields(FileConfiguration fileConfiguration, File file) {
        this.fileName = file.getName();
        this.isEnabled = fileConfiguration.getBoolean("isEnabled");
        this.name = fileConfiguration.getString("name");
        this.effect = fileConfiguration.getString("effect");
        if (fileConfiguration.get("powerCooldown") != null)
            this.powerCooldown = fileConfiguration.getInt("powerCooldown");
        if (fileConfiguration.get("globalCooldown") != null)
            this.globalCooldown = fileConfiguration.getInt("globalCooldown");
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

    public int getGlobalCooldown() {
        return globalCooldown;
    }

    public int getPowerCooldown() {
        return powerCooldown;
    }

}
