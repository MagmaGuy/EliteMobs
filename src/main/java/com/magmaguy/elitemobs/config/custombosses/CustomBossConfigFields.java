package com.magmaguy.elitemobs.config.custombosses;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBossConfigFields {

    public static CustomBossConfigFields getCustomBossMobConfigFields(String fileName) {
        return new CustomBossConfigFields(CustomBossesConfig.getCustomBossConfig(fileName));
    }

    private String fileName;
    private String entityType;
    private boolean isEnabled;
    private String name;
    private String level;
    private int timeout;
    private boolean isPersistent;
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

    /**
     * Called to write defaults for a new Custom Boss Mob Entity
     */
    public CustomBossConfigFields(String fileName,
                                  String entityType,
                                  boolean isEnabled,
                                  String name,
                                  String level,
                                  int timeout,
                                  boolean isPersistent,
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
                                  List<String> trails) {
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
    }


    /**
     * Generates config defaults to be used by CustomBossesConfig
     */
    public Map<String, Object> generateConfigDefaults() {
        Map<String, Object> configDefaults = new HashMap<>();
        configDefaults.put("isEnabled", isEnabled);
        configDefaults.put("entityType", entityType);
        configDefaults.put("name", name);
        configDefaults.put("level", level);
        configDefaults.put("timeout", timeout);
        configDefaults.put("isPersistent", isPersistent);
        configDefaults.put("healthMultiplier", healthMultiplier);
        configDefaults.put("damageMultiplier", damageMultiplier);
        if (helmet != null)
            configDefaults.put("helmet", helmet.toString());
        if (chestplate != null)
            configDefaults.put("chestplate", chestplate.toString());
        if (leggings != null)
            configDefaults.put("leggings", leggings.toString());
        if (boots != null)
            configDefaults.put("boots", boots.toString());
        if (mainHand != null)
            configDefaults.put("mainHand", mainHand.toString());
        if (offHand != null)
            configDefaults.put("offHand", offHand.toString());
        configDefaults.put("isBaby", isBaby);
        configDefaults.put("powers", powers);
        configDefaults.put("spawnMessage", spawnMessage);
        configDefaults.put("deathMessage", deathMessage);
        configDefaults.put("escapeMessage", escapeMessage);
        configDefaults.put("locationMessage", locationMessage);
        configDefaults.put("uniqueLootList", uniqueLootList);
        configDefaults.put("dropsEliteMobsLoot", dropsEliteMobsLoot);
        configDefaults.put("dropsVanillaLoot", dropsVanillaLoot);
        configDefaults.put("trails", trails);
        return configDefaults;
    }

    /**
     * Pulls from the config so it can be used in other spots
     */
    public CustomBossConfigFields(FileConfiguration configuration) {

        this.fileName = configuration.getName();
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

    public boolean getIsPersistent() {
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

    public List<String> getTrails() {
        return this.trails;
    }
}
