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

    private final String fileName;
    private File file;
    private FileConfiguration fileConfiguration;
    private final String entityType;
    private final boolean isEnabled;
    private final String name;
    private final String level;
    private final int timeout;
    private Boolean isPersistent;
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
    private HashMap<Material, Double> damageModifiers = new HashMap();

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
                if (isEnabled)
                    naturallySpawnedElites.add(this);
        } else
            this.spawnChance = 0;

        if (!configuration.contains("isRegionalBoss"))
            this.isRegionalBoss = false;
        else
            this.isRegionalBoss = configuration.getBoolean("isRegionalBoss");

        this.followRange = configuration.getInt("followDistance");

        this.leashRadius = configuration.getDouble("leashRadius");

        this.onDeathCommands = (List<String>) configuration.getList("onDeathCommands");

        this.onSpawnCommands = (List<String>) configuration.getList("onSpawnCommands");

        this.onCombatEnterCommands = (List<String>) configuration.getList("onCombatEnterCommands");

        this.onCombatLeaveCommands = (List<String>) configuration.getList("onCombatLeaveCommands");

        this.mountedEntity = configuration.getString("mountedEntity");

        customBossConfigFields.put(fileName, this);

        if (configuration.get("announcementPriority") != null)
            this.announcementPriority = configuration.getInt("announcementPriority");
        else
            this.announcementPriority = 1;

        this.disguise = configuration.getString("disguise");
        this.customDisguiseData = configuration.getString("customDisguiseData");

        this.frozen = configuration.getBoolean("frozen");
        if (frozen == null)
            frozen = false;

        if (!configuration.contains("spawnCooldown")) this.spawnCooldown = 0;
        else this.spawnCooldown = configuration.getInt("spawnCooldown");

        if (this.isRegionalBoss) {
            regionalElites.put(fileName, this);
            AbstractRegionalEntity.initialize(this);
        }

        this.phases = configuration.getStringList("phases");

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
                else new WarningMessage("Entry " + parsedDamageModifier + " is invalid for boss file " + fileName + " !");
            }

            if (material != null && multiplier != null)
                damageModifiers.put(material, multiplier);

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

    public int getTimeout() {
        return this.timeout;
    }

    public Boolean getIsPersistent() {
        return this.isPersistent;
    }

    //Used to override the persistent behavior of mounted entites, in the future also of regional bosses
    public void setIsPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
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

    public File getFile() {
        return file;
    }

    public double getDamageModifier(Material material ){
        return damageModifiers.get(material) == null ? 1 : damageModifiers.get(material);
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public boolean filesOutOfSync = false;

}
