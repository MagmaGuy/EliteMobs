package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomBossConfigFields {

    public static HashSet<CustomBossConfigFields> customBossConfigFields = new HashSet<>();

    private static final HashSet<CustomBossConfigFields> naturallySpawnedElites = new HashSet<>();

    public static HashSet<CustomBossConfigFields> getNaturallySpawnedElites() {
        return naturallySpawnedElites;
    }

    private static final HashSet<CustomBossConfigFields> regionalElites = new HashSet<>();

    public static HashSet<CustomBossConfigFields> getRegionalElites() {
        return regionalElites;
    }

    private final String fileName;
    private File file;
    private FileConfiguration fileConfiguration;
    private final String entityType;
    private final boolean isEnabled;
    private final String name;
    private final String level;
    private final int timeout;
    private final Boolean isPersistent;
    private final double healthMultiplier;
    private final double damageMultiplier;
    private final Material helmet;
    private final Material chestplate;
    private final Material leggings;
    private final Material boots;
    private final Material mainHand;
    private final Material offHand;
    private boolean isBaby;
    private final List<String> powers;
    private final String spawnMessage;
    private final String deathMessage;
    private final List<String> deathMessages;
    private final String escapeMessage;
    private final String locationMessage;
    private final List<String> uniqueLootList;
    private final boolean dropsEliteMobsLoot;
    private final boolean dropsVanillaLoot;
    private final List<String> trails;
    private final List<String> onDamageMessages;
    private final List<String> onDamagedMessages;
    private final HashMap<String, Object> additionalConfigOptions = new HashMap<>();
    private double spawnChance;
    private boolean isRegionalBoss;
    private final HashMap<UUID, ConfigRegionalEntity> ConfigRegionalEntities = new HashMap<>();
    private int spawnCooldown;
    private double leashRadius;
    private Integer followRange;
    private List<String> onDeathCommands;
    private String mountedEntity;
    private Integer announcementPriority = 0;

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
                                  Integer spawnCooldown,
                                  Double leashRadius,
                                  List<String> onDeathCommands,
                                  Integer announcementPriority) {
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
        if (spawnCooldown != null)
            this.spawnCooldown = spawnCooldown;
        if (leashRadius != null)
            this.leashRadius = leashRadius;
        if (onDeathCommands != null)
            this.onDeathCommands = onDeathCommands;
        this.announcementPriority = announcementPriority;
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
        fileConfiguration.addDefault("spawnCooldown", spawnCooldown);
        fileConfiguration.addDefault("leashRadius", leashRadius);
        fileConfiguration.addDefault("onDeathCommands", onDeathCommands);
        if (!additionalConfigOptions.isEmpty())
            fileConfiguration.addDefaults(additionalConfigOptions);
        fileConfiguration.addDefault("announcementPriority", announcementPriority);
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

            /*
            Legacy: convert old spawn locations to new spawn locations
             */
            if (configuration.contains("spawnLocation") && !configuration.contains("spawnLocations")) {
                List<String> newSpawnLocations = new ArrayList<>();
                newSpawnLocations.add(configuration.getString("spawnLocation"));
                fileConfiguration.set("spawnLocation", null);
                fileConfiguration.set("spawnLocations", newSpawnLocations);
                try {
                    fileConfiguration.save(file);
                } catch (IOException ex) {
                    new WarningMessage("Failed to save Custom Boss data during shutdown! Let the dev know about this error!");
                }

                try {
                    fileConfiguration.save(file);
                } catch (IOException ex) {
                    new WarningMessage("Failed to save data conversion of file " + fileName + " to new location format. Tell the developer!");
                }
                new WarningMessage("Converted old spawn location to new spawn location for " + fileName);
            }

            if (configuration.contains("spawnLocation") && configuration.contains("spawnLocations")) {
                configuration.set("spawnLocation", null);
                try {
                    fileConfiguration.save(file);
                } catch (IOException ex) {
                    new WarningMessage("Failed to save data conversion of file " + fileName + " to new location format. Tell the developer!");
                }
                new WarningMessage("Safely cleared deprecated file location for " + fileName);
            }

            List<String> locations = configuration.getStringList("spawnLocations");
            if (locations.size() < 1)
                new WarningMessage("Regional / World boss does not have a set location! It will not spawn. File name: " + this.fileName);

            if (locations.size() > 0) {
                Location deserializedLocation = null;
                long respawnTime = 0L;
                for (String string : locations) {
                    deserializedLocation = ConfigurationLocation.deserialize(string);
                    if (deserializedLocation == null)
                        new WarningMessage("Warning: file " + fileName + " has an invalid location " + string);
                    if (string.contains(":"))
                        respawnTime = Long.parseLong(string.substring(string.indexOf(":") + 1));
                    ConfigRegionalEntity configRegionalEntity = new ConfigRegionalEntity(deserializedLocation, respawnTime);
                    this.ConfigRegionalEntities.put(configRegionalEntity.uuid, configRegionalEntity);
                }
            }

            if (!configuration.contains("spawnCooldown")) this.spawnCooldown = 0;
            else this.spawnCooldown = configuration.getInt("spawnCooldown");

            if (ConfigRegionalEntities != null)
                regionalElites.add(this);

        }

        this.followRange = configuration.getInt("followRange");

        this.leashRadius = configuration.getDouble("leashRadius");

        this.onDeathCommands = (List<String>) configuration.getList("onDeathCommands");

        this.mountedEntity = configuration.getString("mountedEntity");

        customBossConfigFields.add(this);

        if (configuration.get("announcementPriority") != null)
            this.announcementPriority = configuration.getInt("announcementPriority");
        else
            this.announcementPriority = 1;

    }

    public class ConfigRegionalEntity {
        public UUID uuid = UUID.randomUUID();
        public long respawnTimeLeft = 0;
        public Location spawnLocation;

        public ConfigRegionalEntity(Location spawnLocation, long cooldown) {
            this.spawnLocation = spawnLocation;
            this.respawnTimeLeft = cooldown;
        }
    }

    private Material parseMaterial(String materialString) {
        if (materialString == null)
            return Material.AIR;
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

    public HashMap<UUID, ConfigRegionalEntity> getConfigRegionalEntities() {
        return ConfigRegionalEntities;
    }

    public void addSpawnLocation(Location location) {
        ConfigRegionalEntity newConfigRegionalEntity = new ConfigRegionalEntity(location, 0);
        ConfigRegionalEntities.put(newConfigRegionalEntity.uuid, newConfigRegionalEntity);
        List<String> convertedList = new ArrayList<>();
        for (ConfigRegionalEntity configRegionalEntity : ConfigRegionalEntities.values())
            convertedList.add(ConfigurationLocation.serialize(configRegionalEntity.spawnLocation) + ":" + configRegionalEntity.respawnTimeLeft);
        fileConfiguration.set("spawnLocations", convertedList);
        try {
            fileConfiguration.save(file);
        } catch (IOException ex) {
            new WarningMessage("Failed to save new boss location! It will not show up in the right place after a restart. Report this to the dev.");
        }
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

    public Integer getFollowRange() {
        return this.followRange;
    }

    public List<String> getOnDeathCommands() {
        return onDeathCommands;
    }

    public Map<String, Object> getAdditionalConfigOptions() {
        return additionalConfigOptions;
    }

    public int getTicksBeforeRespawn(UUID uuid) {
        return (int) (ConfigRegionalEntities.get(uuid).respawnTimeLeft - System.currentTimeMillis()) / 1000 * 20 < 0 ?
                0 :
                (int) (ConfigRegionalEntities.get(uuid).respawnTimeLeft - System.currentTimeMillis()) / 1000 * 20;
    }

    public void updateTicksBeforeRespawn(UUID uuid, int delayInMinutes) {
        long newTime = delayInMinutes * 60 * 1000 + System.currentTimeMillis();
        ConfigRegionalEntities.get(uuid).respawnTimeLeft = newTime;
    }

    public void saveTicksBeforeRespawn() {
        List<String> convertedList = new ArrayList<>();
        for (ConfigRegionalEntity configRegionalEntity : ConfigRegionalEntities.values())
            convertedList.add(ConfigurationLocation.serialize(configRegionalEntity.spawnLocation) + ":" + configRegionalEntity.respawnTimeLeft);
        fileConfiguration.set("spawnLocations", convertedList);
        try {
            fileConfiguration.save(file);
        } catch (IOException ex) {
            new WarningMessage("Failed to save Custom Boss data during shutdown! Let the dev know about this error!");
        }
    }

    public String getMountedEntity() {
        return this.mountedEntity;
    }

    /**
     * Announcement priority:
     * 0 - no messages
     * 1 - spawn/kill/escape messages
     * 2 - spawn/kill/escape messages + player tracking
     * 3 - spawn/kill/escape messages + player tracking + DiscordSRV discord notifications
     * <p>
     * Default is 1 since the spawn messages have to be added to config files intentionally and it's weird to have to
     * enabled them elsewhere on purpose
     *
     * @return
     */
    public int getAnnouncementPriority() {
        return this.announcementPriority;
    }

}
