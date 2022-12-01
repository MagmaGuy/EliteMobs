package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptConditionsBlueprint {

    @Getter
    private Boolean isAlive = null;

    private final String scriptName;
    private final String filename;
    @Getter
    private List<String> hasTags = null;
    @Getter
    private Boolean locationIsAir = null;
    @Getter
    private Boolean isOnFloor = null;
    @Getter
    private List<String> doesNotHaveTags = null;
    @Getter
    private ScriptTargetsBlueprint scriptTargets = null;

    public ScriptConditionsBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;
        if (configurationSection.get("Conditions") == null) return;
        Map<?, ?> values = ((ConfigurationSection) configurationSection.get("Conditions")).getValues(false);
        processMapList(values);
        if (scriptTargets == null) scriptTargets = new ScriptTargetsBlueprint(new HashMap<>(), scriptName, filename);
    }

    private void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase()) {
            case "isalive" -> isAlive = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "locationisair" -> locationIsAir = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "hastags" -> hasTags = MapListInterpreter.parseStringList(key, value, scriptName);
            case "doesnothavetags" -> doesNotHaveTags = MapListInterpreter.parseStringList(key, value, scriptName);
            case "isonfloor" -> isOnFloor = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "target" -> scriptTargets = new ScriptTargetsBlueprint((Map) value, scriptName, filename);
            default -> new WarningMessage("Failed to read key " + key + " for script " + scriptName);
        }
    }

    public ScriptConditionsBlueprint(Map<?, ?> values, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;
        if (values != null) processMapList(values);
    }

    private void processMapList(Map<?, ?> entry) {
        for (Map.Entry entrySet : entry.entrySet()) {
            String key = (String) entrySet.getKey();
            processKeyAndValue(key, entrySet.getValue());
        }
    }

    public enum ConditionTarget {
        SELF,
        DIRECT_TARGET
    }
}
