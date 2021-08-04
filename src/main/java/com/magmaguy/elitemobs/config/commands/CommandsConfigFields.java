package com.magmaguy.elitemobs.config.commands;

import org.bukkit.configuration.file.FileConfiguration;

public class CommandsConfigFields {

    private String fileName;

    public CommandsConfigFields(String fileName) {
        this.fileName = fileName + ".yml";
    }

    public CommandsConfigFields(FileConfiguration configuration) {

    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
    }

    public String getFileName() {
        return fileName;
    }

}
