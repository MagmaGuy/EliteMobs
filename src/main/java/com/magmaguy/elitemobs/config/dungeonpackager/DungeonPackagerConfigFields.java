package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.DebugBlockLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class DungeonPackagerConfigFields extends CustomConfigFields {

    @Getter
    private String name;
    @Getter
    private DungeonLocationType dungeonLocationType = DungeonLocationType.WORLD;
    @Getter
    private List<String> customInfo = new ArrayList<>();
    @Getter
    private List<String> relativeBossLocations = new ArrayList<>();
    @Getter
    private List<String> relativeTreasureChestLocations = new ArrayList<>();
    @Getter
    private String downloadLink = "";
    @Getter
    private DungeonSizeCategory dungeonSizeCategory = DungeonSizeCategory.LAIR;
    @Getter
    private String worldName;
    @Getter
    private String schematicName = "";
    @Getter
    private World.Environment environment = World.Environment.NORMAL;
    @Getter
    private boolean protect = true;
    @Getter
    private Location anchorPoint;
    @Getter
    private double rotation;
    @Getter
    private Vector corner1 = new Vector(0,0,0), corner2 = new Vector(0,0,0);
    @Getter
    private Vector teleportPoint = new Vector(0,0,0);
    @Getter
    private double teleportPointPitch = 0d;
    @Getter
    private double teleportPointYaw = 0d;
    @Getter
    private int dungeonVersion = 0;
    @Getter
    private String playerInfo = "";
    @Getter
    private String regionEnterMessage = "";
    @Getter
    private String regionLeaveMessage = "";
    @Getter
    @Setter
    private List<String> worldGuardFlags = new ArrayList<>();


    public DungeonPackagerConfigFields(String fileName, boolean isEnabled){
        super(fileName, isEnabled);
    }

    public DungeonPackagerConfigFields(String fileName,
                                       boolean isEnabled,
                                       String name,
                                       DungeonLocationType dungeonLocationType,
                                       List<String> customInfo,
                                       List<String> relativeBossLocations,
                                       List<String> relativeTreasureChestLocations,
                                       String downloadLink,
                                       DungeonSizeCategory dungeonSizeCategory,
                                       String worldName,
                                       String schematicName,
                                       World.Environment environment,
                                       Boolean protect,
                                       Vector corner1,
                                       Vector corner2,
                                       Vector teleportPoint,
                                       double teleportPointPitch,
                                       double teleportPointYaw,
                                       int dungeonVersion,
                                       String playerInfo,
                                       String regionEnterMessage,
                                       String regionLeaveMessage) {
        super(fileName, isEnabled);
        this.name = name;
        this.dungeonLocationType = dungeonLocationType;
        this.customInfo = customInfo;
        this.relativeBossLocations = new ArrayList<>(relativeBossLocations);
        this.relativeTreasureChestLocations = new ArrayList<>(relativeTreasureChestLocations);
        this.downloadLink = downloadLink;
        this.dungeonSizeCategory = dungeonSizeCategory;
        this.worldName = worldName;
        this.schematicName = schematicName;
        this.worldName = worldName;
        this.environment = environment;
        this.protect = protect;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.teleportPoint = teleportPoint;
        this.teleportPointPitch = teleportPointPitch;
        this.teleportPointYaw = teleportPointYaw;
        this.dungeonVersion = dungeonVersion;
        this.playerInfo = playerInfo;
        this.regionEnterMessage = regionEnterMessage;
        this.regionLeaveMessage = regionLeaveMessage;
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, false, true);
        this.name = processString("name", name, "name", true);
        this.dungeonLocationType = processEnum("dungeonLocationType", dungeonLocationType, null, true);
        if (dungeonLocationType == null) {
            new WarningMessage("File " + filename + " does not have a valid dungeonLocationType!");
            this.fileConfiguration = null;
            return;
        }
        this.customInfo = processStringList("customInfo", customInfo, customInfo, true);
        this.relativeBossLocations = processStringList("relativeBossLocations", relativeBossLocations, new ArrayList<>(), false);
        this.relativeTreasureChestLocations = processStringList("relativeTreasureChestLocations", relativeTreasureChestLocations, new ArrayList<>(), false);
        this.downloadLink = processString("downloadLink", downloadLink, "", false);
        this.dungeonSizeCategory = processEnum("dungeonSizeCategory", dungeonSizeCategory, null, false);
        if (dungeonSizeCategory == null) {
            new WarningMessage("File " + filename + " does not have a valid dungeonSizeCategory!");
            this.fileConfiguration = null;
            return;
        }
        this.worldName = processString("worldName", worldName, "", false);
        this.schematicName = processString("schematicName", schematicName, "", false);
        this.environment = processEnum("environment", environment, null, false);
        this.protect = processBoolean("protect", protect, true, true);
        this.anchorPoint = processLocation("anchorPoint", anchorPoint, null, false);
        this.rotation = processDouble("rotation", rotation, 0, false);
        this.corner1 = processVector("corner1", corner1, null, false);
        this.corner2 = processVector("corner2", corner2, null, false);
        this.teleportPoint = processVector("teleportPoint", teleportPoint, null, false);
        this.teleportPointPitch = processDouble("teleportPointPitch", teleportPointPitch, 0d, false);
        this.teleportPointYaw = processDouble("teleportPointYaw", teleportPointYaw, 0d, false);
        this.dungeonVersion = processInt("dungeonVersion", dungeonVersion, 0, false);
        this.playerInfo = processString("playerInfo", playerInfo, "", false);
        this.regionEnterMessage = processString("regionEnterMessage", regionEnterMessage, "", false);
        this.regionLeaveMessage = processString("regionLeaveMessage", regionLeaveMessage, "", false);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        ConfigurationEngine.writeValue(isEnabled, file, fileConfiguration, "isEnabled");
    }

    public void setEnabled(boolean isEnabled, Location anchorPoint) {
        setEnabled(isEnabled);
        if (isEnabled)
            setAnchorPoint(anchorPoint);
        else
            removeAnchorPoint();
    }

    public boolean addRelativeBossLocation(CustomBossesConfigFields customBossesConfigFields, Location relativeLocation) {
        String configurationLocation = customBossesConfigFields.getFilename() + ":" + relativeLocation.getX() + "," + relativeLocation.getY() + "," + relativeLocation.getZ();
        relativeBossLocations.add(configurationLocation);
        return ConfigurationEngine.writeValue(relativeBossLocations, file, fileConfiguration, "relativeBossLocations");
    }

    public void removeRelativeBossLocation(CustomBossesConfigFields customBossesConfigFields, Vector relativeLocation){
        String configurationLocation = customBossesConfigFields.getFilename() + ":" + relativeLocation.getX() + "," + relativeLocation.getY() + "," + relativeLocation.getZ();
        relativeBossLocations.remove(configurationLocation);
        ConfigurationEngine.writeValue(relativeBossLocations, file, fileConfiguration, "relativeBossLocations");
    }

    public boolean addRelativeTreasureChests(CustomTreasureChestConfigFields treasureChestConfigFields, Location relativeLocation) {
        String configurationLocation = treasureChestConfigFields.getFilename() + ":" + relativeLocation.getX() + "," + relativeLocation.getY() + "," + relativeLocation.getZ();
        relativeTreasureChestLocations.add(configurationLocation);
        return ConfigurationEngine.writeValue(relativeTreasureChestLocations, file, fileConfiguration, "relativeTreasureChestLocations");
    }

    private void setAnchorPoint(Location location) {
        Location roundedLocation = location.clone();
        roundedLocation.setX(roundedLocation.getBlockX() + 0.5);
        roundedLocation.setY(roundedLocation.getBlockY() + 0.5);
        roundedLocation.setZ(roundedLocation.getBlockZ() + 0.5);
        float roundedYaw = roundedLocation.getYaw();
        while (roundedYaw < -225F) {
            roundedYaw += 360F;
        }
        while (roundedYaw > 225F) {
            roundedYaw -= 360F;
        }
        if (roundedYaw <= 45F && roundedYaw >= -45F)
            roundedYaw = 0F;
        else if (roundedYaw >= 45F && roundedYaw <= 135F)
            roundedYaw = 90F;
        else if (roundedYaw <= -45F && roundedYaw >= -135F)
            roundedYaw = -90F;
        else if (roundedYaw >= 135F || roundedYaw <= -135F)
            roundedYaw = 180F;
        roundedLocation.setYaw(roundedYaw);
        this.anchorPoint = roundedLocation;
        setRotation(roundedYaw);
        ConfigurationEngine.writeValue(ConfigurationLocation.deserialize(roundedLocation), file, fileConfiguration, "anchorPoint");
        new DebugBlockLocation(roundedLocation);
    }

    private void removeAnchorPoint() {
        ConfigurationEngine.removeValue(file, fileConfiguration, "anchorPoint");
        this.anchorPoint = null;
    }

    public void setRotation(float roundedRotation) {
        this.rotation = roundedRotation;
        ConfigurationEngine.writeValue(rotation, file, fileConfiguration, "rotation");
    }

    public enum DungeonLocationType {
        WORLD,
        SCHEMATIC
    }

    public enum DungeonSizeCategory {
        LAIR,
        MINIDUNGEON,
        DUNGEON,
        RAID,
        ADVENTURE
    }
}
