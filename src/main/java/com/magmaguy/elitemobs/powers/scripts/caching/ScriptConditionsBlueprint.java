package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class ScriptConditionsBlueprint {
    private final String scriptName;
    private final String filename;
    @Getter
    private Boolean bossIsAlive = null;
    @Getter
    private Boolean locationIsAir = null;

    public ScriptConditionsBlueprint(ConfigurationSection configurationSection, String scriptName, String filename) {
        this.scriptName = scriptName;
        this.filename = filename;
        Map<?, ?> values = (Map<?, ?>) configurationSection.get("Conditions");
        if (values != null) processMapList(values);
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

    private void processKeyAndValue(String key, Object value) {
        switch (key.toLowerCase()) {
            case "bossisalive" -> bossIsAlive = MapListInterpreter.parseBoolean(key, value, scriptName);
            case "locationisair" -> locationIsAir = MapListInterpreter.parseBoolean(key, value, scriptName);
            default -> new WarningMessage("Failed to read key " + key + " for script " + scriptName);
        }
    }
}
