package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DungeonPackagerConfigFields extends CustomConfigFields {

    @Getter
    private String name;
    @Getter
    private DungeonLocationType dungeonLocationType = DungeonLocationType.WORLD;
    @Getter
    private ContentType contentType = null;
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
    @Setter
    private String wormholeWorldName;
    @Getter
    private String schematicName = "";
    @Getter
    private World.Environment environment = World.Environment.NORMAL;
    @Getter
    private boolean protect = true;
    @Getter
    private Location anchorPoint;
    @Getter
    private String defaultSchematicRotationString = null;
    private SchematicPackage.SchematicRotation defaultSchematicRotation = null;
    @Getter
    @Setter
    private Integer calculatedRotation = 0;
    @Getter
    private Vector corner1 = new Vector(0, 0, 0), corner2 = new Vector(0, 0, 0);
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
    @Getter
    @Setter
    private boolean hasCustomModels = false;
    @Getter
    private boolean defaultDungeon = false;
    @Getter
    private Location teleportLocation = null;
    @Getter
    private String teleportLocationString = null;
    @Getter
    private String teleportLocationOffsetString = "";
    @Getter
    private Location teleportLocationOffset = null;
    @Getter
    private String permission = null;
    @Getter
    @Setter
    private int minPlayerCount = 1;
    @Getter
    @Setter
    private int maxPlayerCount = 5;
    @Getter
    @Setter
    private List<String> rawDungeonObjectives = null;
    @Getter
    private String startLocationString = null;
    @Getter
    private String dungeonConfigFolderName;
    @Getter
    private int contentLevel;
    @Getter
    @Setter
    private List<Map<String, Object>> difficulties;

    public DungeonPackagerConfigFields(String fileName, boolean isEnabled) {
        super(fileName, isEnabled);
    }

    /**
     * Used by the world-based open dungeons
     *
     * @param filename               Filename of the dungeon
     * @param isEnabled              If the dungeon is enabled
     * @param name                   Human name of the dungeon
     * @param customInfo             Info displayed to players in menus
     * @param downloadLink           Download link for the dungeon
     * @param dungeonSizeCategory    Size of the dungeon
     * @param worldName              Name of the world in the Minecraft files
     * @param environment            World environment, is shared with the wormhole world
     * @param protect                If the dungeon should be protected with WorldGuard
     * @param teleportLocationString Location to teleport players to
     * @param dungeonVersion         Version of the dungeon
     * @param playerInfo             Additional custom info for players
     * @param regionEnterMessage     Message upon entering the region
     * @param regionLeaveMessage     Message upon leaving the region
     */
    public DungeonPackagerConfigFields(String filename,
                                       boolean isEnabled,
                                       String name,
                                       List<String> customInfo,
                                       String downloadLink,
                                       DungeonSizeCategory dungeonSizeCategory,
                                       String worldName,
                                       World.Environment environment,
                                       Boolean protect,
                                       String teleportLocationString,
                                       int dungeonVersion,
                                       String playerInfo,
                                       String regionEnterMessage,
                                       String regionLeaveMessage,
                                       String dungeonConfigFolderName) {
        super(filename, isEnabled);
        this.contentType = ContentType.OPEN_DUNGEON;
        this.name = name;
        this.customInfo = customInfo;
        this.downloadLink = downloadLink;
        this.dungeonSizeCategory = dungeonSizeCategory;
        this.worldName = worldName;
        this.environment = environment;
        this.protect = protect;
        this.teleportLocationString = teleportLocationString;
        this.dungeonVersion = dungeonVersion;
        this.playerInfo = playerInfo;
        this.regionEnterMessage = regionEnterMessage;
        this.regionLeaveMessage = regionLeaveMessage;
        this.dungeonConfigFolderName = dungeonConfigFolderName;
    }

    //For instanced dungeons
    public DungeonPackagerConfigFields(String filename,
                                       boolean isEnabled,
                                       String name,
                                       List<String> customInfo,
                                       String downloadLink,
                                       DungeonSizeCategory dungeonSizeCategory,
                                       String worldName,
                                       World.Environment environment,
                                       Boolean protect,
                                       String teleportLocationString,
                                       String startLocationString,
                                       int dungeonVersion,
                                       String playerInfo,
                                       String regionEnterMessage,
                                       String regionLeaveMessage,
                                       List<String> rawDungeonObjectives,
                                       String dungeonConfigFolderName,
                                       int contentLevel) {
        super(filename, isEnabled);
        this.contentType = ContentType.INSTANCED_DUNGEON;
        this.name = name;
        this.customInfo = customInfo;
        this.downloadLink = downloadLink;
        this.dungeonSizeCategory = dungeonSizeCategory;
        this.worldName = worldName;
        this.environment = environment;
        this.protect = protect;
        this.teleportLocationString = teleportLocationString;
        this.startLocationString = startLocationString;
        this.dungeonVersion = dungeonVersion;
        this.playerInfo = playerInfo;
        this.regionEnterMessage = regionEnterMessage;
        this.regionLeaveMessage = regionLeaveMessage;
        this.rawDungeonObjectives = rawDungeonObjectives;
        this.dungeonConfigFolderName = dungeonConfigFolderName;
        this.contentLevel = contentLevel;
    }

    /**
     * Used by schematic-based dungeons
     *
     * @param filename                       Filename of the dungeon
     * @param isEnabled                      If the dungeon is enabled
     * @param name                           Human name of the dungeon
     * @param customInfo                     Info displayed to players in menus
     * @param relativeBossLocations          List of relative locations for the bosses
     * @param relativeTreasureChestLocations List of relative locations for the treasure chests
     * @param downloadLink                   Download link for the dungeon
     * @param dungeonSizeCategory            Size of the dungeon
     * @param schematicName                  Name of the schematic file of the dungeon
     * @param protect                        If the dungeon should be protected with WorldGuard
     * @param corner1                        Corner of the dungeon for creating a region
     * @param corner2                        Other corner of the dungeon for creating a region
     * @param teleportLocationOffsetString   Point to teleport to offset from the anchor point of the schematic
     * @param dungeonVersion                 Version of the dungeon
     * @param playerInfo                     Additional custom info for players
     * @param regionEnterMessage             Message upon entering the region
     * @param regionLeaveMessage             Message upon leaving the region
     */
    public DungeonPackagerConfigFields(String filename,
                                       boolean isEnabled,
                                       String name,
                                       List<String> customInfo,
                                       List<String> relativeBossLocations,
                                       List<String> relativeTreasureChestLocations,
                                       String downloadLink,
                                       DungeonSizeCategory dungeonSizeCategory,
                                       String schematicName,
                                       Boolean protect,
                                       Vector corner1,
                                       Vector corner2,
                                       String teleportLocationOffsetString,
                                       int dungeonVersion,
                                       String playerInfo,
                                       String regionEnterMessage,
                                       String regionLeaveMessage,
                                       String defaultSchematicRotation,
                                       String dungeonConfigFolderName) {
        super(filename, isEnabled);
        this.contentType = ContentType.SCHEMATIC_DUNGEON;
        this.name = name;
        this.customInfo = customInfo;
        this.relativeBossLocations = relativeBossLocations;
        this.relativeTreasureChestLocations = relativeTreasureChestLocations;
        this.downloadLink = downloadLink;
        this.dungeonSizeCategory = dungeonSizeCategory;
        this.schematicName = schematicName;
        this.protect = protect;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.teleportLocationOffsetString = teleportLocationOffsetString;
        this.dungeonVersion = dungeonVersion;
        this.playerInfo = playerInfo;
        this.regionEnterMessage = regionEnterMessage;
        this.regionLeaveMessage = regionLeaveMessage;
        this.defaultSchematicRotationString = defaultSchematicRotation;
        this.dungeonConfigFolderName = dungeonConfigFolderName;
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
                                       String teleportLocationString,
                                       String wormholeLocationString,
                                       int dungeonVersion,
                                       String playerInfo,
                                       String regionEnterMessage,
                                       String regionLeaveMessage,
                                       String dungeonConfigFolderName) {
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
        this.environment = environment;
        this.protect = protect;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.teleportLocationString = teleportLocationString;
        this.dungeonVersion = dungeonVersion;
        this.playerInfo = playerInfo;
        this.regionEnterMessage = regionEnterMessage;
        this.regionLeaveMessage = regionLeaveMessage;
        this.dungeonConfigFolderName = dungeonConfigFolderName;
        defaultDungeon = true;
    }

    public SchematicPackage.SchematicRotation getDefaultSchematicRotation() {
        if (defaultSchematicRotation == null) return SchematicPackage.SchematicRotation.SOUTH;
        return defaultSchematicRotation;
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, false, true);
        this.name = translatable(filename, "name", processString("name", name, "name", true));
        this.dungeonLocationType = processEnum("dungeonLocationType", dungeonLocationType, null, DungeonLocationType.class, true);
        if (dungeonLocationType == null) {
            new WarningMessage("File " + filename + " does not have a valid dungeonLocationType!");
            this.fileConfiguration = null;
            return;
        }
        this.customInfo = translatable(filename, "customInfo", processStringList("customInfo", customInfo, customInfo, true));
        this.relativeBossLocations = processStringList("relativeBossLocations", relativeBossLocations, new ArrayList<>(), false);
        this.relativeTreasureChestLocations = processStringList("relativeTreasureChestLocations", relativeTreasureChestLocations, new ArrayList<>(), false);
        this.downloadLink = processString("downloadLink", downloadLink, "", false);
        this.dungeonSizeCategory = processEnum("dungeonSizeCategory", dungeonSizeCategory, null, DungeonSizeCategory.class, false);
        if (dungeonSizeCategory == null) {
            new WarningMessage("File " + filename + " does not have a valid dungeonSizeCategory!");
            this.fileConfiguration = null;
            return;
        }
        this.worldName = processString("worldName", worldName, "", false);
        this.wormholeWorldName = processString("wormholeWorldName", wormholeWorldName, "", false);
        this.schematicName = processString("schematicName", schematicName, "", false);
        this.environment = processEnum("environment", environment, null, World.Environment.class, false);
        this.protect = processBoolean("protect", protect, true, true);
        this.anchorPoint = processLocation("anchorPoint", anchorPoint, null, false);
        this.defaultSchematicRotationString = processString("defaultSchematicRotation", defaultSchematicRotationString, null, false);
        if (defaultSchematicRotationString != null)
            try {
                if (defaultSchematicRotationString == null || defaultSchematicRotationString.isEmpty())
                    defaultSchematicRotationString = "0";
                this.defaultSchematicRotation = SchematicPackage.SchematicRotation.valueOf(defaultSchematicRotationString);
            } catch (Exception ex) {
                new WarningMessage("Bad default schematic rotation for dungeon " + filename);
            }
        this.calculatedRotation = processInt("calculatedRotation", calculatedRotation, 0, false);
        this.corner1 = processVector("corner1", corner1, null, false);
        this.corner2 = processVector("corner2", corner2, null, false);
        this.dungeonVersion = processInt("dungeonVersion", dungeonVersion, 0, false);
        this.playerInfo = translatable(filename, "playerInfo", processString("playerInfo", playerInfo, "", false));
        this.regionEnterMessage = translatable(filename, "regionEnterMessage", processString("regionEnterMessage", regionEnterMessage, "", false));
        this.regionLeaveMessage = translatable(filename, "regionLeaveMessage", processString("regionLeaveMessage", regionLeaveMessage, "", false));
        this.hasCustomModels = processBoolean("hasCustomModels", hasCustomModels, false, false);
        this.startLocationString = processString("startLocation", startLocationString, null, false);
        this.teleportLocationString = processString("teleportLocation", teleportLocationString, null, false);
        this.teleportLocationOffsetString = processString("teleportLocationOffset", teleportLocationOffsetString, "", false);
        if (teleportLocationOffsetString != null && !teleportLocationOffsetString.isEmpty())
            this.teleportLocationOffset = ConfigurationLocation.serialize(teleportLocationOffsetString);
        this.permission = processString("permission", permission, null, false);
        this.minPlayerCount = processInt("minPlayerCount", minPlayerCount, 1, false);
        this.maxPlayerCount = processInt("maxPlayerCount", maxPlayerCount, 5, false);
        this.rawDungeonObjectives = processStringList("dungeonObjectives", rawDungeonObjectives, null, false);
        this.dungeonConfigFolderName = processString("dungeonConfigFolderName", dungeonConfigFolderName, null, false);
        this.contentLevel = processInt("contentLevel", contentLevel, 0, false);
        if (fileConfiguration.getConfigurationSection("difficulties") != null)
            this.difficulties = (List<Map<String, Object>>) fileConfiguration.getList("difficulties");
        else
            fileConfiguration.addDefault("difficulties", difficulties);
        processAdditionalFields();
    }

    public void processAdditionalFields() {
    }

    /**
     * This just sets the installed status to true, doesn't really do anything else
     */
    public void simpleInstall() {
        this.isEnabled = true;
        ConfigurationEngine.writeValue(true, file, fileConfiguration, "isEnabled");
    }

    public void installWorld() {
        this.isEnabled = true;
        ConfigurationEngine.writeValue(true, file, fileConfiguration, "isEnabled");
        this.teleportLocation = processLocation("teleportLocation", teleportLocation, null, false);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public void installSchematic(Location location, int calculatedRotation, SchematicPackage schematicPackage) {
        this.isEnabled = true;
        ConfigurationEngine.writeValue(true, file, fileConfiguration, "isEnabled");
        this.anchorPoint = location;
        ConfigurationEngine.writeValue(ConfigurationLocation.deserialize(location), file, fileConfiguration, "anchorPoint");
        if (teleportLocationOffset == null) teleportLocationOffset = new Location(null, 0, 0, 0, 0, 0);
        this.teleportLocation = schematicPackage.toRealPosition(teleportLocationOffset.toVector());
        this.teleportLocation.setYaw(location.getYaw() + teleportLocationOffset.getYaw());
        this.teleportLocation.setPitch(location.getPitch() + teleportLocationOffset.getPitch());
        ConfigurationEngine.writeValue(ConfigurationLocation.deserialize(teleportLocation), file, fileConfiguration, "teleportLocation");
        this.calculatedRotation = calculatedRotation;
        ConfigurationEngine.writeValue(calculatedRotation, file, fileConfiguration, "calculatedRotation");
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public void initializeWorld() {
        this.teleportLocation = processLocation("teleportLocation", teleportLocation, null, false);
    }

    public void initializeSchematic() {
        this.teleportLocation = ConfigurationLocation.serialize(teleportLocationString);
    }

    public void simpleUninstall() {
        this.isEnabled = false;
        ConfigurationEngine.writeValue(false, file, fileConfiguration, "isEnabled");
    }

    public void uninstallWorld() {
        this.isEnabled = false;
        ConfigurationEngine.writeValue(false, file, fileConfiguration, "isEnabled");
        this.teleportLocation = null;
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public void uninstallSchematic() {
        this.isEnabled = false;
        ConfigurationEngine.writeValue(false, file, fileConfiguration, "isEnabled");
        this.teleportLocation = null;
        ConfigurationEngine.writeValue(null, file, fileConfiguration, "teleportLocation");
        this.calculatedRotation = 0;
        ConfigurationEngine.writeValue(null, file, fileConfiguration, "calculatedRotation");
        this.anchorPoint = null;
        ConfigurationEngine.writeValue(null, file, fileConfiguration, "anchorPoint");
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }


    public boolean addRelativeBossLocation(CustomBossesConfigFields customBossesConfigFields, Vector relativeLocation) {
        String configurationLocation = customBossesConfigFields.getFilename() + ":" + relativeLocation.getX() + "," + relativeLocation.getY() + "," + relativeLocation.getZ();
        relativeBossLocations.add(configurationLocation);
        return ConfigurationEngine.writeValue(relativeBossLocations, file, fileConfiguration, "relativeBossLocations");
    }

    public void removeRelativeBossLocation(CustomBossesConfigFields customBossesConfigFields, Vector relativeLocation) {
        String configurationLocation = customBossesConfigFields.getFilename() + ":" + relativeLocation.getX() + "," + relativeLocation.getY() + "," + relativeLocation.getZ();
        relativeBossLocations.remove(configurationLocation);
        ConfigurationEngine.writeValue(relativeBossLocations, file, fileConfiguration, "relativeBossLocations");
    }

    public boolean addRelativeTreasureChests(String treasureChestFilename, Vector relativeLocation) {
        String configurationLocation = treasureChestFilename + ":" + relativeLocation.getX() + "," + relativeLocation.getY() + "," + relativeLocation.getZ();
        relativeTreasureChestLocations.add(configurationLocation);
        return ConfigurationEngine.writeValue(relativeTreasureChestLocations, file, fileConfiguration, "relativeTreasureChestLocations");
    }


    public enum DungeonLocationType {
        WORLD,
        SCHEMATIC,
        INSTANCED
    }

    public enum ContentType {
        OPEN_DUNGEON,
        INSTANCED_DUNGEON,
        HUB,
        SCHEMATIC_DUNGEON
    }

    public enum DungeonSizeCategory {
        LAIR,
        SANCTUM,
        MINIDUNGEON,
        DUNGEON,
        RAID,
        ADVENTURE,
        OTHER
    }
}
