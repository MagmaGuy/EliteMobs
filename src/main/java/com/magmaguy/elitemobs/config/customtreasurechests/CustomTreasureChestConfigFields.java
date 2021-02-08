package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.InfoMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;

public class CustomTreasureChestConfigFields {

    private static final HashSet<CustomTreasureChestConfigFields> customTreasureChestConfigFields = new HashSet<>();

    public static HashSet<CustomTreasureChestConfigFields> getCustomTreasureChestConfigFields() {
        return customTreasureChestConfigFields;
    }

    private final String fileName;
    private File file;
    private FileConfiguration fileConfiguration;
    private final boolean isEnabled;
    private final String chestMaterial;
    private final String facing;
    private final int chestTier;
    private String locationString;
    private Location location;
    private final String dropStyle;
    private final int restockTimer;
    private final List<String> lootList;
    private final double mimicChance;
    private final List<String> mimicCustomBossesList;
    private long restockTime;
    private final List<String> restockTimers;
    private final List<String> effects;

    /**
     * Called to write defaults for a new Custom Boss Mob Entity
     */
    public CustomTreasureChestConfigFields(String fileName,
                                           boolean isEnabled,
                                           String chestMaterial,
                                           String facing,
                                           int chestTier,
                                           Location location,
                                           String dropStyle,
                                           int restockTimer,
                                           List<String> lootList,
                                           double mimicChance,
                                           List<String> mimicCustomBossesList,
                                           long restockTime,
                                           List<String> restockTimers,
                                           List<String> effects) {

        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
        this.chestMaterial = chestMaterial;
        this.facing = facing;
        this.chestTier = chestTier;
        this.location = location;
        this.dropStyle = dropStyle;
        this.restockTimer = restockTimer;
        this.lootList = lootList;
        this.mimicChance = mimicChance;
        this.mimicCustomBossesList = mimicCustomBossesList;
        this.restockTime = restockTime;
        this.restockTimers = restockTimers;
        this.effects = effects;

    }

    /**
     * Generates config defaults to be used by CustomBossesConfig
     */
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {

        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("chestType", chestMaterial);
        fileConfiguration.addDefault("facing", facing);
        fileConfiguration.addDefault("chestTier", chestTier);
        fileConfiguration.addDefault("location", location);
        fileConfiguration.addDefault("dropStyle", dropStyle);
        fileConfiguration.addDefault("restockTimer", restockTimer);
        fileConfiguration.addDefault("lootList", lootList);
        fileConfiguration.addDefault("mimicChance", mimicChance);
        fileConfiguration.addDefault("mimicCustomBossesList", mimicCustomBossesList);
        fileConfiguration.addDefault("restockTime", restockTime);
        fileConfiguration.addDefault("restockTimers", restockTimers);
        fileConfiguration.addDefault("effects", effects);

    }

    /**
     * Pulls from the config so it can be used in other spots
     */
    public CustomTreasureChestConfigFields(FileConfiguration configuration, File file) {

        this.fileName = file.getName();

        this.file = file;

        this.fileConfiguration = configuration;

        this.isEnabled = configuration.getBoolean("isEnabled");

        this.chestMaterial = configuration.getString("chestType");

        this.facing = configuration.getString("facing");

        this.chestTier = configuration.getInt("chestTier");

        String location = configuration.getString("location");
        if (location == null) {
            new InfoMessage("Custom Treasure Chest in file " + fileName + " does not have a defined location! It will not spawn.");
        } else {
            this.locationString = configuration.getString("location");
            this.location = ConfigurationLocation.deserialize(configuration.getString("location"));
        }

        this.dropStyle = configuration.getString("dropStyle");

        this.restockTimer = configuration.getInt("restockTimer");

        this.lootList = configuration.getStringList("lootList");

        this.mimicChance = configuration.getDouble("mimicChance");

        this.mimicCustomBossesList = configuration.getStringList("mimicCustomBossesList");

        this.restockTime = configuration.getLong("restockTime");

        this.restockTimers = configuration.getStringList("restockTimers");

        this.effects = configuration.getStringList("effects");

    }

    public String getFileName() {
        return this.fileName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getChestMaterial() {
        return this.chestMaterial;
    }

    public String getFacing() {
        return this.facing;
    }

    public int getChestTier() {
        return this.chestTier;
    }

    public String getLocationString(){
        return locationString;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getDropStyle() {
        return this.dropStyle;
    }

    public void setRestockTime(long newRestockTime) {
        this.restockTime = newRestockTime;
        this.fileConfiguration.set("restockTime", newRestockTime);
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[EliteMobs] Attempted to update restock time for a custom treasure chest and failed, did you delete it during runtime?");
        }
    }

    public int getRestockTimer() {
        return this.restockTimer;
    }

    public List<String> getLootList() {
        return this.lootList;
    }

    public double getMimicChance() {
        return this.mimicChance;
    }

    public List<String> getMimicCustomBossesList() {
        return this.mimicCustomBossesList;
    }

    public long getRestockTime() {
        return restockTime;
    }

    public List<String> getRestockTimes() {
        return this.restockTimers;
    }

    public List<String> getEfffects() {
        return this.effects;
    }

}
