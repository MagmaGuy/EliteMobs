package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomSpawnConfigFields extends CustomConfigFields {

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
    List<String> validBiomesStrings = new ArrayList<>();
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
        List<String> extendedDefaults = new ArrayList<>();
        if (fileConfiguration.getList("validBiomesV2") == null || fileConfiguration.getList("validBiomesV2").isEmpty()) {
            for (String validBiomesString : validBiomesStrings) {
                Biome biome;
                if (!validBiomesString.contains(":")) {
                    biome = Biome.valueOf(validBiomesString.toLowerCase(Locale.ROOT));
                } else {
                    biome = Registry.BIOME.get(new NamespacedKey(validBiomesString.split(":")[0], validBiomesString.split(":")[1].toLowerCase(Locale.ROOT)));
                }
                if (biome == null && !validBiomesString.contains("minecraft:custom")) {
                    Logger.warn("Null biome for " + validBiomesString);
                    continue;
                }
                //todo: reimplement this but using the new biome system
//                List<String> customBiomes = CustomBiomeCompatibility.getCustomBiomes(biome);
//                if (customBiomes != null && !customBiomes.isEmpty())
//                    for (Biome customBiome : customBiomes) {
//                        String customBiomeString = biome.getKey().getNamespace() + ":" + customBiome.getKey().getKey();
//                        extendedDefaults.add(customBiomeString);
//                    }
            }
        }

        validBiomesStrings.addAll(extendedDefaults);
        fileConfiguration.addDefault("validBiomesV2", validBiomesStrings);
//        this.validBiomesStrings = processStringList("validBiomesV2", validBiomesStrings, validBiomesStrings, false);

        for (String validBiomesString : validBiomesStrings) {
            Biome biome;
            if (!validBiomesString.contains(":")) {
                biome = Biome.valueOf(validBiomesString.toLowerCase(Locale.ROOT));
            } else {
                biome = Registry.BIOME.get(new NamespacedKey(validBiomesString.split(":")[0], validBiomesString.split(":")[1].toLowerCase(Locale.ROOT)));
            }
            validBiomes.add(biome);
//            Logger.debug("Added biome " + biome.getKey().getKey() + " to valid biomes list for generator " + filename + " with namespace " + biome.getKey().getNamespace() + ".");
        }
        this.earliestTime = processLong("earliestTime", earliestTime, 0, false);
        this.latestTime = processLong("latestTime", latestTime, 24000, false);
        this.moonPhase = processEnum("moonPhase", moonPhase, null, MoonPhaseDetector.MoonPhase.class, false);
        this.bypassWorldGuard = processBoolean("bypassWorldGuard", bypassWorldGuard, false, false);
        this.canSpawnInLight = processBoolean("canSpawnInLight", canSpawnInLight, false, false);
        this.isSurfaceSpawn = processBoolean("isSurfaceSpawn", isSurfaceSpawn, false, false);
        this.isUndergroundSpawn = processBoolean("isUndergroundSpawn", isUndergroundSpawn, false, false);
    }
}
