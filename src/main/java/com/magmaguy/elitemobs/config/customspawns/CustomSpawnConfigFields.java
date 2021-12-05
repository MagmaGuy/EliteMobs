package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;

public class CustomSpawnConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    @Getter
    @Setter
    int lowestYLevel = 0;
    @Getter
    @Setter
    int highestYLevel = 320;
    @Getter
    @Setter
    List<World> validWorlds = new ArrayList<>();
    @Getter
    @Setter
    List<World.Environment> validWorldEnvironments = new ArrayList<>();
    @Getter
    @Setter
    List<Biome> validBiomes = new ArrayList<>();
    @Getter
    @Setter
    private long earliestTime = 0;
    @Getter
    @Setter
    private long latestTime = 24000;
    @Getter
    @Setter
    private MoonPhaseDetector.MoonPhase moonPhase = null;
    @Getter
    @Setter
    private boolean bypassWorldGuard = false;
    @Getter
    @Setter
    private boolean canSpawnInLight = false;
    @Getter
    @Setter
    private boolean isSurfaceSpawn = false;
    @Getter
    @Setter
    private boolean isUndergroundSpawn = false;

    public CustomSpawnConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.lowestYLevel = processInt("lowestYLevel", lowestYLevel, 0, false);
        this.highestYLevel = processInt("highestYLevel", highestYLevel, 320, false);
        this.validWorlds = processWorldList("validWorlds", validWorlds, new ArrayList<>(), false);
        this.validWorldEnvironments = processEnumList("validWorldEnvironments", validWorldEnvironments, new ArrayList<>(), World.Environment.class, false);
        this.validBiomes = processEnumList("validBiomes", validBiomes, new ArrayList<>(), Biome.class, false);
        this.earliestTime = processLong("earliestTime", earliestTime, 0, false);
        this.latestTime = processLong("latestTime", latestTime, 24000, false);
        this.moonPhase = processEnum("moonPhase", moonPhase, null, false);
        this.bypassWorldGuard = processBoolean("bypassWorldGuard", bypassWorldGuard, false, false);
        this.canSpawnInLight = processBoolean("canSpawnInLight", canSpawnInLight, false, false);
        this.isSurfaceSpawn = processBoolean("isSurfaceSpawn", isSurfaceSpawn, false, false);
        this.isUndergroundSpawn = processBoolean("isUndergroundSpawn", isUndergroundSpawn, false, false);
    }
}
