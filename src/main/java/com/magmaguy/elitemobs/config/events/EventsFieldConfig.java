package com.magmaguy.elitemobs.config.events;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class EventsFieldConfig {

    private final String fileName;
    private final boolean isEnabled;

    public EventsFieldConfig(String fileName,
                             boolean isEnabled) {
        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", this.isEnabled);
    }

    public EventsFieldConfig(FileConfiguration configuration, File file) {
        this.fileName = file.getName();
        this.isEnabled = configuration.getBoolean("isEnabled");
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

}
