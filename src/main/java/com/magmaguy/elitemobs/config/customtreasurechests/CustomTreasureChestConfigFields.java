package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.playerdata.database.DungeonRuntimeData;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

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
        List<String> legacyRestockTimers = processStringList("restockTimers", restockTimers, new ArrayList<>(), false);
        this.restockTimers = DungeonRuntimeData.loadTreasureChestPlayerCooldowns(filename, legacyRestockTimers);
        if (this.restockTimers == null) this.restockTimers = new ArrayList<>();
        this.effects = processStringList("effects", effects, new ArrayList<>(), false);
        this.locationsString = processStringList("locations", locationsString, new ArrayList<>(), false);
        this.locationString = processString("location", locationString, null, false);
        this.instanced = processBoolean("instanced", instanced, false, false);
        boolean migrated = false;
        if (locationString != null) {
            long storedRestockTime = DungeonRuntimeData.loadTreasureChestCooldown(filename, locationString, restockTime);
            new TreasureChest(this, locationString, storedRestockTime);
            migrated = restockTime > 0;
        } else if (locationsString != null) {
            List<String> authoredLocations = new ArrayList<>();
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
                String authoredLocation = strings[0];
                authoredLocations.add(authoredLocation);
                long storedRestockTime = DungeonRuntimeData.loadTreasureChestCooldown(filename, authoredLocation, timestamp);
                new TreasureChest(this, authoredLocation, storedRestockTime);
                migrated |= strings.length > 1;
            }
            if (DungeonRuntimeData.isAvailable()) this.locationsString = authoredLocations;
        } else Logger.warn("No locations found for chest " + filename);

        if (DungeonRuntimeData.isAvailable() && (migrated || hasLegacyRestockTimers(legacyRestockTimers))) {
            fileConfiguration.set("locations", locationsString);
            fileConfiguration.set("restockTime", null);
            fileConfiguration.set("restockTimers", null);
            ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
            restockTime = 0;
        }
    }

    static boolean hasLegacyRestockTimers(List<String> legacyRestockTimers) {
        return legacyRestockTimers != null && !legacyRestockTimers.isEmpty();
    }

    /**
     * For the new format of Treasure chests, which have multiple locations per file
     *
     * @param chestInstanceLocation
     * @param unixTimeStamp
     * @return
     */
    public TreasureChest addTreasureChest(Location chestInstanceLocation, long unixTimeStamp) {
        int index = -1;
        String deserializedLocation = ConfigurationLocation.deserialize(chestInstanceLocation.getBlock().getLocation());
        for (String string : locationsString)
            if (string.split(":")[0].equals(deserializedLocation)) {
                index = locationsString.indexOf(string);
                break;
            }
        boolean storedInDatabase = unixTimeStamp > 0
                ? DungeonRuntimeData.saveTreasureChestCooldown(filename, deserializedLocation, unixTimeStamp)
                : DungeonRuntimeData.clearTreasureChestCooldown(filename, deserializedLocation);
        if (!storedInDatabase)
            Logger.warn("Failed to queue the runtime cooldown for treasure chest " + filename + ".");
        String serializedUpdatedLocation = deserializedLocation;
        TreasureChest treasureChest = null;
        if (index != -1) {
            //case for existing treasure chest getting a cooldown
            locationsString.set(index, serializedUpdatedLocation);
        } else {
            //case for a new treasure chest
            locationsString.add(serializedUpdatedLocation);
            treasureChest = new TreasureChest(this, ConfigurationLocation.deserialize(chestInstanceLocation), unixTimeStamp);
        }
        fileConfiguration.set("locations", locationsString);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
        return treasureChest;
    }

    public void setRestockTime(Location location, long newRestockTime) {
        if (isInstanced()) return;
        String serializedLocation = ConfigurationLocation.deserialize(location.getBlock().getLocation());
        if (!DungeonRuntimeData.saveTreasureChestCooldown(filename, serializedLocation, newRestockTime))
            Logger.warn("Failed to queue the runtime cooldown for treasure chest " + filename + ".");
    }

    public void purgeLocations() {
        DungeonRuntimeData.clearTreasureChestCooldowns(filename);
        this.locationsString = new ArrayList<>();
        ConfigurationEngine.writeValue(null, file, fileConfiguration, "locations");
        this.locationString = null;
        ConfigurationEngine.writeValue(null, file, fileConfiguration, "location");
    }

}
