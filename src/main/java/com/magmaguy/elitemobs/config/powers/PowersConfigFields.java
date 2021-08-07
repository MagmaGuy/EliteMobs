package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PowersConfigFields extends CustomConfigFields {

    private String name;
    private String effect;
    private HashMap<String, Object> additionalConfigOptions = new HashMap<>();

    private int powerCooldown = 0;
    private int globalCooldown = 0;

    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String name,
                              String material) {
        super(fileName, isEnabled);
        this.name = name;
        this.effect = material;
    }

    public PowersConfigFields(String fileName,
                              boolean isEnabled,
                              String name,
                              String material,
                              int powerCooldown,
                              int globalCooldown) {
        super(fileName, isEnabled);
        this.name = name;
        this.effect = material;
        this.powerCooldown = powerCooldown;
        this.globalCooldown = globalCooldown;
    }

    public PowersConfigFields(String fileName,
                              boolean isEnabled){
        super(fileName, isEnabled);
    }

    @Override
    public void generateConfigDefaults() {
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

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled);
        this.name = processString("name", name);
        this.effect = processString("effect", effect);
        this.powerCooldown = processInt("powerCooldown", powerCooldown);
            this.globalCooldown = processInt("globalCooldown", globalCooldown);
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

    public int getGlobalCooldown() {
        return globalCooldown;
    }

    public int getPowerCooldown() {
        return powerCooldown;
    }

}
