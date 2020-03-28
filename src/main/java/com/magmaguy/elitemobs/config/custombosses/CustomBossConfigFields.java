package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

public class CustomBossConfigFields {

    private static HashSet<CustomBossConfigFields> naturallySpawnedElites = new HashSet<>();

    public static HashSet<CustomBossConfigFields> getNaturallySpawnedElites() {
        return naturallySpawnedElites;
    }

    private static HashSet<CustomBossConfigFields> regionalElites = new HashSet<>();

    public static HashSet<CustomBossConfigFields> getRegionalElites() {
        return regionalElites;
    }

    private String fileName;
    private File file;
    private FileConfiguration fileConfiguration;
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
    private List<String> deathMessages;
    private String escapeMessage;
    private String locationMessage;
    private List<String> uniqueLootList;
    private boolean dropsEliteMobsLoot;
    private boolean dropsVanillaLoot;
    private List<String> trails;
    private List<String> onDamageMessages, onDamagedMessages;
    private HashMap<String, Object> additionalConfigOptions = new HashMap<>();
    private double spawnChance;
    private boolean isRegionalBoss;
    private Location spawnLocation;
    private int spawnCooldown;
    private double leashRadius;
    private List<String> onDeathCommands;

    /**
     * Called to write defaults for a new Custom Boss Mob Entity
     */
    public CustomBossConfigFields(String fileName,
                                  String entityType,
                                  boolean isEnabled,
                                  String name,
                                  String level,
                                  Integer timeout,
                                  Boolean isPersistent,
                                  double healthMultiplier,
                                  double damageMultiplier,
                                  Material helmet,
                                  Material chestplate,
                                  Material leggings,
                                  Material boots,
                                  Material mainHand,
                                  Material offHand,
                                  Boolean isBaby,
                                  List<String> powers,
                                  String spawnMessage,
                                  String deathMessage,
                                  List<String> deathMessages,
                                  String escapeMessage,
                                  String locationMessage,
                                  List<String> uniqueLootList,
                                  Boolean dropsEliteMobsLoot,
                                  Boolean dropsVanillaLoot,
                                  List<String> trails,
                                  List<String> onDamageMessages,
                                  List<String> onDamagedMessages,
                                  Double spawnChance,
                                  Boolean isRegionalBoss,
                                  Location spawnLocation,
                                  Integer spawnCooldown,
                                  Double leashRadius,
                                  List<String> onDeathCommands) {
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
        if (isBaby != null)
            this.isBaby = isBaby;
        this.powers = powers;
        this.spawnMessage = spawnMessage;
        this.deathMessage = deathMessage;
        this.deathMessages = deathMessages;
        this.escapeMessage = escapeMessage;
        this.locationMessage = locationMessage;
        this.uniqueLootList = uniqueLootList;
        this.dropsEliteMobsLoot = dropsEliteMobsLoot;
        this.dropsVanillaLoot = dropsVanillaLoot;
        this.trails = trails;
        this.onDamageMessages = onDamageMessages;
        this.onDamagedMessages = onDamagedMessages;
        if (spawnChance != null)
            this.spawnChance = spawnChance;
        if (isRegionalBoss != null)
            this.isRegionalBoss = isRegionalBoss;
        this.spawnLocation = spawnLocation;
        if (spawnCooldown != null)
            this.spawnCooldown = spawnCooldown;
        if (leashRadius != null)
            this.leashRadius = leashRadius;
        if (onDeathCommands != null)
            this.onDeathCommands = onDeathCommands;
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
        fileConfiguration.addDefault("deathMessages", deathMessages);
        fileConfiguration.addDefault("escapeMessage", escapeMessage);
        fileConfiguration.addDefault("locationMessage", locationMessage);
        fileConfiguration.addDefault("uniqueLootList", uniqueLootList);
        fileConfiguration.addDefault("dropsEliteMobsLoot", dropsEliteMobsLoot);
        fileConfiguration.addDefault("dropsVanillaLoot", dropsVanillaLoot);
        fileConfiguration.addDefault("trails", trails);
        fileConfiguration.addDefault("onDamageMessages", onDamageMessages);
        fileConfiguration.addDefault("onDamagedMessages", onDamagedMessages);
        fileConfiguration.addDefault("spawnChance", spawnChance);
        fileConfiguration.addDefault("isRegionalBoss", isRegionalBoss);
        fileConfiguration.addDefault("spawnLocation", spawnLocation);
        fileConfiguration.addDefault("spawnCooldown", spawnCooldown);
        fileConfiguration.addDefault("leashRadius", leashRadius);
        fileConfiguration.addDefault("onDeathCommands", onDeathCommands);
        if (!additionalConfigOptions.isEmpty())
            fileConfiguration.addDefaults(additionalConfigOptions);
    }

    /**
     * Pulls from the config so it can be used in other spots
     */
    public CustomBossConfigFields(FileConfiguration configuration, File file) {

        this.fileName = file.getName();

        this.file = file;

        this.fileConfiguration = configuration;

        this.entityType = configuration.getString("entityType");

        if (configuration.get("isEnabled") == null)
            this.isEnabled = true;
        else
            this.isEnabled = configuration.getBoolean("isEnabled");

        if (configuration.getString("name") == null)
            this.name = "Undefined name";
        else
            this.name = configuration.getString("name");

        if (configuration.getString("level") == null)
            this.level = "-1";
        else
            this.level = configuration.getString("level");

        if (configuration.get("timeout") == null)
            this.timeout = 0;
        else
            this.timeout = configuration.getInt("timeout");

        if (configuration.get("isPersistent") == null)
            this.isPersistent = false;
        else
            this.isPersistent = configuration.getBoolean("isPersistent");

        if (configuration.get("healthMultiplier") == null)
            this.healthMultiplier = 1;
        else
            this.healthMultiplier = configuration.getDouble("healthMultiplier");

        if (configuration.get("healthMultiplier") == null)
            this.damageMultiplier = 1;
        else
            this.damageMultiplier = configuration.getDouble("damageMultiplier");


        this.helmet = parseMaterial(configuration.getString("helmet"));
        this.chestplate = parseMaterial(configuration.getString("chestplate"));
        this.leggings = parseMaterial(configuration.getString("leggings"));
        this.boots = parseMaterial(configuration.getString("boots"));
        this.mainHand = parseMaterial(configuration.getString("mainHand"));
        this.offHand = parseMaterial(configuration.getString("offHand"));

        if (!configuration.contains("isBaby"))
            this.isBaby = false;
        else
            this.isBaby = configuration.getBoolean("isBaby");

        this.powers = configuration.getStringList("powers");
        this.spawnMessage = configuration.getString("spawnMessage");
        this.deathMessage = configuration.getString("deathMessage");
        this.deathMessages = configuration.getStringList("deathMessages");
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
        if (!configuration.contains("onDamageMessages"))
            this.onDamageMessages = new ArrayList<>();
        else
            this.onDamageMessages = configuration.getStringList("onDamageMessages");
        if (!configuration.contains("onDamagedMessages"))
            this.onDamagedMessages = new ArrayList<>();
        else
            this.onDamagedMessages = configuration.getStringList("onDamagedMessages");
        if (configuration.get("spawnChance") != null) {
            this.spawnChance = configuration.getDouble("spawnChance");
            if (this.spawnChance > 0)
                naturallySpawnedElites.add(this);
        } else
            this.spawnChance = 0;
        if (!configuration.contains("isRegionalBoss"))
            this.isRegionalBoss = false;
        else if (configuration.getBoolean("isRegionalBoss")) {
            String location = configuration.getString("spawnLocation");
            if (location == null) {
                new WarningMessage("Regional / World boss does not have a set location! It will not spawn.");
            } else {
                this.spawnLocation = ConfigurationLocation.deserialize(configuration.getString("spawnLocation"));
            }
            if (!configuration.contains("spawnCooldown")) this.spawnCooldown = 0;
            else this.spawnCooldown = configuration.getInt("spawnCooldown");

            if (location != null)
                regionalElites.add(this);
        }

        this.leashRadius = configuration.getDouble("leashRadius");

        this.onDeathCommands = (List<String>) configuration.getList("onDeathCommands");

    }

    private Material parseMaterial(String materialString) {
        try {
            return Material.getMaterial(materialString);
        } catch (Exception e) {
            new WarningMessage("File " + this.fileName + " has an invalid material string: " + materialString);
            return Material.AIR;
        }
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

    public List<String> getDeathMessages() {
        return this.deathMessages;
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

    public double getSpawnChance() {
        return spawnChance;
    }

    public boolean isRegionalBoss() {
        return isRegionalBoss;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        this.fileConfiguration.set("spawnLocation", ConfigurationLocation.serialize(spawnLocation));
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public void setLeashRadius(double leashRadius) {
        this.leashRadius = leashRadius;
        this.fileConfiguration.set("leashRadius", leashRadius);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public int getSpawnCooldown() {
        return spawnCooldown;
    }

    public double getLeashRadius() {
        return leashRadius;
    }

    public List<String> getOnDeathCommands() {
        return onDeathCommands;
    }

    public Map<String, Object> getAdditionalConfigOptions() {
        return additionalConfigOptions;
    }

}
