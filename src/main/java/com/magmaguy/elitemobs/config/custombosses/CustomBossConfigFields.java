package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.events.mobs.sharedeventproperties.DynamicBossLevelConstructor;
import com.magmaguy.elitemobs.mobconstructor.custombosses.AbstractRegionalEntity;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.*;

public class CustomBossConfigFields {

    public static HashMap<String, CustomBossConfigFields> customBossConfigFields = new HashMap<>();
    private static final HashSet<CustomBossConfigFields> naturallySpawnedElites = new HashSet<>();

    public static HashSet<CustomBossConfigFields> getNaturallySpawnedElites() {
        return naturallySpawnedElites;
    }

    public static HashMap<String, CustomBossConfigFields> regionalElites = new HashMap<>();

    /**
     * Creates a new default pre-made Custom Boss. The boss is further customized through a builder pattern.
     *
     * @param fileName
     * @param entityType
     * @param isEnabled
     * @param name
     * @param level
     */
    public CustomBossConfigFields(String fileName,
                                  String entityType,
                                  boolean isEnabled,
                                  String name,
                                  String level) {
        this.fileName = fileName + ".yml";
        this.entityType = entityType;
        this.enabled = isEnabled;
        this.name = name;
        this.level = level;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private int timeout = 0;

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    private boolean persistent = false;

    public double getHealthMultiplier() {
        return this.healthMultiplier;
    }

    public void setHealthMultiplier(double healthMultiplier) {
        this.healthMultiplier = healthMultiplier;
    }

    private double healthMultiplier = 1;

    public double getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    private double damageMultiplier = 1;

    public ItemStack getHelmet() {
        return this.helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    private ItemStack helmet = null;

    public ItemStack getChestplate() {
        return this.chestplate;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    private ItemStack chestplate = null;

    public ItemStack getLeggings() {
        return this.leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    private ItemStack leggings = null;

    public ItemStack getBoots() {
        return this.boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    private ItemStack boots = null;

    public ItemStack getMainHand() {
        return this.mainHand;
    }

    public void setMainHand(ItemStack mainHand) {
        this.mainHand = mainHand;
    }

    private ItemStack mainHand = null;

    public ItemStack getOffHand() {
        return this.offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    private ItemStack offHand = null;

    public boolean getBaby() {
        return this.baby;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    private boolean baby = false;

    public List<String> getPowers() {
        return this.powers;
    }

    public void setPowers(List<String> powers) {
        this.powers = powers;
    }

    private List<String> powers = new ArrayList<>();

    public String getSpawnMessage() {
        return this.spawnMessage;
    }

    public void setSpawnMessage(String spawnMessage) {
        this.spawnMessage = spawnMessage;
    }

    private String spawnMessage = null;

    public String getDeathMessage() {
        return this.deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    private String deathMessage = null;

    public List<String> getDeathMessages() {
        return this.deathMessages;
    }

    public void setDeathMessages(List<String> deathMessages) {
        this.deathMessages = deathMessages;
    }

    private List<String> deathMessages = new ArrayList<>();

    public String getEscapeMessage() {
        return this.escapeMessage;
    }

    public void setEscapeMessage(String escapeMessage) {
        this.escapeMessage = escapeMessage;
    }

    private String escapeMessage = null;

    public String getLocationMessage() {
        return this.locationMessage;
    }

    public void setLocationMessage(String locationMessage) {
        this.locationMessage = locationMessage;
    }

    private String locationMessage = null;

    public List<String> getUniqueLootList() {
        return this.uniqueLootList;
    }

    public void setUniqueLootList(List<String> uniqueLootList) {
        this.uniqueLootList = uniqueLootList;
    }

    private List<String> uniqueLootList = new ArrayList<>();

    public boolean getDropsEliteMobsLoot() {
        return dropsEliteMobsLoot;
    }

    public void setDropsEliteMobsLoot(boolean dropsEliteMobsLoot) {
        this.dropsEliteMobsLoot = dropsEliteMobsLoot;
    }

    private boolean dropsEliteMobsLoot = true;

    public boolean getDropsVanillaLoot() {
        return dropsVanillaLoot;
    }

    public void setDropsVanillaLoot(boolean dropsVanillaLoot) {
        this.dropsVanillaLoot = dropsVanillaLoot;
    }

    private boolean dropsVanillaLoot = true;

    public List<String> getTrails() {
        return this.trails;
    }

    public void setTrails(List<String> trails) {
        this.trails = trails;
    }

    private List<String> trails = new ArrayList<>();

    public List<String> getOnDamageMessages() {
        return onDamageMessages;
    }

    public void setOnDamageMessages(List<String> onDamageMessages) {
        this.onDamageMessages = onDamageMessages;
    }

    private List<String> onDamageMessages = new ArrayList<>();

    public List<String> getOnDamagedMessages() {
        return onDamagedMessages;
    }

    public void setOnDamagedMessages(List<String> onDamagedMessages) {
        this.onDamagedMessages = onDamagedMessages;
    }

    private List<String> onDamagedMessages = new ArrayList<>();

    public double getLeashRadius() {
        return leashRadius;
    }

    public void runtimeSetLeashRadius(double leashRadius) {
        this.leashRadius = leashRadius;
        this.fileConfiguration.set("leashRadius", leashRadius);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public void setLeashRadius(double leashRadius) {
        this.leashRadius = leashRadius;
    }

    private double leashRadius = 0;

    public Integer getFollowDistance() {
        return this.followDistance;
    }

    public void setFollowDistance(Integer followDistance) {
        this.followDistance = followDistance;
    }

    private Integer followDistance = 0;

    public double getSpawnChance() {
        return spawnChance;
    }

    public void setSpawnChance(double spawnChance) {
        this.spawnChance = spawnChance;
    }

    private double spawnChance = 0;

    public boolean isRegionalBoss() {
        return regionalBoss;
    }

    public void setRegionalBoss(boolean regionalBoss) {
        this.regionalBoss = regionalBoss;
    }

    private boolean regionalBoss = false;

    public int getSpawnCooldown() {
        return spawnCooldown;
    }

    public void setSpawnCooldown(int spawnCooldown) {
        this.spawnCooldown = spawnCooldown;
    }

    private int spawnCooldown = 0;

    public List<String> getOnDeathCommands() {
        return onDeathCommands;
    }

    public void setOnDeathCommands(List<String> onDeathCommands) {
        this.onDeathCommands = onDeathCommands;
    }

    private List<String> onDeathCommands = new ArrayList<>();

    public List<String> getOnSpawnCommands() {
        return onSpawnCommands;
    }

    public void setOnSpawnCommands(List<String> onSpawnCommands) {
        this.onSpawnCommands = onSpawnCommands;
    }

    private List<String> onSpawnCommands = new ArrayList<>();

    public List<String> getOnCombatEnterCommands() {
        return onCombatEnterCommands;
    }

    public void setOnCombatEnterCommands(List<String> onCombatEnterCommands) {
        this.onCombatEnterCommands = onCombatEnterCommands;
    }

    private List<String> onCombatEnterCommands = new ArrayList<>();

    public List<String> getOnCombatLeaveCommands() {
        return onCombatLeaveCommands;
    }

    public void setOnCombatLeaveCommands(List<String> onCombatLeaveCommands) {
        this.onCombatLeaveCommands = onCombatLeaveCommands;
    }

    private List<String> onCombatLeaveCommands = new ArrayList<>();

    public String getMountedEntity() {
        return this.mountedEntity;
    }

    public void setMountedEntity(String mountedEntity) {
        this.mountedEntity = mountedEntity;
    }

    private String mountedEntity = null;

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

    public void setAnnouncementPriority(int announcementPriority) {
        this.announcementPriority = announcementPriority;
    }

    private int announcementPriority = 0;

    /**
     * Integration with LibsDisguises
     * Only used if that plugin is loaded.
     *
     * @return The string with which to form the DisguiseType
     */
    public String getDisguise() {
        return this.disguise;
    }

    public void setDisguise(String disguise) {
        this.disguise = disguise;
    }

    private String disguise = null;

    public String getCustomDisguiseData() {
        return customDisguiseData;
    }

    public void setCustomDisguiseData(String customDisguiseData) {
        this.customDisguiseData = customDisguiseData;
    }

    private String customDisguiseData = null;

    public Boolean getFrozen() {
        return this.frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    private boolean frozen = false;

    public List<String> getPhases() {
        return this.phases;
    }

    public void setPhases(List<String> phases) {
        this.phases = phases;
    }

    private List<String> phases = new ArrayList<>();

    public double getDamageModifier(Material material) {
        return damageModifiers.get(material) == null ? 1 : damageModifiers.get(material);
    }

    public HashMap<Material, Double> getDamageModifiers() {
        return damageModifiers;
    }

    private final HashMap<Material, Double> damageModifiers = new HashMap();

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private File file = null;

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String fileName = null;

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    private FileConfiguration fileConfiguration = null;

    public String getEntityType() {
        return this.entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    private String entityType = null;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean enabled = true;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name = null;

    public int getLevel() {
        if (level.equalsIgnoreCase("dynamic")) {
            return DynamicBossLevelConstructor.findDynamicBossLevel();
        } else {
            try {
                return Integer.valueOf(level);
            } catch (Exception ex) {
                new WarningMessage("Regional Elite Mob level for " + fileName + " is neither numeric nor dynamic. Fix the configuration for it.");
                return 1;
            }
        }
    }

    public void setLevel(String level) {
        this.level = level;
    }

    private String level = null;

    public boolean getCullReinforcements() {
        return cullReinforcements;
    }

    public void setCullReinforcements(boolean cullReinforcements) {
        this.cullReinforcements = cullReinforcements;
    }

    private boolean cullReinforcements = true;

    public boolean isFilesOutOfSync() {
        return filesOutOfSync;
    }

    public void setFilesOutOfSync(boolean filesOutOfSync) {
        this.filesOutOfSync = filesOutOfSync;
    }

    private boolean filesOutOfSync = false;

    /**
     * Generates config defaults to be used by CustomBossesConfig
     */
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        //initialize core defaults
        fileConfiguration.addDefault("isEnabled", enabled);
        fileConfiguration.addDefault("entityType", entityType);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("level", level);
        //All defaults below this point are optional
        if (timeout != 0)
            fileConfiguration.addDefault("timeout", timeout);
        if (persistent)
            fileConfiguration.addDefault("isPersistent", persistent);
        if (healthMultiplier != 1)
            fileConfiguration.addDefault("healthMultiplier", healthMultiplier);
        if (damageMultiplier != 1)
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
        if (baby)
            fileConfiguration.addDefault("isBaby", baby);
        if (!powers.isEmpty())
            fileConfiguration.addDefault("powers", powers);
        if (spawnMessage != null)
            fileConfiguration.addDefault("spawnMessage", spawnMessage);
        if (deathMessage != null)
            fileConfiguration.addDefault("deathMessage", deathMessage);
        if (!deathMessages.isEmpty())
            fileConfiguration.addDefault("deathMessages", deathMessages);
        if (escapeMessage != null)
            fileConfiguration.addDefault("escapeMessage", escapeMessage);
        if (locationMessage != null)
            fileConfiguration.addDefault("locationMessage", locationMessage);
        if (!uniqueLootList.isEmpty())
            fileConfiguration.addDefault("uniqueLootList", uniqueLootList);
        if (!dropsEliteMobsLoot)
            fileConfiguration.addDefault("dropsEliteMobsLoot", dropsEliteMobsLoot);
        if (!dropsVanillaLoot)
            fileConfiguration.addDefault("dropsVanillaLoot", dropsVanillaLoot);
        if (!trails.isEmpty())
            fileConfiguration.addDefault("trails", trails);
        if (!onDamageMessages.isEmpty())
            fileConfiguration.addDefault("onDamageMessages", onDamageMessages);
        if (!onDamagedMessages.isEmpty())
            fileConfiguration.addDefault("onDamagedMessages", onDamagedMessages);
        if (spawnChance > 0)
            fileConfiguration.addDefault("spawnChance", spawnChance);
        if (regionalBoss)
            fileConfiguration.addDefault("isRegionalBoss", regionalBoss);
        if (spawnCooldown > 0)
            fileConfiguration.addDefault("spawnCooldown", spawnCooldown);
        if (leashRadius > 0)
            fileConfiguration.addDefault("leashRadius", leashRadius);
        if (!onDeathCommands.isEmpty())
            fileConfiguration.addDefault("onDeathCommands", onDeathCommands);
        if (announcementPriority > 0)
            fileConfiguration.addDefault("announcementPriority", announcementPriority);
        if (disguise != null)
            fileConfiguration.addDefault("disguise", disguise);
        if (customDisguiseData != null)
            fileConfiguration.addDefault("customDisguiseData", customDisguiseData);
        if (followDistance > 0)
            fileConfiguration.addDefault("followDistance", followDistance);
        if (!phases.isEmpty())
            fileConfiguration.addDefault("phases", phases);
        if (mountedEntity != null)
            fileConfiguration.addDefault("mountedEntity", mountedEntity);
        if (!cullReinforcements)
            fileConfiguration.addDefault("cullReinforcements", cullReinforcements);
        if (frozen)
            fileConfiguration.addDefault("frozen", frozen);
    }

    /**
     * Pulls from the config so it can be used in other spots
     */
    public CustomBossConfigFields(FileConfiguration configuration, File file) {
        //initialize core defaults
        this.fileName = file.getName();
        this.file = file;
        this.fileConfiguration = configuration;

        if (!configHas("entityType")) {
            this.entityType = "ZOMBIE";
            new WarningMessage("Your custom boss " + fileName + " does not have an entity type! Make sure you set one!");
        } else
            this.entityType = parseConfigString("entityType");

        if (configHas("isEnabled"))
            this.enabled = configuration.getBoolean("isEnabled");

        if (!configHas("name"))
            this.name = "Undefined name";
        else
            this.name = parseConfigString("name");

        if (!configHas("level"))
            this.level = "dynamic";
        else
            this.level = parseConfigString("level");

        if (configHas("isPersistent"))
            this.persistent = parseConfigBoolean("isPersistent");

        if (configHas("healthMultiplier"))
            this.healthMultiplier = parseConfigDouble("healthMultiplier");

        if (configHas("damageMultiplier"))
            this.damageMultiplier = parseConfigDouble("damageMultiplier");

        if (configHas("isBaby"))
            this.baby = parseConfigBoolean("isBaby");

        if (configHas("dropsEliteMobsLoot"))
            this.dropsEliteMobsLoot = parseConfigBoolean("dropsEliteMobsLoot");

        if (configHas("dropsVanillaLoot"))
            this.dropsVanillaLoot = parseConfigBoolean("dropsVanillaLoot");

        if (configHas("spawnChance")) {
            this.spawnChance = parseConfigDouble("spawnChance");
            if (this.spawnChance > 0)
                if (enabled)
                    naturallySpawnedElites.add(this);
        }

        if (configHas("frozen"))
            this.frozen = parseConfigBoolean("frozen");

        this.onDeathCommands = (List<String>) parseConfigArray("onDeathCommands");
        this.onSpawnCommands = (List<String>) parseConfigArray("onSpawnCommands");
        this.onCombatEnterCommands = (List<String>) parseConfigArray("onCombatEnterCommands");
        this.onCombatLeaveCommands = (List<String>) parseConfigArray("onCombatLeaveCommands");
        this.deathMessages = parseConfigArray("deathMessages");
        this.uniqueLootList = parseConfigArray("uniqueLootList");
        this.powers = parseConfigArray("powers");
        this.onDamageMessages = parseConfigArray("onDamageMessages");
        this.onDamagedMessages = parseConfigArray("onDamagedMessages");
        this.trails = parseConfigArray("trails");
        this.phases = parseConfigArray("phases");

        this.locationMessage = parseConfigString("locationMessage");
        this.mountedEntity = parseConfigString("mountedEntity");
        this.spawnMessage = parseConfigString("spawnMessage");
        this.deathMessage = parseConfigString("deathMessage");
        this.escapeMessage = parseConfigString("escapeMessage");
        this.disguise = parseConfigString("disguise");
        this.customDisguiseData = parseConfigString("customDisguiseData");

        this.announcementPriority = parseConfigInteger("announcementPriority");
        this.followDistance = parseConfigInteger("followDistance");
        this.spawnCooldown = parseConfigInteger("spawnCooldown");
        this.timeout = parseConfigInteger("timeout");

        this.leashRadius = parseConfigDouble("leashRadius");

        this.helmet = parseItem(configuration.getString("helmet"));
        this.chestplate = parseItem(configuration.getString("chestplate"));
        this.leggings = parseItem(configuration.getString("leggings"));
        this.boots = parseItem(configuration.getString("boots"));
        this.mainHand = parseItem(configuration.getString("mainHand"));
        this.offHand = parseItem(configuration.getString("offHand"));

        customBossConfigFields.put(fileName, this);

        if (configHas("isRegionalBoss")) {
            this.regionalBoss = parseConfigBoolean("isRegionalBoss");
            if (this.regionalBoss) {
                regionalElites.put(fileName, this);
                AbstractRegionalEntity.initialize(this);
            }
        }

        for (String rawDamageModifier : configuration.getStringList("damageModifiers")) {
            String[] parsedStrings = rawDamageModifier.split(",");
            Material material = null;
            Double multiplier = null;
            for (String parsedDamageModifier : parsedStrings) {
                if (parsedDamageModifier.contains("material:"))
                    try {
                        material = Material.getMaterial(parsedDamageModifier.replace("material:", ""));
                    } catch (Exception ex) {
                        new WarningMessage("Boss " + fileName + " has invalid entry " + parsedDamageModifier + " !");
                    }
                else if (parsedDamageModifier.contains("multiplier:"))
                    try {
                        multiplier = Double.parseDouble(parsedDamageModifier.replace("multiplier:", ""));
                    } catch (Exception ex) {
                        new WarningMessage("Boss " + fileName + " has invalid entry " + parsedDamageModifier + " !");
                    }
                else
                    new WarningMessage("Entry " + parsedDamageModifier + " is invalid for boss file " + fileName + " !");
            }

            if (material != null && multiplier != null)
                damageModifiers.put(material, multiplier);

        }

    }

    private String parseConfigString(String configKey) {
        return fileConfiguration.getString(configKey);
    }

    private List parseConfigArray(String configKey) {
        if (!configHas(configKey)) return new ArrayList();
        return fileConfiguration.getList(configKey);
    }

    //Note: this is currently limited to defaulting to 0 if something doesn't exist, which works only for most things
    private int parseConfigInteger(String configKey) {
        if (!configHas(configKey)) return 0;
        return fileConfiguration.getInt(configKey);
    }

    private double parseConfigDouble(String configKey) {
        if (!configHas(configKey)) return 0;
        return fileConfiguration.getDouble(configKey);
    }

    private boolean parseConfigBoolean(String configKey) {
        return fileConfiguration.getBoolean(configKey);
    }

    private boolean configHas(String configKey) {
        return fileConfiguration.contains(configKey);
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

}
