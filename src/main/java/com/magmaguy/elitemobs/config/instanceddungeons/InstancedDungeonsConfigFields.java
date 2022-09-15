package com.magmaguy.elitemobs.config.instanceddungeons;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;

import java.util.List;

public class InstancedDungeonsConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    @Getter
    String dungeonName;
    @Getter
    String worldName;
    @Getter
    @Setter
    private int minimumPlayerCount = 1;
    @Getter
    @Setter
    private int maximumPlayerCount;
    @Getter
    @Setter
    private String permission = null;
    @Getter
    @Setter
    private int timeMinutes = 20;
    @Getter
    private List<String> rawDungeonObjectives = null;
    @Getter
    private String startLocation;
    @Getter
    private String endLocation;
    @Getter
    private World.Environment environment;

    /**
     * Used by plugin-generated files (defaults)
     *
     * @param filename
     */
    public InstancedDungeonsConfigFields(String filename,
                                         String dungeonName,
                                         World.Environment environment,
                                         String worldName,
                                         List<String> rawDungeonObjectives,
                                         String startLocation,
                                         String endLocation) {
        super(filename, true);
        this.dungeonName = dungeonName;
        this.environment = environment;
        this.worldName = worldName;
        this.rawDungeonObjectives = rawDungeonObjectives;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public InstancedDungeonsConfigFields(String fileName, boolean isEnabled) {
        super(fileName, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, false);
        this.dungeonName = processString("dungeonName", dungeonName, null, false);
        this.environment = processEnum("worldEnvironment", environment, World.Environment.NORMAL, World.Environment.class, false);
        this.worldName = processString("worldName", worldName, null, false);
        this.minimumPlayerCount = processInt("minimumPlayerCount", minimumPlayerCount, 1, false);
        this.maximumPlayerCount = processInt("maximumPlayerCount", maximumPlayerCount, 5, false);
        this.permission = processString("permission", permission, null, false);
        this.timeMinutes = processInt("timeMinutes", timeMinutes, 20, false);
        this.rawDungeonObjectives = processStringList("dungeonObjectives", rawDungeonObjectives, null, false);
        this.startLocation = processString("startLocation", startLocation, null, false);
        this.endLocation = processString("endLocation", endLocation, null, false);
    }
}
