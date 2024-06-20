package com.magmaguy.elitemobs.config.dungeonpackager;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

public class DungeonPackagerConfigFields extends CustomConfigFields {

    @Getter
    private String name;
    @Getter
    private ContentType contentType = null;
    @Getter
    private List<String> customInfo = null;
    @Getter
    private List<String> relativeBossLocations = null;
    @Getter
    private List<String> relativeTreasureChestLocations = null;
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
    private String schematicName = null;
    @Getter
    private World.Environment environment = World.Environment.NORMAL;
    @Getter
    private boolean protect = true;
    @Getter
    private Location anchorPoint;
    @Getter
    private String defaultSchematicRotationString = null;
    @Getter
    @Setter
    private Integer calculatedRotation = 0;
    @Getter
    private Vector corner1 = new Vector(0, 0, 0), corner2 = new Vector(0, 0, 0);
    @Getter
    private int dungeonVersion = 0;
    @Getter
    private String playerInfo = null;
    @Getter
    private String regionEnterMessage = null;
    @Getter
    private String regionLeaveMessage = null;
    @Getter
    @Setter
    private List<String> worldGuardFlags = null;
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
    private String teleportLocationOffsetString = null;
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
    @Getter
    @Setter
    private boolean enchantmentChallenge = false;
    @Getter
    @Setter
    private boolean allowExplosions;

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
                                       String dungeonConfigFolderName,
                                       boolean allowExplosions) {
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
        this.allowExplosions = allowExplosions;
        defaultDungeon = true;
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
                                       int contentLevel,
                                       boolean allowExplosions) {
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
        this.allowExplosions = allowExplosions;
        defaultDungeon = true;
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, false, true);
        this.name = translatable(filename, "name", processString("name", name, null, true));
        this.customInfo = translatable(filename, "customInfo", processStringList("customInfo", customInfo, null, true));
        this.relativeBossLocations = processStringList("relativeBossLocations", relativeBossLocations, null, false);
        this.relativeTreasureChestLocations = processStringList("relativeTreasureChestLocations", relativeTreasureChestLocations, null, false);
        this.downloadLink = processString("downloadLink", downloadLink, null, false);
        this.dungeonSizeCategory = processEnum("dungeonSizeCategory", dungeonSizeCategory, null, DungeonSizeCategory.class, false);
        if (dungeonSizeCategory == null) {
            new WarningMessage("File " + filename + " does not have a valid dungeonSizeCategory!");
            this.fileConfiguration = null;
            return;
        }
        this.worldName = processString("worldName", worldName, null, false);
        this.wormholeWorldName = processString("wormholeWorldName", wormholeWorldName, null, false);
        this.schematicName = processString("schematicName", schematicName, null, false);
        this.environment = processEnum("environment", environment, null, World.Environment.class, false);
        this.protect = processBoolean("protect", protect, true, true);
        this.anchorPoint = processLocation("anchorPoint", anchorPoint, null, false);
        this.defaultSchematicRotationString = processString("defaultSchematicRotation", defaultSchematicRotationString, null, false);
        this.calculatedRotation = processInt("calculatedRotation", calculatedRotation, 0, false);
        this.corner1 = processVector("corner1", corner1, null, false);
        this.corner2 = processVector("corner2", corner2, null, false);
        this.dungeonVersion = processInt("dungeonVersion", dungeonVersion, 0, false);
        this.playerInfo = translatable(filename, "playerInfo", processString("playerInfo", playerInfo, null, false));
        this.regionEnterMessage = translatable(filename, "regionEnterMessage", processString("regionEnterMessage", regionEnterMessage, null, false));
        this.regionLeaveMessage = translatable(filename, "regionLeaveMessage", processString("regionLeaveMessage", regionLeaveMessage, null, false));
        this.hasCustomModels = processBoolean("hasCustomModels", hasCustomModels, false, false);
        this.startLocationString = processString("startLocation", startLocationString, null, false);
        this.teleportLocationString = processString("teleportLocation", teleportLocationString, null, false);
        this.teleportLocationOffsetString = processString("teleportLocationOffset", teleportLocationOffsetString, null, false);
        if (teleportLocationOffsetString != null && !teleportLocationOffsetString.isEmpty())
            this.teleportLocationOffset = ConfigurationLocation.serialize(teleportLocationOffsetString);
        this.permission = processString("permission", permission, null, false);
        this.minPlayerCount = processInt("minPlayerCount", minPlayerCount, 1, false);
        this.maxPlayerCount = processInt("maxPlayerCount", maxPlayerCount, 5, false);
        this.rawDungeonObjectives = processStringList("dungeonObjectives", rawDungeonObjectives, null, false);
        this.contentType = processEnum("contentType", contentType, null, ContentType.class, true);
        this.dungeonConfigFolderName = processString("dungeonConfigFolderName", dungeonConfigFolderName, null, false);
        this.contentLevel = processInt("contentLevel", contentLevel, 0, false);
        if (fileConfiguration.contains("difficulties"))
            this.difficulties = (List<Map<String, Object>>) fileConfiguration.getList("difficulties");
        else fileConfiguration.addDefault("difficulties", difficulties);
        enchantmentChallenge = processBoolean("enchantmentChallenge", enchantmentChallenge, false, false);
        this.allowExplosions = processBoolean("allowExplosionBlockDamage", allowExplosions, false, false);
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

    public void initializeWorld() {
        this.teleportLocation = processLocation("teleportLocation", teleportLocation, null, false);
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
        OTHER,
        ARENA
    }
}
