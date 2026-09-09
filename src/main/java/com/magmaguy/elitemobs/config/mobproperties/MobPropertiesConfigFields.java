package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.List;

/** Per-type elite settings. isEnabled controls natural conversion, not authored boss eligibility. */
public class MobPropertiesConfigFields {

    /** A zero default delegates base health to the spawned creature's native attribute. */
    public static final double NATIVE_BASE_HEALTH = 0.0D;

    private final String fileName;
    private final EntityType entityType;
    private final boolean isEnabled;
    private final String name;
    private final List<String> deathMessages;
    private final double baseDamage;
    private final double defaultMaxHealth;
    private final String behavior;
    private final EliteMindBodyLocomotion locomotion;

    public MobPropertiesConfigFields(String fileName,
                                     EntityType entityType,
                                     boolean isEnabled,
                                     String name,
                                     List<String> deathMessages,
                                     double baseDamage,
                                     double defaultMaxHealth) {
        this.fileName = fileName + ".yml";
        this.entityType = entityType;
        this.isEnabled = isEnabled;
        this.name = name;
        this.deathMessages = List.copyOf(deathMessages);
        this.baseDamage = baseDamage;
        this.defaultMaxHealth = defaultMaxHealth;
        this.behavior = MobTypeDefaults.behavior(entityType);
        this.locomotion = MobTypeDefaults.locomotion(entityType);
    }

    public MobPropertiesConfigFields(FileConfiguration configuration, File file, MobPropertiesConfigFields defaults) {
        this.fileName = file.getName();
        // The canonical filename owns its species; an edited key cannot corrupt another catalog entry.
        this.entityType = defaults.entityType;
        if (!entityType.name().equals(configuration.getString("entityType"))) {
            throw new IllegalArgumentException("Mob properties " + fileName + " must use entityType " + entityType);
        }
        this.isEnabled = configuration.getBoolean("isEnabled", defaults.isEnabled);
        this.name = TranslationsConfig.add(fileName, "name", configuration.getString("name", defaults.name));
        List<String> configuredMessages = configuration.getStringList("deathMessages");
        if (configuredMessages.isEmpty()) {
            Logger.warn("Mob properties " + fileName + " has no death messages; using its default messages.");
            configuredMessages = defaults.deathMessages;
        }
        this.deathMessages = List.copyOf(TranslationsConfig.add(fileName, "deathMessages", configuredMessages));
        this.baseDamage = configuration.getDouble("baseDamageV2", defaults.baseDamage);
        if (!Double.isFinite(baseDamage) || baseDamage < 0) {
            throw new IllegalArgumentException("Mob properties " + fileName + " requires finite nonnegative baseDamageV2");
        }
        this.defaultMaxHealth = defaults.defaultMaxHealth;
        String selectedBehavior = configuration.getString("behavior", defaults.behavior);
        this.behavior = selectedBehavior == null || selectedBehavior.isBlank()
                || selectedBehavior.trim().equalsIgnoreCase("native") ? null : selectedBehavior.trim();
        this.locomotion = defaults.locomotion;
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("entityType", entityType.toString());
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("deathMessages", deathMessages);
        fileConfiguration.addDefault("baseDamageV2", baseDamage);
        fileConfiguration.addDefault("behavior", behavior == null ? "native" : behavior);
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

    public double getBaseDamage() {
        return this.baseDamage;
    }

    public double getDefaultMaxHealth() {
        return defaultMaxHealth;
    }

    /** Null selects native AI. Otherwise this is a filename under behaviors/. */
    public String getBehavior() {
        return behavior;
    }

    public EliteMindBodyLocomotion getLocomotion() {
        return locomotion;
    }

}
