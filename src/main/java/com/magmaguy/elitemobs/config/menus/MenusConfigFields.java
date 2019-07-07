package com.magmaguy.elitemobs.config.menus;

import org.bukkit.configuration.file.FileConfiguration;

public class MenusConfigFields {

    private String fileName;

    public MenusConfigFields(String fileName) {
        this.fileName = fileName;
    }

    public FileConfiguration generateConfigDefaults(FileConfiguration fileConfiguration) {
        return fileConfiguration;
    }

    public MenusConfigFields(FileConfiguration configuration) {

    }

    public String getFileName() {
        return fileName;
    }

}
