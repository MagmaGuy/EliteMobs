package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;

public class CustomSpawnConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    int lowestYLevel = 0;
    int highestYLevel = 320;
    List<World> validWorlds = new ArrayList<>();
    List<World.Environment> validWorldTypes = new ArrayList<>();
    List<Biome> validBiomes = new ArrayList<>();
    private long earliestTime = 0;
    private long latestTime = 24000;
    private MoonPhaseDetector.MoonPhase moonPhase = null;
    private boolean bypassWorldGuard = false;
    private boolean canSpawnInLight = false;
    private boolean isSurfaceSpawn = false;
    private boolean isUndergroundSpawn = false;

    public CustomSpawnConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    public int getLowestYLevel() {
        return lowestYLevel;
    }

    public void setLowestYLevel(int lowestYLevel) {
        this.lowestYLevel = lowestYLevel;
    }

    public int getHighestYLevel() {
        return highestYLevel;
    }

    public void setHighestYLevel(int highestYLevel) {
        this.highestYLevel = highestYLevel;
    }

    public List<World> getValidWorlds() {
        return validWorlds;
    }

    public void setValidWorlds(List<World> validWorlds) {
        this.validWorlds = validWorlds;
    }

    public List<World.Environment> getValidWorldTypes() {
        return validWorldTypes;
    }

    public void setValidWorldTypes(List<World.Environment> validWorldTypes) {
        this.validWorldTypes = validWorldTypes;
    }

    public List<Biome> getValidBiomes() {
        return validBiomes;
    }

    public void setValidBiomes(List<Biome> validBiomes) {
        this.validBiomes = validBiomes;
    }

    public long getEarliestTime() {
        return earliestTime;
    }

    public void setEarliestTime(long earliestTime) {
        this.earliestTime = earliestTime;
    }

    public long getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(long latestTime) {
        this.latestTime = latestTime;
    }

    public MoonPhaseDetector.MoonPhase getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(MoonPhaseDetector.MoonPhase moonPhase) {
        this.moonPhase = moonPhase;
    }

    public boolean isBypassWorldGuard() {
        return bypassWorldGuard;
    }

    public void setBypassWorldGuard(boolean bypassWorldGuard) {
        this.bypassWorldGuard = bypassWorldGuard;
    }

    public boolean canSpawnInLight() {
        return canSpawnInLight;
    }

    public void setCanSpawnInLight(boolean canSpawnInLight) {
        this.canSpawnInLight = canSpawnInLight;
    }

    public boolean isSurfaceSpawn() {
        return isSurfaceSpawn;
    }

    public void setSurfaceSpawn(boolean surfaceSpawn) {
        isSurfaceSpawn = surfaceSpawn;
    }

    public boolean isUndergroundSpawn() {
        return isUndergroundSpawn;
    }

    public void setUndergroundSpawn(boolean undergroundSpawn) {
        isUndergroundSpawn = undergroundSpawn;
    }

    @Override
    public void generateConfigDefaults() {
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
