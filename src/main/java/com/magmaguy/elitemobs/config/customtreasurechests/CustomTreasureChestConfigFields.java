package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.List;

public class CustomTreasureChestConfigFields extends CustomConfigFields {

    @Getter
    private Material chestMaterial = Material.CHEST;
    @Getter
    private BlockFace facing = BlockFace.NORTH;
    @Getter
    private int chestTier = 0;
    @Getter
    private TreasureChest.DropStyle dropStyle = TreasureChest.DropStyle.SINGLE;
    @Getter
    private int restockTimer = 0;
    @Getter
    private List<Object> lootList = null;
    @Getter
    private double mimicChance = 0;
    @Getter
    private List<String> mimicCustomBossesList = null;
    @Getter
    private List<String> restockTimers = null;
    @Getter
    private List<String> effects = null;
    @Getter
    private String worldName;
    @Getter
    private Location location;
    @Getter
    private String locationString;
    @Getter
    private long restockTime = 0L;
    @Getter
    private List<String> locationsString = new ArrayList<>();
    @Getter
    private CustomLootTable customLootTable = null;
    @Getter
    @Setter
    private boolean instanced = false;


    public CustomTreasureChestConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    /**
     * Called to write defaults for a new Custom Boss Mob Entity
     */
    public CustomTreasureChestConfigFields(String fileName,
                                           boolean isEnabled,
                                           Material chestMaterial,
                                           BlockFace facing,
                                           int chestTier,
                                           Location location,
                                           TreasureChest.DropStyle dropStyle,
                                           int restockTimer,
                                           List<Object> lootList,
                                           double mimicChance,
                                           List<String> mimicCustomBossesList,
                                           long restockTime,
                                           List<String> restockTimers,
                                           List<String> effects) {

        super(fileName, isEnabled);
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

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, false, false);
        this.chestMaterial = processEnum("chestType", chestMaterial, Material.CHEST, Material.class, true);
        this.facing = processEnum("facing", facing, BlockFace.NORTH, BlockFace.class, true);
        this.chestTier = processInt("chestTier", chestTier, 0, true);
        this.worldName = processString("location", worldName, null, false);
        if (worldName != null)
            worldName = worldName.split(",")[0];
        this.dropStyle = processEnum("dropStyle", dropStyle, TreasureChest.DropStyle.SINGLE, TreasureChest.DropStyle.class, true);
        this.restockTimer = processInt("restockTimer", restockTimer, 0, true);
        this.lootList = processList("lootList", lootList, new ArrayList<>(), false);
        this.customLootTable = new CustomLootTable(this);
        this.mimicChance = processDouble("mimicChance", mimicChance, 0, true);
        this.mimicCustomBossesList = processStringList("mimicCustomBossesList", mimicCustomBossesList, new ArrayList<>(), true);
        this.restockTime = processLong("restockTime", restockTime, 0, false);
        this.restockTimers = processStringList("restockTimers", restockTimers, new ArrayList<>(), false);
        if (this.restockTimers == null) this.restockTimers = new ArrayList<>();
        this.effects = processStringList("effects", effects, new ArrayList<>(), false);
        this.locationsString = processStringList("locations", locationsString, new ArrayList<>(), false);
        this.locationString = processString("location", locationString, null, false);
        this.instanced = processBoolean("instanced", instanced, false, false);
        if (locationString != null)
            new TreasureChest(this, locationString, restockTime);
        else if (locationsString != null)
            for (String string : locationsString) {
                String[] strings = string.split(":");
                long timestamp = 0;
                if (strings.length > 1) {
                    try {
                        timestamp = Long.parseLong(strings[1]);
                    } catch (Exception exception) {
                        Logger.warn("Bad unix timestamp in locations for " + filename + " . Entry: " + strings[0]);
                    }
                }
                new TreasureChest(this, strings[0], timestamp);
            }
        else Logger.warn("No locations found for chest " + filename);
    }

    /**
     * For the new format of Treasure chests, which have multiple locations per file
     *
     * @param chestInstanceLocation
     * @param unixTimeStamp
     * @return
     */
    public synchronized TreasureChest addTreasureChest(Location chestInstanceLocation, long unixTimeStamp) {
        Location block = chestInstanceLocation.getBlock().getLocation();
        String coordinates = ConfigurationLocation.deserialize(block);
        List<String> updated = new ArrayList<>(locationsString == null ? List.of() : locationsString);
        boolean existing = false;
        for (int i = 0; i < updated.size(); i++) {
            if (!sameBlock(updated.get(i), block)) continue;
            updated.set(i, coordinates + ":" + unixTimeStamp);
            existing = true;
            break;
        }
        if (!existing) updated.add(coordinates + ":" + unixTimeStamp);
        YamlConfiguration snapshot = configurationSnapshot();
        snapshot.set("locations", updated);
        ConfigurationEngine.fileSaverSerialized(snapshot.saveToString(), file);
        locationsString = updated;
        fileConfiguration = snapshot;
        return existing ? null : new TreasureChest(this, coordinates, unixTimeStamp);
    }

    public void setRestockTime(Location location, long newRestockTime) {
        if (isInstanced()) return;
        if (!locationsString.isEmpty()) {
            addTreasureChest(location, newRestockTime);
            return;
        }

        this.restockTime = newRestockTime;
        this.fileConfiguration.set("restockTime", newRestockTime);
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            Logger.warn("Attempted to update restock time for a custom treasure chest and failed, did you delete it during runtime?");
        }
    }

    public synchronized boolean removeLocation(Location selected) {
        List<String> retained = new ArrayList<>(locationsString == null ? List.of() : locationsString);
        boolean changed = retained.removeIf(entry -> sameBlock(entry, selected));
        boolean clearLegacy = sameBlock(locationString, selected);
        if (!changed && !clearLegacy) return false;
        YamlConfiguration snapshot = configurationSnapshot();
        snapshot.set("locations", retained);
        if (clearLegacy) snapshot.set("location", null);
        ConfigurationEngine.fileSaverSerialized(snapshot.saveToString(), file);
        locationsString = retained;
        if (clearLegacy) locationString = null;
        fileConfiguration = snapshot;
        return true;
    }

    private YamlConfiguration configurationSnapshot() {
        YamlConfiguration snapshot = new YamlConfiguration();
        try { snapshot.loadFromString(fileConfiguration.saveToString()); }
        catch (InvalidConfigurationException failure) { throw new IllegalStateException("Invalid chest configuration snapshot", failure); }
        return snapshot;
    }

    private static boolean sameBlock(String entry, Location selected) {
        if (entry == null || selected == null || selected.getWorld() == null) return false;
        Location parsed = ConfigurationLocation.serialize(entry, true);
        return parsed != null && selected.getWorld().equals(parsed.getWorld())
                && parsed.getBlockX() == selected.getBlockX() && parsed.getBlockY() == selected.getBlockY()
                && parsed.getBlockZ() == selected.getBlockZ();
    }

}
