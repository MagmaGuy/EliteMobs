package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomBossConfigFields {

    public static HashSet<CustomBossConfigFields> customBossConfigFields = new HashSet<>();

    private static final HashSet<CustomBossConfigFields> naturallySpawnedElites = new HashSet<>();

    public static HashSet<CustomBossConfigFields> getNaturallySpawnedElites() {
        return naturallySpawnedElites;
    }

    public static final HashSet<CustomBossConfigFields> regionalElites = new HashSet<>();

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
    private ItemStack helmet = null;
    private ItemStack chestplate = null;
    private ItemStack leggings = null;
    private ItemStack boots = null;
    private ItemStack mainHand = null;
    private ItemStack offHand = null;
    private boolean isBaby = false;
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
    private final HashMap<UUID, ConfigRegionalEntity> configRegionalEntities = new HashMap<>();
    private int spawnCooldown;
    private double leashRadius;
    private Integer followRange;
    private List<String> onDeathCommands;
    private List<String> onSpawnCommands;
    private List<String> onCombatEnterCommands;
    private List<String> onCombatLeaveCommands;
    private String mountedEntity;
    private Integer announcementPriority = 0;
    private String disguise = null;
    private String customDisguiseData = null;
    private Boolean frozen = false;
    private List<String> phases = new ArrayList<>();

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
        if (helmet != null)
            this.helmet = parseItem(helmet.toString());
        if (chestplate != null)
            this.chestplate = parseItem(chestplate.toString());
        if (leggings != null)
            this.leggings = parseItem(leggings.toString());
        if (boots != null)
            this.boots = parseItem(boots.toString());
        if (mainHand != null)
            this.mainHand = parseItem(mainHand.toString());
        if (offHand != null)
            this.offHand = parseItem(offHand.toString());
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
        //warning: this makes it so defaults can't use custom model data, which doesn't really matter right now for defaults
        if (helmet != null)
            fileConfiguration.addDefault("helmet", helmet.getType().toString());
        if (chestplate != null)
            fileConfiguration.addDefault("chestplate", chestplate.getType().toString());
        if (leggings != null)
            fileConfiguration.addDefault("leggings", leggings.getType().toString());
        if (boots != null)
            fileConfiguration.addDefault("boots", boots.getType().toString());
        if (mainHand != null)
            fileConfiguration.addDefault("mainHand", mainHand.getType().toString());
        if (offHand != null)
            fileConfiguration.addDefault("offHand", offHand.getType().toString());
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
        this.helmet = parseItem(configuration.getString("helmet"));
        this.chestplate = parseItem(configuration.getString("chestplate"));
        this.leggings = parseItem(configuration.getString("leggings"));
        this.boots = parseItem(configuration.getString("boots"));
        this.mainHand = parseItem(configuration.getString("mainHand"));
        this.offHand = parseItem(configuration.getString("offHand"));

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
        else
            this.isRegionalBoss = configuration.getBoolean("isRegionalBoss");

        if (this.isRegionalBoss) {

            respawnSaveWatchdog();

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
                    ConfigRegionalEntity configRegionalEntity = new ConfigRegionalEntity(deserializedLocation, string, respawnTime);
                    this.configRegionalEntities.put(configRegionalEntity.uuid, configRegionalEntity);
                }

                if (!configuration.contains("spawnCooldown")) this.spawnCooldown = 0;
                else this.spawnCooldown = configuration.getInt("spawnCooldown");

                regionalElites.add(this);
            }

        }

        this.followRange = configuration.getInt("followDistance");

        this.leashRadius = configuration.getDouble("leashRadius");

        this.onDeathCommands = (List<String>) configuration.getList("onDeathCommands");

        this.onSpawnCommands = (List<String>) configuration.getList("onSpawnCommands");

        this.onCombatEnterCommands = (List<String>) configuration.getList("onCombatEnterCommands");

        this.onCombatLeaveCommands = (List<String>) configuration.getList("onCombatLeaveCommands");

        this.mountedEntity = configuration.getString("mountedEntity");

        customBossConfigFields.add(this);

        if (configuration.get("announcementPriority") != null)
            this.announcementPriority = configuration.getInt("announcementPriority");
        else
            this.announcementPriority = 1;

        this.disguise = configuration.getString("disguise");
        this.customDisguiseData = configuration.getString("customDisguiseData");

        this.frozen = configuration.getBoolean("frozen");
        if (frozen == null)
            frozen = false;

        this.phases = configuration.getStringList("phases");

    }

    public class ConfigRegionalEntity {
        public UUID uuid = UUID.randomUUID();
        public long respawnTimeLeft = 0;
        public Location spawnLocation;
        //this is needed to spawn regional bosses when worlds load and to calculate how many regional bosses exist in a world before it's set up
        public String spawnLocationString;

        public ConfigRegionalEntity(Location spawnLocation, String spawnLocationString, long cooldown) {
            this.spawnLocation = spawnLocation;
            this.spawnLocationString = spawnLocationString;
            this.respawnTimeLeft = cooldown;
        }

    }

    private ItemStack parseItem(String materialString) {
        if (materialString == null)
            return ItemStackGenerator.generateItemStack(Material.AIR);
        try {
            if (materialString.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(materialString)));
                playerHead.setItemMeta(skullMeta);
                return playerHead;
            }
            if (materialString.contains(":")) {
                ItemStack itemStack = ItemStackGenerator.generateItemStack(Material.getMaterial(materialString.split(":")[0]));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setCustomModelData(Integer.parseInt(materialString.split(":")[1]));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            } else
                return ItemStackGenerator.generateItemStack(Material.getMaterial(materialString));
        } catch (Exception e) {
            new WarningMessage("File " + this.fileName + " has an invalid material string: " + materialString);
            new WarningMessage("If you're trying to use a player UUID, something went wrong while trying to assign the UUID.");
            return ItemStackGenerator.generateItemStack(Material.AIR);
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

    public ItemStack getHelmet() {
        return this.helmet;
    }

    public ItemStack getChestplate() {
        return this.chestplate;
    }

    public ItemStack getLeggings() {
        return this.leggings;
    }

    public ItemStack getBoots() {
        return this.boots;
    }

    public ItemStack getMainHand() {
        return this.mainHand;
    }

    public ItemStack getOffHand() {
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
        return configRegionalEntities;
    }

    public ConfigRegionalEntity addSpawnLocation(Location location) {
        ConfigRegionalEntity newConfigRegionalEntity = new ConfigRegionalEntity(location, ConfigurationLocation.serialize(location), 0);
        configRegionalEntities.put(newConfigRegionalEntity.uuid, newConfigRegionalEntity);
        new RegionalBossEntity(this, newConfigRegionalEntity);
        List<String> convertedList = new ArrayList<>();
        for (ConfigRegionalEntity configRegionalEntity : configRegionalEntities.values())
            convertedList.add(configRegionalEntity.spawnLocationString);
        ConfigurationEngine.writeValue(convertedList, file, fileConfiguration, "spawnLocations");
        return newConfigRegionalEntity;
    }

    public void removeSpawnLocation(ConfigRegionalEntity configRegionalEntity) {
        configRegionalEntities.remove(configRegionalEntity.uuid, configRegionalEntity);
        List<String> convertedList = new ArrayList<>();
        for (ConfigRegionalEntity iteratedConfigRegionalEntity : configRegionalEntities.values())
            convertedList.add(ConfigurationLocation.serialize(iteratedConfigRegionalEntity.spawnLocation) + ":" + iteratedConfigRegionalEntity.respawnTimeLeft);
        for (Iterator<RegionalBossEntity> regionalBossEntityIterator = RegionalBossEntity.getRegionalBossEntityList().iterator(); regionalBossEntityIterator.hasNext(); ) {
            RegionalBossEntity regionalBossEntity = regionalBossEntityIterator.next();
            if (regionalBossEntity.configRegionalEntity.equals(configRegionalEntity)) {
                regionalBossEntity.softDelete();
                regionalBossEntityIterator.remove();
            }

        }
        ConfigurationEngine.writeValue(convertedList, file, fileConfiguration, "spawnLocations");
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

    public List<String> getOnSpawnCommands() {
        return onSpawnCommands;
    }

    public List<String> getOnCombatEnterCommands() {
        return onCombatEnterCommands;
    }

    public List<String> getOnCombatLeaveCommands() {
        return onCombatLeaveCommands;
    }

    public Map<String, Object> getAdditionalConfigOptions() {
        return additionalConfigOptions;
    }

    public long getTicksBeforeRespawn(UUID uuid) {
        return (configRegionalEntities.get(uuid).respawnTimeLeft - System.currentTimeMillis()) / 1000 * 20 < 0 ?
                0 :
                (configRegionalEntities.get(uuid).respawnTimeLeft - System.currentTimeMillis()) / 1000 * 20;
    }

    public void updateTicksBeforeRespawn(UUID uuid, int delayInMinutes) {
        configRegionalEntities.get(uuid).respawnTimeLeft = (delayInMinutes * 60 * 1000) + System.currentTimeMillis();
    }

    private boolean saveQueued = false;

    public void saveTicksBeforeRespawn() {
        List<String> convertedList = new ArrayList<>();
        for (ConfigRegionalEntity configRegionalEntity : configRegionalEntities.values())
            convertedList.add(ConfigurationLocation.serialize(configRegionalEntity.spawnLocation) + ":" + configRegionalEntity.respawnTimeLeft);
        fileConfiguration.set("spawnLocations", convertedList);
        saveQueued = true;
    }

    /**
     * This exists to avoid saving the same file multiple times
     */
    private void respawnSaveWatchdog() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (saveQueued) {
                    try {
                        fileConfiguration.save(file);
                        saveQueued = false;
                    } catch (IOException ex) {
                        new WarningMessage("Failed to save Custom Boss data during shutdown! Let the dev know about this error!");
                    }
                }
            }
        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 20 * 60, 20 * 60);
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
        if (this.announcementPriority == null)
            return 1;
        return this.announcementPriority;
    }

    /**
     * Integration with LibsDisguises
     * Only used if that plugin is loaded.
     *
     * @return The string with which to form the DisguiseType
     */
    public String getDisguise() {
        return this.disguise;
    }

    public String getCustomDisguiseData() {
        return customDisguiseData;
    }

    public Boolean getFrozen() {
        return this.frozen;
    }

    public List<String> getPhases() {
        return this.phases;
    }

}
