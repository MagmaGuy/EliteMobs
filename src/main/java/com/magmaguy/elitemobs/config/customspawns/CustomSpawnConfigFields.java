package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomSpawnConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    public CustomSpawnConfigFields(String filename, boolean isEnabled) {
        super( filename, isEnabled);
    }

    public int getLowestYLevel() {
        return lowestYLevel;
    }

    public void setLowestYLevel(int lowestYLevel) {
        this.lowestYLevel = lowestYLevel;
    }

    int lowestYLevel = 0;

    public int getHighestYLevel() {
        return highestYLevel;
    }

    public void setHighestYLevel(int highestYLevel) {
        this.highestYLevel = highestYLevel;
    }

    int highestYLevel = 320;

    public List<World> getValidWorlds() {
        return validWorlds;
    }

    public void setValidWorlds(List<World> validWorlds) {
        this.validWorlds = validWorlds;
    }

    List<World> validWorlds = new ArrayList<>();

    public List<World.Environment> getValidWorldTypes() {
        return validWorldTypes;
    }

    public void setValidWorldTypes(List<World.Environment> validWorldTypes) {
        this.validWorldTypes = validWorldTypes;
    }

    List<World.Environment> validWorldTypes = new ArrayList<>();

    public List<Biome> getValidBiomes() {
        return validBiomes;
    }

    public void setValidBiomes(List<Biome> validBiomes) {
        this.validBiomes = validBiomes;
    }

    List<Biome> validBiomes = new ArrayList<>();

    public long getEarliestTime() {
        return earliestTime;
    }

    public void setEarliestTime(long earliestTime) {
        this.earliestTime = earliestTime;
    }

    private long earliestTime = 0;

    public long getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(long latestTime) {
        this.latestTime = latestTime;
    }

    private long latestTime = 24000;

    public MoonPhaseDetector.MoonPhase getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(MoonPhaseDetector.MoonPhase moonPhase) {
        this.moonPhase = moonPhase;
    }

    private MoonPhaseDetector.MoonPhase moonPhase = null;

    public boolean isBypassWorldGuard() {
        return bypassWorldGuard;
    }

    public void setBypassWorldGuard(boolean bypassWorldGuard) {
        this.bypassWorldGuard = bypassWorldGuard;
    }

    private boolean bypassWorldGuard = false;

    public boolean canSpawnInLight() {
        return canSpawnInLight;
    }

    public void setCanSpawnInLight(boolean canSpawnInLight) {
        this.canSpawnInLight = canSpawnInLight;
    }

    private boolean canSpawnInLight = false;

    public boolean isSurfaceSpawn() {
        return isSurfaceSpawn;
    }

    public void setSurfaceSpawn(boolean surfaceSpawn) {
        isSurfaceSpawn = surfaceSpawn;
    }

    private boolean isSurfaceSpawn = false;

    public boolean isUndergroundSpawn() {
        return isUndergroundSpawn;
    }

    public void setUndergroundSpawn(boolean undergroundSpawn) {
        isUndergroundSpawn = undergroundSpawn;
    }

    private boolean isUndergroundSpawn = false;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration, File file) {
        super.fileConfiguration = fileConfiguration;
        super.file = file;
        fileConfiguration.addDefault("isEnabled", isEnabled);
        if (lowestYLevel != 0) addDefault("lowestYLevel", lowestYLevel);
        if (highestYLevel != 320) addDefault("highestYLevel", highestYLevel);
        if (!validWorlds.isEmpty()) addDefault("validWorlds", validWorlds);
        if (!validWorldTypes.isEmpty()) addDefault("validWorldTypes", validWorldTypes);
        if (!validBiomes.isEmpty()) addDefault("validBiomes", validBiomes);
        if (earliestTime != 0) addDefault("earliestTime", earliestTime);
        if (latestTime != 24000) addDefault("latestTime", latestTime);
        if (moonPhase != null) addDefault("moonPhase", moonPhase.toString());
        if (!bypassWorldGuard) addDefault("bypassWorldGuard", bypassWorldGuard);
        if (canSpawnInLight) addDefault("canSpawnInLight", canSpawnInLight);
        if (isSurfaceSpawn) addDefault("isSurfaceSpawn", isSurfaceSpawn);
        if (isUndergroundSpawn) addDefault("isUndergroundSpawn", isUndergroundSpawn);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled);
        this.lowestYLevel = processInt("lowestYLevel", lowestYLevel);
        this.highestYLevel = processInt("highestYLevel", highestYLevel);
        this.validWorlds = processWorldList("validWorlds", validWorlds);
        this.validWorldTypes = processEnumList("validWorldTypes", validWorldTypes, World.Environment.class);
        this.validBiomes = processEnumList("validBiomes", validBiomes, Biome.class);
        this.earliestTime = processLong("earliestTime", earliestTime);
        this.latestTime = processLong("latestTime", latestTime);
        this.moonPhase = processEnum("moonPhase", moonPhase);
        this.bypassWorldGuard = processBoolean("bypassWorldGuard", bypassWorldGuard);
        this.canSpawnInLight = processBoolean("canSpawnInLight", canSpawnInLight);
        this.isSurfaceSpawn = processBoolean("isSurfaceSpawn", isSurfaceSpawn);
        this.isUndergroundSpawn = processBoolean("isUndergroundSpawn", isUndergroundSpawn);
    }
}
