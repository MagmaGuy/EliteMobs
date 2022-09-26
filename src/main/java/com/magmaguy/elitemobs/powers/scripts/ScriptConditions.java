package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

public class ScriptConditions {
    private final String scriptName;
    private Boolean bossIsAlive = null;
    private Boolean locationIsAir = null;
    public ScriptConditions(Map<?, ?> values, String scriptName) {
        this.scriptName = scriptName;
        processMapList(values);
    }

    private void processMapList(Map<?, ?> entry) {
        for (Map.Entry entrySet : entry.entrySet()) {
            String key = (String) entrySet.getKey();
            processKeyAndValue(key, entrySet.getValue());
        }
    }

    private void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase()) {
            case "bossisalive" -> bossIsAlive = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "locationisair" -> locationIsAir = MapListInterpreter.parseBoolean(key, value, scriptName);
            default -> new WarningMessage("Failed to read key " + key + " for script " + scriptName);
        }
    }

    private boolean bossIsAliveCheck(EliteEntity eliteEntity) {
        if (bossIsAlive == null) return true;
        return eliteEntity.exists() == bossIsAlive;
    }

    private boolean isAirCheck(EliteEntity eliteEntity, Location targetLocation) {
        if (locationIsAir == null) return true;
        return targetLocation.getBlock().getType().isAir();
    }

    public boolean meetsConditions(EliteEntity eliteEntity, LivingEntity directTarget) {
        return bossIsAliveCheck(eliteEntity);
    }

    public boolean meetsConditions(EliteEntity eliteEntity, Location location) {
        if (location == null) return true;
        return isAirCheck(eliteEntity, location);
    }

}
