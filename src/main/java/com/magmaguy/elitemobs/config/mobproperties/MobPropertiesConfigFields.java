package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.ChatColorConverter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.List;

public class MobPropertiesConfigFields {

    private final String fileName;
    private final EntityType entityType;
    private final boolean isEnabled;
    private final String name;
    private final List<String> deathMessages;
    private final double baseDamage;

    public MobPropertiesConfigFields(String fileName,
                                     EntityType entityType,
                                     boolean isEnabled,
                                     String name,
                                     List<String> deathMessages,
                                     double baseDamage) {
        this.fileName = fileName + ".yml";
        this.entityType = entityType;
        this.isEnabled = isEnabled;
        this.name = name;
        this.deathMessages = deathMessages;
        this.baseDamage = baseDamage;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("entityType", entityType.toString());
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("deathMessages", deathMessages);
        fileConfiguration.addDefault("baseDamage", baseDamage);
    }

    public MobPropertiesConfigFields(FileConfiguration configuration, File file) {
        this.fileName = file.getName();
        this.entityType = EntityType.valueOf(configuration.getString("entityType"));
        this.isEnabled = configuration.getBoolean("isEnabled");
        this.name = configuration.getString("name");
        this.deathMessages = configuration.getStringList("deathMessages");
        this.baseDamage = configuration.getDouble("baseDamage");
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
        return ChatColorConverter.convert(name);
    }

    public List<String> getDeathMessages() {
        return deathMessages;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

}
