package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public interface CustomConfigFieldsInterface {
     void generateConfigDefaults(FileConfiguration fileConfiguration, File file);
     void processCustomSpawnConfigFields();
}
