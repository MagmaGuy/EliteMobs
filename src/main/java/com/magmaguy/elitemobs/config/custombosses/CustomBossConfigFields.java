package com.magmaguy.elitemobs.config.custombosses;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class CustomBossConfigFields {

    private String fileName;
    private String entityType;
    private boolean isEnabled;
    private String name;
    private String level;
    private int timeout;
    private Boolean isPersistent;
    private double healthMultiplier;
    private double damageMultiplier;
    private Material helmet;
    private Material chestplate;
    private Material leggings;
    private Material boots;
    private Material mainHand;
    private Material offHand;
    private boolean isBaby;
    private List<String> powers;
    private String spawnMessage;
    private String deathMessage;
    private String escapeMessage;
    private String locationMessage;
    private List<String> uniqueLootList;
    private boolean dropsEliteMobsLoot;
    private boolean dropsVanillaLoot;
    private List<String> trails;
    private List<String> onDamageMessages, onDamagedMessages;

    /**
     * Called to write defaults for a new Custom Boss Mob Entity
     */
    public CustomBossConfigFields(String fileName,
                                  String entityType,
                                  boolean isEnabled,
                                  String name,
                                  String level,
                                  int timeout,
                                  Boolean isPersistent,
                                  double healthMultiplier,
                                  double damageMultiplier,
                                  Material helmet,
                                  Material chestplate,
                                  Material leggings,
                                  Material boots,
                                  Material mainHand,
                                  Material offHand,
                                  boolean isBaby,
                                  List<String> powers,
                                  String spawnMessage,
                                  String deathMessage,
                                  String escapeMessage,
                                  String locationMessage,
                                  List<String> uniqueLootList,
                                  boolean dropsEliteMobsLoot,
                                  boolean dropsVanillaLoot,
                                  List<String> trails,
                                  List<String> onDamageMessages,
                                  List<String> onDamagedMessages) {
        this.fileName = fileName + ".yml";
        this.entityType = entityType;
        this.isEnabled = isEnabled;
        this.name = name;
        this.level = level;
        this.timeout = timeout;
        this.isPersistent = isPersistent;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.isBaby = isBaby;
        this.powers = powers;
        this.spawnMessage = spawnMessage;
        this.deathMessage = deathMessage;
        this.escapeMessage = escapeMessage;
        this.locationMessage = locationMessage;
        this.uniqueLootList = uniqueLootList;
        this.dropsEliteMobsLoot = dropsEliteMobsLoot;
        this.dropsVanillaLoot = dropsVanillaLoot;
        this.trails = trails;
        this.onDamageMessages = onDamageMessages;
        this.onDamagedMessages = onDamagedMessages;
    }


    /**
     * Generates config defaults to be used by CustomBossesConfig
     */
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("entityType", entityType);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("level", level);
        fileConfiguration.addDefault("timeout", timeout);
        fileConfiguration.addDefault("isPersistent", isPersistent);
        fileConfiguration.addDefault("healthMultiplier", healthMultiplier);
        fileConfiguration.addDefault("damageMultiplier", damageMultiplier);
        if (helmet != null)
            fileConfiguration.addDefault("helmet", helmet.toString());
        if (chestplate != null)
            fileConfiguration.addDefault("chestplate", chestplate.toString());
        if (leggings != null)
            fileConfiguration.addDefault("leggings", leggings.toString());
        if (boots != null)
            fileConfiguration.addDefault("boots", boots.toString());
        if (mainHand != null)
            fileConfiguration.addDefault("mainHand", mainHand.toString());
        if (offHand != null)
            fileConfiguration.addDefault("offHand", offHand.toString());
        fileConfiguration.addDefault("isBaby", isBaby);
        fileConfiguration.addDefault("powers", powers);
        fileConfiguration.addDefault("spawnMessage", spawnMessage);
        fileConfiguration.addDefault("deathMessage", deathMessage);
        fileConfiguration.addDefault("escapeMessage", escapeMessage);
        fileConfiguration.addDefault("locationMessage", locationMessage);
        fileConfiguration.addDefault("uniqueLootList", uniqueLootList);
        fileConfiguration.addDefault("dropsEliteMobsLoot", dropsEliteMobsLoot);
        fileConfiguration.addDefault("dropsVanillaLoot", dropsVanillaLoot);
        fileConfiguration.addDefault("trails", trails);
        fileConfiguration.addDefault("onDamageMessages", onDamageMessages);
        fileConfiguration.addDefault("onDamagedMessages", onDamagedMessages);
    }

    /**
     * Pulls from the config so it can be used in other spots
     */
    public CustomBossConfigFields(FileConfiguration configuration, File file) {

        this.fileName = file.getName();
        this.entityType = configuration.getString("entityType");
        if (configuration.get("isEnabled") != null)
            this.isEnabled = configuration.getBoolean("isEnabled");
        else this.isEnabled = true;
        this.name = configuration.getString("name");
        this.level = configuration.getString("level");
        if (configuration.get("timeout") != null)
            this.timeout = configuration.getInt("timeout");
        else this.timeout = 0;
        if (configuration.get("isPersistent") != null)
            this.isPersistent = configuration.getBoolean("isPersistent");
        else this.isPersistent = false;
        this.healthMultiplier = configuration.getDouble("healthMultiplier");
        this.damageMultiplier = configuration.getDouble("damageMultiplier");
        this.helmet = Material.getMaterial(configuration.getString("helmet"));
        this.chestplate = Material.getMaterial(configuration.getString("chestplate"));
        this.leggings = Material.getMaterial(configuration.getString("leggings"));
        this.boots = Material.getMaterial(configuration.getString("boots"));
        this.mainHand = Material.getMaterial(configuration.getString("mainHand"));
        this.offHand = Material.getMaterial(configuration.getString("offHand"));
        if (configuration.get("isBaby") != null)
            this.isBaby = configuration.getBoolean("isBaby");
        else this.isBaby = false;
        this.powers = configuration.getStringList("powers");
        this.spawnMessage = configuration.getString("spawnMessage");
        this.deathMessage = configuration.getString("deathMessage");
        this.escapeMessage = configuration.getString("escapeMessage");
        this.locationMessage = configuration.getString("locationMessage");
        this.uniqueLootList = configuration.getStringList("uniqueLootList");
        if (configuration.get("dropsEliteMobsLoot") != null)
            this.dropsEliteMobsLoot = configuration.getBoolean("dropsEliteMobsLoot");
        else this.dropsEliteMobsLoot = true;
        if (configuration.get("dropsVanillaLoot") != null)
            this.dropsVanillaLoot = configuration.getBoolean("dropsVanillaLoot");
        else this.dropsVanillaLoot = true;
        this.trails = configuration.getStringList("trails");
        this.onDamageMessages = configuration.getStringList("onDamageMessages");
        this.onDamagedMessages = configuration.getStringList("onDamagedMessages");
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getEntityType() {
        return this.entityType;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public String getName() {
        return this.name;
    }

    public String getLevel() {
        return this.level;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public Boolean getIsPersistent() {
        return this.isPersistent;
    }

    public double getHealthMultiplier() {
        return this.healthMultiplier;
    }

    public double getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public Material getHelmet() {
        return this.helmet;
    }

    public Material getChestplate() {
        return this.chestplate;
    }

    public Material getLeggings() {
        return this.leggings;
    }

    public Material getBoots() {
        return this.boots;
    }

    public Material getMainHand() {
        return this.mainHand;
    }

    public Material getOffHand() {
        return this.offHand;
    }

    public boolean isBaby() {
        return this.isBaby;
    }

    public List<String> getPowers() {
        return this.powers;
    }

    public String getSpawnMessage() {
        return this.spawnMessage;
    }

    public String getDeathMessage() {
        return this.deathMessage;
    }

    public String getEscapeMessage() {
        return this.escapeMessage;
    }

    public String getLocationMessage() {
        return this.locationMessage;
    }

    public List<String> getUniqueLootList() {
        return this.uniqueLootList;
    }

    public boolean getDropsEliteMobsLoot() {
        return dropsEliteMobsLoot;
    }

    public boolean getDropsVanillaLoot() {
        return dropsVanillaLoot;
    }

    public List<String> getTrails() {
        return this.trails;
    }

    public List<String> getOnDamageMessages() {
        return onDamageMessages;
    }

    public List<String> getOnDamagedMessages() {
        return onDamagedMessages;
    }
}
