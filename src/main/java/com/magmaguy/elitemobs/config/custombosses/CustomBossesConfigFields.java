package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CustomBossesConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    private static final HashSet<CustomBossesConfigFields> naturallySpawnedElites = new HashSet<>();
    public static HashMap<String, CustomBossesConfigFields> customBossConfigFields = new HashMap<>();
    public static HashMap<String, CustomBossesConfigFields> regionalElites = new HashMap<>();
    private final HashMap<Material, Double> damageModifiers = new HashMap();
    private int timeout = 0;
    private boolean isPersistent = false;
    private double healthMultiplier = 1;
    private double damageMultiplier = 1;
    private ItemStack helmet = null;
    private ItemStack chestplate = null;
    private ItemStack leggings = null;
    private ItemStack boots = null;
    private ItemStack mainHand = null;
    private ItemStack offHand = null;
    private boolean baby = false;
    private List<String> powers = new ArrayList<>();
    private String spawnMessage = null;
    private String deathMessage = null;
    private List<String> deathMessages = new ArrayList<>();
    private String escapeMessage = null;
    private String locationMessage = null;
    private List<String> uniqueLootList = new ArrayList<>();
    private HashMap<CustomItem, Double> parsedUniqueLootList = new HashMap<>();
    private boolean dropsEliteMobsLoot = true;
    private boolean dropsVanillaLoot = true;
    private List<String> trails = new ArrayList<>();
    private List<String> onDamageMessages = new ArrayList<>();
    private List<String> onDamagedMessages = new ArrayList<>();
    private double leashRadius = 0;
    private Integer followDistance = 0;
    private boolean regionalBoss = false;
    private int spawnCooldown = 0;
    private List<String> onDeathCommands = new ArrayList<>();
    private List<String> onSpawnCommands = new ArrayList<>();
    private List<String> onCombatEnterCommands = new ArrayList<>();
    private List<String> onCombatLeaveCommands = new ArrayList<>();
    private String mountedEntity = null;
    private int announcementPriority = 0;
    private String disguise = null;
    private String customDisguiseData = null;
    private boolean frozen = false;
    private boolean reinforcement = false;
    private List<String> phases = new ArrayList<>();
    private EntityType entityType = EntityType.ZOMBIE;
    private boolean enabled = true;
    private String name = "Default Name";
    private String level = null;
    private boolean cullReinforcements = true;
    private boolean filesOutOfSync = false;

    /**
     * Creates a new default pre-made Custom Boss. The boss is further customized through a builder pattern.
     *
     * @param fileName
     * @param entityType
     * @param isEnabled
     * @param name
     * @param level
     */
    public CustomBossesConfigFields(String fileName,
                                    EntityType entityType,
                                    boolean isEnabled,
                                    String name,
                                    String level) {
        super(fileName, isEnabled);
        this.entityType = entityType;
        this.name = name;
        this.level = level;
    }

    public CustomBossesConfigFields(String fileName,
                                    boolean isEnabled) {
        super(fileName, isEnabled);
    }

    public static HashSet<CustomBossesConfigFields> getNaturallySpawnedElites() {
        return naturallySpawnedElites;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public void setPersistent(boolean persistent) {
        this.isPersistent = persistent;
    }

    public double getHealthMultiplier() {
        return this.healthMultiplier;
    }

    public void setHealthMultiplier(double healthMultiplier) {
        this.healthMultiplier = healthMultiplier;
    }

    public double getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public ItemStack getHelmet() {
        return this.helmet;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public ItemStack getChestplate() {
        return this.chestplate;
    }

    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }

    public ItemStack getLeggings() {
        return this.leggings;
    }

    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }

    public ItemStack getBoots() {
        return this.boots;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public ItemStack getMainHand() {
        return this.mainHand;
    }

    public void setMainHand(ItemStack mainHand) {
        this.mainHand = mainHand;
    }

    public ItemStack getOffHand() {
        return this.offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public boolean getBaby() {
        return this.baby;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    public List<String> getPowers() {
        return this.powers;
    }

    public void setPowers(List<String> powers) {
        this.powers = powers;
    }

    public String getSpawnMessage() {
        return this.spawnMessage;
    }

    public void setSpawnMessage(String spawnMessage) {
        this.spawnMessage = spawnMessage;
    }

    public String getDeathMessage() {
        return this.deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    public List<String> getDeathMessages() {
        return this.deathMessages;
    }

    public void setDeathMessages(List<String> deathMessages) {
        this.deathMessages = deathMessages;
    }

    public String getEscapeMessage() {
        return this.escapeMessage;
    }

    public void setEscapeMessage(String escapeMessage) {
        this.escapeMessage = escapeMessage;
    }

    public String getLocationMessage() {
        return this.locationMessage;
    }

    public void setLocationMessage(String locationMessage) {
        this.locationMessage = locationMessage;
    }

    public HashMap<CustomItem, Double> getUniqueLootList() {
        return this.parsedUniqueLootList;
    }

    public void setUniqueLootList(List<String> uniqueLootList) {
        this.uniqueLootList = uniqueLootList;
    }

    public boolean getDropsEliteMobsLoot() {
        return dropsEliteMobsLoot;
    }

    public void setDropsEliteMobsLoot(boolean dropsEliteMobsLoot) {
        this.dropsEliteMobsLoot = dropsEliteMobsLoot;
    }

    public boolean getDropsVanillaLoot() {
        return dropsVanillaLoot;
    }

    public void setDropsVanillaLoot(boolean dropsVanillaLoot) {
        this.dropsVanillaLoot = dropsVanillaLoot;
    }

    public List<String> getTrails() {
        return this.trails;
    }

    public void setTrails(List<String> trails) {
        this.trails = trails;
    }

    public List<String> getOnDamageMessages() {
        return onDamageMessages;
    }

    public void setOnDamageMessages(List<String> onDamageMessages) {
        this.onDamageMessages = onDamageMessages;
    }

    public List<String> getOnDamagedMessages() {
        return onDamagedMessages;
    }

    public void setOnDamagedMessages(List<String> onDamagedMessages) {
        this.onDamagedMessages = onDamagedMessages;
    }

    public double getLeashRadius() {
        return leashRadius;
    }

    public void setLeashRadius(double leashRadius) {
        this.leashRadius = leashRadius;
    }

    public void runtimeSetLeashRadius(double leashRadius) {
        this.leashRadius = leashRadius;
        this.fileConfiguration.set("leashRadius", leashRadius);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public Integer getFollowDistance() {
        return this.followDistance;
    }

    public void setFollowDistance(Integer followDistance) {
        this.followDistance = followDistance;
    }

    public boolean isRegionalBoss() {
        return regionalBoss;
    }

    public void setRegionalBoss(boolean regionalBoss) {
        this.regionalBoss = regionalBoss;
    }

    public int getSpawnCooldown() {
        return spawnCooldown;
    }

    public void setSpawnCooldown(int spawnCooldown) {
        this.spawnCooldown = spawnCooldown;
    }

    public List<String> getOnDeathCommands() {
        return onDeathCommands;
    }

    public void setOnDeathCommands(List<String> onDeathCommands) {
        this.onDeathCommands = onDeathCommands;
    }

    public List<String> getOnSpawnCommands() {
        return onSpawnCommands;
    }

    public void setOnSpawnCommands(List<String> onSpawnCommands) {
        this.onSpawnCommands = onSpawnCommands;
    }

    public List<String> getOnCombatEnterCommands() {
        return onCombatEnterCommands;
    }

    public void setOnCombatEnterCommands(List<String> onCombatEnterCommands) {
        this.onCombatEnterCommands = onCombatEnterCommands;
    }

    public List<String> getOnCombatLeaveCommands() {
        return onCombatLeaveCommands;
    }

    public void setOnCombatLeaveCommands(List<String> onCombatLeaveCommands) {
        this.onCombatLeaveCommands = onCombatLeaveCommands;
    }

    public String getMountedEntity() {
        return this.mountedEntity;
    }

    public void setMountedEntity(String mountedEntity) {
        this.mountedEntity = mountedEntity;
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

    public void setAnnouncementPriority(int announcementPriority) {
        this.announcementPriority = announcementPriority;
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

    public void setDisguise(String disguise) {
        this.disguise = disguise;
    }

    public String getCustomDisguiseData() {
        return customDisguiseData;
    }

    public void setCustomDisguiseData(String customDisguiseData) {
        this.customDisguiseData = customDisguiseData;
    }

    public Boolean getFrozen() {
        return this.frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isReinforcement() {
        return reinforcement;
    }

    public void setReinforcement(boolean reinforcement) {
        this.reinforcement = reinforcement;
    }

    public List<String> getPhases() {
        return this.phases;
    }

    public void setPhases(List<String> phases) {
        this.phases = phases;
    }

    public double getDamageModifier(Material material) {
        return damageModifiers.get(material) == null ? 1 : damageModifiers.get(material);
    }

    public HashMap<Material, Double> getDamageModifiers() {
        return damageModifiers;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return ChatColorConverter.convert(this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        if (level.equalsIgnoreCase("dynamic")) {
            return -1;
        } else {
            try {
                return Integer.valueOf(level);
            } catch (Exception ex) {
                new WarningMessage("Regional Elite Mob level for " + getFilename() + " is neither numeric nor dynamic. Fix the configuration for it.");
                return 1;
            }
        }
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean getCullReinforcements() {
        return cullReinforcements;
    }

    public void setCullReinforcements(boolean cullReinforcements) {
        this.cullReinforcements = cullReinforcements;
    }

    public boolean isFilesOutOfSync() {
        return filesOutOfSync;
    }

    public void setFilesOutOfSync(boolean filesOutOfSync) {
        this.filesOutOfSync = filesOutOfSync;
    }

    /**
     * Generates config defaults to be used by CustomBossesConfig
     */
    @Override
    public void generateConfigDefaults() {
        //initialize core defaults
        fileConfiguration.addDefault("isEnabled", enabled);
        fileConfiguration.addDefault("entityType", entityType.toString());
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("level", level);
        //All defaults below this point are optional
        if (timeout != 0) fileConfiguration.addDefault("timeout", timeout);
        if (isPersistent) fileConfiguration.addDefault("isPersistent", isPersistent);
        if (healthMultiplier != 1) fileConfiguration.addDefault("healthMultiplier", healthMultiplier);
        if (damageMultiplier != 1) fileConfiguration.addDefault("damageMultiplier", damageMultiplier);
        //warning: this makes it so defaults can't use custom model data, which doesn't really matter right now for defaults
        if (helmet != null) fileConfiguration.addDefault("helmet", helmet.getType().toString());
        if (chestplate != null) fileConfiguration.addDefault("chestplate", chestplate.getType().toString());
        if (leggings != null) fileConfiguration.addDefault("leggings", leggings.getType().toString());
        if (boots != null) fileConfiguration.addDefault("boots", boots.getType().toString());
        if (mainHand != null) fileConfiguration.addDefault("mainHand", mainHand.getType().toString());
        if (offHand != null) fileConfiguration.addDefault("offHand", offHand.getType().toString());
        if (baby) fileConfiguration.addDefault("isBaby", baby);
        if (!powers.isEmpty()) fileConfiguration.addDefault("powers", powers);
        if (spawnMessage != null) fileConfiguration.addDefault("spawnMessage", spawnMessage);
        if (deathMessage != null) fileConfiguration.addDefault("deathMessage", deathMessage);
        if (!deathMessages.isEmpty()) fileConfiguration.addDefault("deathMessages", deathMessages);
        if (escapeMessage != null) fileConfiguration.addDefault("escapeMessage", escapeMessage);
        if (locationMessage != null) fileConfiguration.addDefault("locationMessage", locationMessage);
        if (!uniqueLootList.isEmpty()) fileConfiguration.addDefault("uniqueLootList", uniqueLootList);
        if (!dropsEliteMobsLoot) fileConfiguration.addDefault("dropsEliteMobsLoot", dropsEliteMobsLoot);
        if (!dropsVanillaLoot) fileConfiguration.addDefault("dropsVanillaLoot", dropsVanillaLoot);
        if (!trails.isEmpty()) fileConfiguration.addDefault("trails", trails);
        if (!onDamageMessages.isEmpty()) fileConfiguration.addDefault("onDamageMessages", onDamageMessages);
        if (!onDamagedMessages.isEmpty()) fileConfiguration.addDefault("onDamagedMessages", onDamagedMessages);
        if (regionalBoss) fileConfiguration.addDefault("isRegionalBoss", regionalBoss);
        if (spawnCooldown > 0) fileConfiguration.addDefault("spawnCooldown", spawnCooldown);
        if (leashRadius > 0) fileConfiguration.addDefault("leashRadius", leashRadius);
        if (!onDeathCommands.isEmpty()) fileConfiguration.addDefault("onDeathCommands", onDeathCommands);
        if (announcementPriority > 0) fileConfiguration.addDefault("announcementPriority", announcementPriority);
        if (disguise != null) fileConfiguration.addDefault("disguise", disguise);
        if (customDisguiseData != null) fileConfiguration.addDefault("customDisguiseData", customDisguiseData);
        if (followDistance > 0) fileConfiguration.addDefault("followDistance", followDistance);
        if (!phases.isEmpty()) fileConfiguration.addDefault("phases", phases);
        if (mountedEntity != null) fileConfiguration.addDefault("mountedEntity", mountedEntity);
        if (!cullReinforcements) fileConfiguration.addDefault("cullReinforcements", cullReinforcements);
        if (frozen) fileConfiguration.addDefault("frozen", frozen);
        if (reinforcement) fileConfiguration.addDefault("reinforcement", reinforcement);
    }

    @Override
    public void processConfigFields() {
        customBossConfigFields.put(getFilename(), this);
        this.entityType = processEnum("entityType", entityType);
        if (this.entityType == null) {
            new WarningMessage("Your custom boss " + getFilename() + " does not have an entity type! Make sure you set one! Defaulting to zombie.");
            return;
        }
        this.enabled = processBoolean("isEnabled", isEnabled);
        this.name = processString("name", name);
        //Levels are strings because "dynamic" is a valid value
        this.level = processString("level", level);
        this.isPersistent = processBoolean("isPersistent", isPersistent);
        this.healthMultiplier = processDouble("healthMultiplier", healthMultiplier);
        this.damageMultiplier = processDouble("damageMultiplier", damageMultiplier);
        this.baby = processBoolean("isBaby", baby);
        this.dropsEliteMobsLoot = processBoolean("dropsEliteMobsLoot", dropsEliteMobsLoot);
        this.dropsVanillaLoot = processBoolean("dropsVanillaLoot", dropsVanillaLoot);
        this.frozen = processBoolean("frozen", frozen);
        this.reinforcement = processBoolean("reinforcement", reinforcement);
        this.onDeathCommands = processStringList("onDeathCommands", onDeathCommands);
        this.onSpawnCommands = processStringList("onSpawnCommands", onSpawnCommands);
        this.onCombatEnterCommands = processStringList("onCombatEnterCommands", onCombatEnterCommands);
        this.onCombatLeaveCommands = processStringList("onCombatLeaveCommands", onCombatLeaveCommands);
        this.deathMessages = processStringList("deathMessages", deathMessages);
        this.uniqueLootList = processStringList("uniqueLootList", uniqueLootList);
        for (String entry : this.uniqueLootList) {
            try {
                CustomItem customItem = CustomItem.getCustomItem(entry.split(":")[0]);
                if (customItem == null)
                    throw new Exception();
                this.parsedUniqueLootList.put(customItem, Double.parseDouble(entry.split(":")[1]));
            } catch (Exception ex) {
                new WarningMessage("Boss " + this.getName() + " has an invalid loot entry - " + entry + " - Skipping it!");
            }
        }
        //this can't be converted directly to an enum list because there are some special string features in here
        this.powers = processStringList("powers", powers);
        this.onDamageMessages = processStringList("onDamageMessages", onDamageMessages);
        this.onDamagedMessages = processStringList("onDamagedMessages", onDamagedMessages);
        this.trails = processStringList("trails", trails);
        this.phases = processStringList("phases", phases);
        this.locationMessage = processString("locationMessage", locationMessage);
        this.mountedEntity = processString("mountedEntity", mountedEntity);
        this.spawnMessage = processString("spawnMessage", spawnMessage);
        this.deathMessage = processString("deathMessage", deathMessage);
        this.escapeMessage = processString("escapeMessage", escapeMessage);
        this.disguise = processString("disguise", disguise);
        this.customDisguiseData = processString("customDisguiseData", customDisguiseData);
        this.announcementPriority = processInt("announcementPriority", announcementPriority);
        this.followDistance = processInt("followDistance", followDistance);
        this.spawnCooldown = processInt("spawnCooldown", spawnCooldown);
        this.timeout = processInt("timeout", timeout);
        this.leashRadius = processDouble("leashRadius", leashRadius);
        this.helmet = processItemStack("helmet", helmet);
        this.chestplate = processItemStack("chestplate", chestplate);
        this.leggings = processItemStack("leggings", leggings);
        this.boots = processItemStack("boots", boots);
        this.mainHand = processItemStack("mainHand", mainHand);
        this.offHand = processItemStack("offHand", offHand);
        this.regionalBoss = processBoolean("isRegionalBoss", isRegionalBoss());
        if (isRegionalBoss()) {
            regionalElites.put(getFilename(), this);
            //Reinforcement elites are only temporary and situational, don't initialize them
            if (!isReinforcement()) {
                //Initialize the regional bosses in the world
                List<String> locations = processStringList("spawnLocations", new ArrayList<>());
                if (locations.size() < 1)
                    new InfoMessage(getFilename() + " does not have a set location yet! It will not spawn. Did you install its minidungeon?");
                for (String string : locations)
                    new RegionalBossEntity(this, string).initialize();
            }

        }

        //Might be worth cleaning up the damage multipliers at some point
        for (String rawDamageModifier : fileConfiguration.getStringList("damageModifiers")) {
            String[] parsedStrings = rawDamageModifier.split(",");
            Material material = null;
            Double multiplier = null;
            for (String parsedDamageModifier : parsedStrings) {
                if (parsedDamageModifier.contains("material:"))
                    try {
                        material = Material.getMaterial(parsedDamageModifier.replace("material:", ""));
                    } catch (Exception ex) {
                        new WarningMessage("Boss " + getFilename() + " has invalid entry " + parsedDamageModifier + " !");
                    }
                else if (parsedDamageModifier.contains("multiplier:"))
                    try {
                        multiplier = Double.parseDouble(parsedDamageModifier.replace("multiplier:", ""));
                    } catch (Exception ex) {
                        new WarningMessage("Boss " + getFilename() + " has invalid entry " + parsedDamageModifier + " !");
                    }
                else
                    new WarningMessage("Entry " + parsedDamageModifier + " is invalid for boss file " + getFilename() + " !");
            }

            if (material != null && multiplier != null)
                damageModifiers.put(material, multiplier);

        }
    }

}
