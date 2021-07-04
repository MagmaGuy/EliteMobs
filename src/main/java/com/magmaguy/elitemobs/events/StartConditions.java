package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;

public class StartConditions {

    public int lowestYLevel;
    public int highestYLevel;
    public List<World> validWorlds = new ArrayList<>();
    public List<World.Environment> validWorldTypes;
    public List<Biome> validBiomes;
    public long earliestTime;
    public long latestTime;
    public MoonPhaseDetector.MoonPhase moonPhase;
    public int minimumPlayerCount;

    public StartConditions(CustomEventsConfigFields customEventsConfigFields) {
        this.lowestYLevel = customEventsConfigFields.getLowestYLevel();
        this.highestYLevel = customEventsConfigFields.getHighestYLevel();
        for (String worldString : customEventsConfigFields.getValidWorlds()){
            World worldObject = Bukkit.getWorld(worldString);
            if (worldObject != null)
                validWorlds.add(worldObject);
            else
                new WarningMessage("Custom event " + customEventsConfigFields.getFilename() + " had world " + worldString
                        + " in validWorlds but it is not a valid entry! Make sure your world name matches correctly.");
        }
        this.validWorldTypes = customEventsConfigFields.getValidWorldTypes();
        this.validBiomes = customEventsConfigFields.getValidBiomes();
        this.earliestTime = customEventsConfigFields.getEarliestTime();
        this.latestTime = customEventsConfigFields.getLatestTime();
        this.moonPhase = customEventsConfigFields.getMoonPhase();
        this.minimumPlayerCount = customEventsConfigFields.getMinimumPlayerCount();
    }

    public boolean conditionsAreValid(Location location){
        if (location.getY() < lowestYLevel) return false;
        if (location.getY() > highestYLevel) return false;
        if (!validWorlds.isEmpty() && !validWorlds.contains(location.getWorld())) return false;
        if (!validWorldTypes.isEmpty() && !validWorldTypes.contains(location.getWorld().getEnvironment())) return false;
        if (!validBiomes.isEmpty() && !validBiomes.contains(location.getBlock().getBiome())) return false;
        if (location.getWorld().getTime() < earliestTime) return false;
        if (location.getWorld().getTime() > latestTime) return false;
        if (moonPhase != null && !MoonPhaseDetector.detectMoonPhase(location.getWorld()).equals(moonPhase)) return false;
        if (Bukkit.getServer().getOnlinePlayers().size() < minimumPlayerCount) return false;
        return true;
    }

}
