package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.DebugBlockLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.List;

public class DungeonPackagerConfigFields {

    public enum DungeonLocationType {
        WORLD,
        SCHEMATIC
    }

    public enum DungeonSizeCategory {
        LAIR,
        MINIDUNGEON,
        DUNGEON,
        RAID
    }

    private final String fileName;
    private boolean isEnabled;
    private final String name;
    private FileConfiguration fileConfiguration;
    private DungeonLocationType dungeonLocationType;
    private final List<String> customInfo;
    private final List<String> relativeBossLocations;
    private final List<String> relativeTreasureChestLocations;
    private final String downloadLink;
    private DungeonSizeCategory dungeonSizeCategory;
    private String worldName;
    private final String schematicName;
    private World.Environment environment;
    private final Boolean protect;
    private File file;
    private Location anchorPoint;
    private double rotation;
    private Vector corner1, corner2;
    private Vector teleportPoint;
    private final Double teleportPointPitch;
    private final Double teleportPointYaw;
    private final int dungeonVersion;
    private final String playerInfo;

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
                                       Double teleportPointPitch,
                                       Double teleportPointYaw,
                                       int dungeonVersion,
                                       String playerInfo) {
        this.fileName = fileName + ".yml";
        this.isEnabled = isEnabled;
        this.name = name;
        this.dungeonLocationType = dungeonLocationType;
        this.customInfo = customInfo;
        this.relativeBossLocations = relativeBossLocations;
        this.relativeTreasureChestLocations = relativeTreasureChestLocations;
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
    }

    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        fileConfiguration.addDefault("isEnabled", isEnabled);
        fileConfiguration.addDefault("name", name);
        fileConfiguration.addDefault("dungeonLocationType", dungeonLocationType.toString());
        fileConfiguration.addDefault("customInfo", customInfo);
        fileConfiguration.addDefault("relativeBossLocations", relativeBossLocations);
        fileConfiguration.addDefault("relativeTreasureLocations", relativeTreasureChestLocations);
        fileConfiguration.addDefault("downloadLink", downloadLink);
        fileConfiguration.addDefault("dungeonSizeCategory", dungeonSizeCategory.toString());
        fileConfiguration.addDefault("worldName", worldName);
        fileConfiguration.addDefault("schematicName", schematicName);
        if (environment != null)
            fileConfiguration.addDefault("environment", environment.toString());
        if (protect != null)
            fileConfiguration.addDefault("protect", protect);
        fileConfiguration.addDefault("rotation", 0);
        if (corner1 != null)
            fileConfiguration.addDefault("corner1", corner1.getBlockX() + "," + corner1.getBlockY() + "," + corner1.getBlockZ());
        if (corner2 != null)
            fileConfiguration.addDefault("corner2", corner2.getBlockX() + "," + corner2.getBlockY() + "," + corner2.getBlockZ());
        if (teleportPoint != null)
            fileConfiguration.addDefault("teleportPoint", teleportPoint.getBlockX() + "," + teleportPoint.getBlockY() + "," + teleportPoint.getBlockZ());
        fileConfiguration.addDefault("teleportPointPitch", teleportPointPitch);
        fileConfiguration.addDefault("teleportPointYaw", teleportPointYaw);
        fileConfiguration.addDefault("dungeonVersion", dungeonVersion);
        fileConfiguration.addDefault("playerInfo", playerInfo);
    }

    public DungeonPackagerConfigFields(FileConfiguration fileConfiguration, File file) {
        this.file = file;
        this.fileName = file.getName();
        this.fileConfiguration = fileConfiguration;
        this.isEnabled = fileConfiguration.getBoolean("isEnabled");
        this.name = fileConfiguration.getString("name");
        try {
            this.dungeonLocationType = DungeonLocationType.valueOf(fileConfiguration.getString("dungeonLocationType"));
        } catch (Exception exception) {
            new WarningMessage("File " + fileName + " does not have a valid dungeonLocationType!");
        }
        this.customInfo = fileConfiguration.getStringList("customInfo");
        this.relativeBossLocations = fileConfiguration.getStringList("relativeBossLocations");
        this.relativeTreasureChestLocations = fileConfiguration.getStringList("relativeTreasureChestLocations");
        this.downloadLink = fileConfiguration.getString("downloadLink");
        try {
            this.dungeonSizeCategory = DungeonSizeCategory.valueOf(fileConfiguration.getString("dungeonSizeCategory"));
        } catch (Exception exception) {
            new WarningMessage("File " + fileName + " does not have a valid dungeonSizeCategory!");
        }
        this.worldName = fileConfiguration.getString("worldName");
        this.schematicName = fileConfiguration.getString("schematicName");
        if (fileConfiguration.contains("environment"))
            this.environment = World.Environment.valueOf(fileConfiguration.getString("environment"));
        this.protect = fileConfiguration.getBoolean("protect");
        if (fileConfiguration.contains("anchorPoint"))
            this.anchorPoint = ConfigurationLocation.deserialize(fileConfiguration.getString("anchorPoint"));
        if (fileConfiguration.contains("rotation"))
            this.rotation = fileConfiguration.getDouble("rotation");
        if (fileConfiguration.contains("corner1"))
            corner1 = new Vector(
                    Integer.parseInt(fileConfiguration.getString("corner1").split(",")[0]),
                    Integer.parseInt(fileConfiguration.getString("corner1").split(",")[1]),
                    Integer.parseInt(fileConfiguration.getString("corner1").split(",")[2])
            );

        if (fileConfiguration.contains("corner2"))
            corner2 = new Vector(
                    Integer.parseInt(fileConfiguration.getString("corner2").split(",")[0]),
                    Integer.parseInt(fileConfiguration.getString("corner2").split(",")[1]),
                    Integer.parseInt(fileConfiguration.getString("corner2").split(",")[2])
            );
        if (fileConfiguration.contains("teleportPoint"))
            teleportPoint = new Vector(
                    Integer.parseInt(fileConfiguration.getString("teleportPoint").split(",")[0]),
                    Integer.parseInt(fileConfiguration.getString("teleportPoint").split(",")[1]),
                    Integer.parseInt(fileConfiguration.getString("teleportPoint").split(",")[2])
            );
        teleportPointPitch = fileConfiguration.getDouble("teleportPointPitch");
        teleportPointYaw = fileConfiguration.getDouble("teleportPointYaw");
        dungeonVersion = fileConfiguration.getInt("dungeonVersion");
        playerInfo = fileConfiguration.getString("playerInfo");
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

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

    public String getName() {
        return ChatColorConverter.convert(name);
    }

    public DungeonLocationType getDungeonLocationType() {
        return this.dungeonLocationType;
    }

    public List<String> getCustomInfo() {
        return ChatColorConverter.convert(customInfo);
    }

    public List<String> getRelativeBossLocations() {
        return relativeBossLocations;
    }

    public boolean setRelativeBossLocations(CustomBossConfigFields customBossConfigFields, Location relativeLocation) {
        String configurationLocation = customBossConfigFields.getFileName() + ":" + relativeLocation.getX() + "," + relativeLocation.getY() + "," + relativeLocation.getZ();
        relativeBossLocations.add(configurationLocation);
        return ConfigurationEngine.writeValue(relativeBossLocations, file, fileConfiguration, "relativeBossLocations");
    }

    public List<String> getRelativeTreasureChestLocations() {
        return relativeTreasureChestLocations;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public DungeonSizeCategory getDungeonSizeCategory() {
        return dungeonSizeCategory;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public World.Environment getEnvironment() {
        return this.environment;
    }

    public boolean getProtect() {
        return this.protect != null && this.protect;
    }

    public Location getAnchorPoint() {
        return this.anchorPoint;
    }

    private void setAnchorPoint(Location location) {
        Location roundedLocation = location.clone();
        roundedLocation.setX(roundedLocation.getBlockX() + 0.5);
        roundedLocation.setY(roundedLocation.getBlockY() + 0.5);
        roundedLocation.setZ(roundedLocation.getBlockZ() + 0.5);
        float roundedYaw = roundedLocation.getYaw();
        if (roundedYaw <= 45 && roundedYaw >= -45)
            roundedYaw = 0;
        else if (roundedYaw >= 45 && roundedYaw <= 135)
            roundedYaw = 90;
        else if (roundedYaw <= -45 && roundedYaw >= -135)
            roundedYaw = -90;
        else if (roundedYaw >= 135 || roundedYaw <= -135)
            roundedYaw = 180;
        roundedLocation.setYaw(roundedYaw);
        this.anchorPoint = roundedLocation;
        setRotation(roundedYaw);
        ConfigurationEngine.writeValue(ConfigurationLocation.serialize(roundedLocation), file, fileConfiguration, "anchorPoint");
        new DebugBlockLocation(roundedLocation);
    }

    private void removeAnchorPoint() {
        ConfigurationEngine.removeValue(file, fileConfiguration, "anchorPoint");
        this.anchorPoint = null;
    }

    public double getRotation() {
        return this.rotation;
    }

    public void setRotation(float roundedRotation) {
        this.rotation = roundedRotation;
        ConfigurationEngine.writeValue(rotation, file, fileConfiguration, "rotation");
    }

    public Vector getCorner1() {
        return corner1;
    }

    public Vector getCorner2() {
        return corner2;
    }

    public Vector getTeleportOffset() {
        return teleportPoint;
    }

    public Double getTeleportPointPitch() {
        return teleportPointPitch;
    }

    public Double getTeleportPointYaw() {
        return teleportPointYaw;
    }

    public int getDungeonVersion() {
        return dungeonVersion;
    }

    public String getPlayerInfo() {
        return playerInfo;
    }
}
