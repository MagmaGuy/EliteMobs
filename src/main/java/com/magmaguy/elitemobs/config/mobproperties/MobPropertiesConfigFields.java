package com.magmaguy.elitemobs.config.mobproperties;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.List;

public class MobPropertiesConfigFields {

    private String fileName;
    private EntityType entityType;
    private boolean isEnabled;
    private String name;
    private List<String> deathMessages;

    public MobPropertiesConfigFields(String fileName,
                                     EntityType entityType,
                                     boolean isEnabled,
                                     String name,
                                     List<String> deathMessages) {
        this.fileName = fileName + ".yml";
        this.entityType = entityType;
        this.isEnabled = isEnabled;
        this.name = name;
        this.deathMessages = deathMessages;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("deathMessages", deathMessages);
    }

    public String getFileName() {
        return fileName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getName() {
        return name;
    }

    public List<String> getDeathMessages() {
        return deathMessages;
    }

}
